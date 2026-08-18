package com.example.data.model

enum class DocumentType(val label: String, val extension: String, val mimeType: String) {
    IMAGE_JPG("JPG Photo", "jpg", "image/jpeg"),
    IMAGE_PNG("PNG Image", "png", "image/png"),
    PDF("PDF Document", "pdf", "application/pdf"),
    CONTACT("Contact", "vcf", "text/x-vcard"),
    TEXT_NOTE("Copied Text", "txt", "text/plain"),
    PHOTO_CAMERA("Camera Photo", "jpg", "image/jpeg"),
    GROUP("Folder Group", "", "")
}
