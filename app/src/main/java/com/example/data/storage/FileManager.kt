package com.example.data.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileManager {

    private fun getDocumentsDir(context: Context): File {
        val dir = File(context.filesDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getThumbnailsDir(context: Context): File {
        val dir = File(context.filesDir, "thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies URI to internal storage with the exact user's choice name embedded into the file.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, baseName: String, extension: String): Pair<String, Long> {
        val dir = getDocumentsDir(context)
        val cleanName = baseName.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "Document" }
        val uniqueSuffix = UUID.randomUUID().toString().take(5)
        val filename = "${cleanName}_$uniqueSuffix.$extension"
        val destFile = File(dir, filename)

        var bytesCopied = 0L
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                bytesCopied = input.copyTo(output)
            }
        } ?: run {
            destFile.createNewFile()
        }

        return Pair(destFile.absolutePath, destFile.length())
    }

    /**
     * Renders the 1st page of a PDF into a high-quality PNG thumbnail.
     */
    fun generatePdfThumbnail(context: Context, pdfFilePath: String): String? {
        if (pdfFilePath.isBlank()) return null
        return try {
            val pdfFile = File(pdfFilePath)
            if (!pdfFile.exists() || !pdfFile.canRead()) return null

            val thumbDir = getThumbnailsDir(context)
            val thumbFile = File(thumbDir, "thumb_${pdfFile.nameWithoutExtension}.png")
            if (thumbFile.exists() && thumbFile.length() > 0) {
                return thumbFile.absolutePath
            }

            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY) ?: return null
            val renderer = PdfRenderer(pfd)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                // Render at good scale (max width 600px for sharp thumbnail)
                val scale = 600f / page.width.coerceAtLeast(1)
                val targetW = (page.width * scale).toInt().coerceAtLeast(100)
                val targetH = (page.height * scale).toInt().coerceAtLeast(100)
                val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                FileOutputStream(thumbFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                thumbFile.absolutePath
            } else {
                renderer.close()
                pfd.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, title: String): Pair<String, Long> {
        val dir = getDocumentsDir(context)
        val cleanName = title.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "Photo" }
        val uniqueSuffix = UUID.randomUUID().toString().take(5)
        val filename = "${cleanName}_$uniqueSuffix.jpg"
        val destFile = File(dir, filename)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        return Pair(destFile.absolutePath, destFile.length())
    }

    fun saveTextToInternalStorage(context: Context, title: String, text: String): Pair<String, Long> {
        val dir = getDocumentsDir(context)
        val cleanName = title.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "Note" }
        val uniqueSuffix = UUID.randomUUID().toString().take(5)
        val filename = "${cleanName}_$uniqueSuffix.txt"
        val destFile = File(dir, filename)

        destFile.writeText(text, Charsets.UTF_8)
        return Pair(destFile.absolutePath, destFile.length())
    }

    fun saveContactVCardToInternalStorage(context: Context, name: String, phone: String): Pair<String, Long> {
        val dir = getDocumentsDir(context)
        val cleanName = name.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "Contact" }
        val uniqueSuffix = UUID.randomUUID().toString().take(5)
        val filename = "${cleanName}_$uniqueSuffix.vcf"
        val destFile = File(dir, filename)

        val vCardContent = """
            BEGIN:VCARD
            VERSION:3.0
            FN:$name
            TEL;TYPE=CELL:$phone
            END:VCARD
        """.trimIndent()

        destFile.writeText(vCardContent, Charsets.UTF_8)
        return Pair(destFile.absolutePath, destFile.length())
    }

    fun deleteInternalFile(filePath: String) {
        if (filePath.isNotEmpty()) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openFileWithSystemApp(context: Context, doc: DocumentItem) {
        try {
            if (doc.internalPath.isNotEmpty()) {
                val file = File(doc.internalPath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, doc.type.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    return
                }
            }
            Toast.makeText(context, "File path not found", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openUrlInBrowser(context: Context, rawUrl: String) {
        try {
            var url = rawUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareDocument(context: Context, doc: DocumentItem) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND)
            
            if (doc.internalPath.isNotEmpty()) {
                val file = File(doc.internalPath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    sendIntent.type = doc.type.mimeType
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, doc.title)
                    if (doc.details.isNotEmpty()) {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "${doc.title}\n${doc.details}")
                    }
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val chooser = Intent.createChooser(sendIntent, "Share ${doc.title}")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    return
                }
            }

            sendIntent.type = "text/plain"
            val textToShare = buildString {
                append(doc.title)
                if (doc.details.isNotEmpty()) append("\n").append(doc.details)
                if (!doc.contactPhone.isNullOrBlank()) append("\nPhone: ").append(doc.contactPhone)
                if (!doc.textContent.isNullOrBlank()) append("\n\n").append(doc.textContent)
            }
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, doc.title)
            val chooser = Intent.createChooser(sendIntent, "Share ${doc.title}")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMultipleDocuments(context: Context, docs: List<DocumentItem>) {
        if (docs.isEmpty()) return
        if (docs.size == 1) {
            shareDocument(context, docs.first())
            return
        }

        try {
            val uris = ArrayList<Uri>()
            val textItems = StringBuilder()

            for (doc in docs) {
                if (doc.internalPath.isNotEmpty()) {
                    val file = File(doc.internalPath)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        uris.add(uri)
                    }
                } else {
                    textItems.append("• ").append(doc.title)
                    if (!doc.contactPhone.isNullOrBlank()) textItems.append(" (${doc.contactPhone})")
                    if (!doc.textContent.isNullOrBlank()) textItems.append(": ").append(doc.textContent)
                    textItems.append("\n")
                }
            }

            val sendIntent = if (uris.isNotEmpty()) {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    if (textItems.isNotEmpty()) {
                        putExtra(Intent.EXTRA_TEXT, textItems.toString())
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textItems.toString())
                }
            }

            val chooser = Intent.createChooser(sendIntent, "Share ${docs.size} files")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share files: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportDocumentToDownloads(context: Context, doc: DocumentItem) {
        try {
            val cleanTitle = doc.title.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_").ifBlank { "Document" }
            val filename = "$cleanTitle.${doc.type.extension}"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, doc.type.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EzWallet")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        if (doc.internalPath.isNotEmpty() && File(doc.internalPath).exists()) {
                            File(doc.internalPath).inputStream().use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        } else if (!doc.textContent.isNullOrBlank()) {
                            outStream.write(doc.textContent.toByteArray(Charsets.UTF_8))
                        } else if (!doc.contactPhone.isNullOrBlank()) {
                            val vcf = "BEGIN:VCARD\nVERSION:3.0\nFN:${doc.contactName ?: doc.title}\nTEL;TYPE=CELL:${doc.contactPhone}\nEND:VCARD"
                            outStream.write(vcf.toByteArray(Charsets.UTF_8))
                        }
                    }
                    Toast.makeText(context, "Saved to Downloads/EzWallet: $filename", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to create download file", Toast.LENGTH_SHORT).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "EzWallet").apply { mkdirs() }
                val targetFile = File(targetDir, filename)
                if (doc.internalPath.isNotEmpty() && File(doc.internalPath).exists()) {
                    File(doc.internalPath).copyTo(targetFile, overwrite = true)
                } else if (!doc.textContent.isNullOrBlank()) {
                    targetFile.writeText(doc.textContent)
                }
                Toast.makeText(context, "Saved to Downloads/EzWallet: $filename", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialContact(context: Context, phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

