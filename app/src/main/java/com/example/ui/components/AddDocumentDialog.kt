package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.DocumentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AddSourceTab(val label: String, val icon: ImageVector) {
    GALLERY("Photos / Images", Icons.Default.Image),
    PDF("PDF Documents", Icons.Default.PictureAsPdf),
    CAMERA("Camera Snap", Icons.Default.CameraAlt),
    CONTACT("Contact Card", Icons.Default.Person),
    TEXT("Text Note / Link", Icons.AutoMirrored.Filled.Note)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentSheet(
    onDismiss: () -> Unit,
    onAddFileFromUri: (title: String, details: String, date: String, category: String, uri: Uri, docType: DocumentType) -> Unit,
    onAddBulkFiles: (title: String, details: String, category: String, uris: List<Uri>, docType: DocumentType) -> Unit,
    onAddCameraPhoto: (title: String, details: String, date: String, category: String, bitmap: Bitmap) -> Unit,
    onAddContact: (title: String, details: String, date: String, category: String, name: String, phone: String) -> Unit,
    onAddTextNote: (title: String, details: String, date: String, category: String, text: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedTab by remember { mutableStateOf(AddSourceTab.GALLERY) }
    var fileName by remember { mutableStateOf("") }
    var fileDetails by remember { mutableStateOf("") }
    var documentDate by remember { mutableStateOf(todayDate) }
    var selectedCategory by remember { mutableStateOf("Personal ID") }
    var fileNameError by remember { mutableStateOf(false) }

    // Multi-file / Bulk support
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedPdfUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }

    val categories = listOf("Personal ID", "License", "Medical", "Financial", "Education", "Contact", "Work", "General")

    // Activity Result Launchers: Multi-file supported
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            if (fileName.isBlank()) {
                fileName = if (uris.size == 1) {
                    "Photo_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                } else {
                    "Batch_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                }
                fileNameError = false
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedPdfUris = uris
            if (fileName.isBlank()) {
                fileName = if (uris.size == 1) {
                    "Document_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                } else {
                    "PDF_Batch_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                }
                fileNameError = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            if (fileName.isBlank()) {
                fileName = "Snap_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                fileNameError = false
            }
        }
    }

    // Intercept back button to fulfill Requirement 12:
    // "Make the upload file section should not cancel via back button or other guestures , only tapping the cross button to close if user wants"
    BackHandler(enabled = true) {
        Toast.makeText(context, "Tap the ✕ button at top-right to close", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = {
            // Intentionally block outside tap dismissal
            Toast.makeText(context, "Tap ✕ to close upload section", Toast.LENGTH_SHORT).show()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .imePadding()
            ) {
                // Header Bar with Title and dedicated Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = "Add Documents",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Encrypted On-Device Storage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Strict Close Button (Only explicit closing allowed)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("btn_close_add_sheet")
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Type Selector Tabs
                Text(
                    text = "Select Document Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddSourceTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tab_source_${tab.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File Source Input Area based on Tab
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedTab) {
                            AddSourceTab.GALLERY -> {
                                if (selectedImageUris.isNotEmpty()) {
                                    Text(
                                        text = "✓ ${selectedImageUris.size} Photo(s) Selected",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(selectedImageUris) { uri ->
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            ) {
                                                AsyncImage(
                                                    model = uri,
                                                    contentDescription = "Selected Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                                        Text("Select Different / More Photos (Bulk)")
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Upload single or multiple images",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { photoPickerLauncher.launch("image/*") },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("btn_pick_gallery")
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Browse Gallery (Bulk Supported)")
                                        }
                                    }
                                }
                            }

                            AddSourceTab.PDF -> {
                                if (selectedPdfUris.isNotEmpty()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(34.dp)
                                            )
                                            Text(
                                                text = "${selectedPdfUris.size} PDF(s) Selected",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "1st page thumbnails will be auto-generated",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                                            Text("Change PDF Selection")
                                        }
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.UploadFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Upload PDF files with fast 1st page previews",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.testTag("btn_pick_pdf")
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Select PDF Files (Bulk Supported)")
                                        }
                                    }
                                }
                            }

                            AddSourceTab.CAMERA -> {
                                if (capturedBitmap != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = capturedBitmap,
                                            contentDescription = "Captured Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { cameraLauncher.launch() }) {
                                        Text("Retake Photo")
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Snap clear document or ID photo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { cameraLauncher.launch() },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("btn_capture_camera")
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Open Camera")
                                        }
                                    }
                                }
                            }

                            AddSourceTab.CONTACT -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = contactName,
                                        onValueChange = {
                                            contactName = it
                                            if (fileName.isBlank()) fileName = it
                                        },
                                        label = { Text("Contact Name *") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_contact_name")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = contactPhone,
                                        onValueChange = { contactPhone = it },
                                        label = { Text("Phone Number *") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_contact_phone")
                                    )
                                }
                            }

                            AddSourceTab.TEXT -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Note / Link / Sensitive Text *", style = MaterialTheme.typography.bodySmall)
                                        TextButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()
                                                if (clip != null && clip.text.isNotBlank()) {
                                                    textContent = clip.text
                                                    if (fileName.isBlank()) fileName = "Note_${SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())}"
                                                } else {
                                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Paste Clipboard")
                                        }
                                    }
                                    OutlinedTextField(
                                        value = textContent,
                                        onValueChange = { textContent = it },
                                        placeholder = { Text("Enter note text or website link (links are clickable!)...") },
                                        minLines = 3,
                                        maxLines = 6,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_text_content")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mandatory File Name Field
                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        if (it.isNotBlank()) fileNameError = false
                    },
                    label = { Text("Document / File Name (Mandatory) *") },
                    isError = fileNameError,
                    supportingText = {
                        if (fileNameError) {
                            Text("File name is required", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Will be saved inside vault under this name")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_file_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional File Details Field
                OutlinedTextField(
                    value = fileDetails,
                    onValueChange = { fileDetails = it },
                    label = { Text("File Details / Description (Optional)") },
                    placeholder = { Text("e.g. Valid until 2030, passport copy, etc.") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_file_details")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date (Optional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = documentDate,
                        onValueChange = { documentDate = it },
                        label = { Text("Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_document_date")
                    )
                    Button(
                        onClick = { documentDate = todayDate },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("Today", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (fileName.isBlank()) {
                            fileNameError = true
                            Toast.makeText(context, "Please enter a Document / File Name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        when (selectedTab) {
                            AddSourceTab.GALLERY -> {
                                if (selectedImageUris.isEmpty()) {
                                    Toast.makeText(context, "Please select at least one image file", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (selectedImageUris.size == 1) {
                                    val uri = selectedImageUris.first()
                                    val docType = if (uri.toString().endsWith(".png", ignoreCase = true)) {
                                        DocumentType.IMAGE_PNG
                                    } else {
                                        DocumentType.IMAGE_JPG
                                    }
                                    onAddFileFromUri(fileName, fileDetails, documentDate, selectedCategory, uri, docType)
                                } else {
                                    onAddBulkFiles(fileName, fileDetails, selectedCategory, selectedImageUris, DocumentType.IMAGE_JPG)
                                }
                            }

                            AddSourceTab.PDF -> {
                                if (selectedPdfUris.isEmpty()) {
                                    Toast.makeText(context, "Please select at least one PDF document", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (selectedPdfUris.size == 1) {
                                    onAddFileFromUri(fileName, fileDetails, documentDate, selectedCategory, selectedPdfUris.first(), DocumentType.PDF)
                                } else {
                                    onAddBulkFiles(fileName, fileDetails, selectedCategory, selectedPdfUris, DocumentType.PDF)
                                }
                            }

                            AddSourceTab.CAMERA -> {
                                val bitmap = capturedBitmap
                                if (bitmap == null) {
                                    Toast.makeText(context, "Please take a photo first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onAddCameraPhoto(fileName, fileDetails, documentDate, selectedCategory, bitmap)
                            }

                            AddSourceTab.CONTACT -> {
                                if (contactName.isBlank() && contactPhone.isBlank()) {
                                    Toast.makeText(context, "Please enter contact name and phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onAddContact(fileName, fileDetails, documentDate, selectedCategory, contactName, contactPhone)
                            }

                            AddSourceTab.TEXT -> {
                                if (textContent.isBlank()) {
                                    Toast.makeText(context, "Please enter text or note content", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                onAddTextNote(fileName, fileDetails, documentDate, selectedCategory, textContent)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_document")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save to EzWallet Vault",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

