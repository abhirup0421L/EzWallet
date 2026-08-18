package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DocumentType

class Converters {
    @TypeConverter
    fun fromDocumentType(type: DocumentType): String {
        return type.name
    }

    @TypeConverter
    fun toDocumentType(value: String): DocumentType {
        return try {
            DocumentType.valueOf(value)
        } catch (e: Exception) {
            DocumentType.IMAGE_JPG
        }
    }
}
