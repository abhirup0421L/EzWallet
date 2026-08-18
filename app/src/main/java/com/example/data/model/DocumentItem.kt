package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val details: String = "",
    val type: DocumentType,
    val internalPath: String = "",
    val thumbnailPath: String? = null,
    val date: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val contactName: String? = null,
    val contactPhone: String? = null,
    val textContent: String? = null,
    val fileSize: Long = 0L,
    val category: String = "General",
    val orderIndex: Int = 0,
    val groupId: Long? = null
)

