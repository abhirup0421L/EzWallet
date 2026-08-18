package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.local.UserProfileDao
import com.example.data.model.DocumentItem
import com.example.data.model.UserProfile
import com.example.data.storage.FileManager
import kotlinx.coroutines.flow.Flow

class EzWalletRepository(
    private val documentDao: DocumentDao,
    private val userProfileDao: UserProfileDao
) {
    val allDocuments: Flow<List<DocumentItem>> = documentDao.getAllDocuments()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    fun searchDocuments(query: String): Flow<List<DocumentItem>> {
        return if (query.isBlank()) {
            documentDao.getAllDocuments()
        } else {
            documentDao.searchDocuments(query.trim())
        }
    }

    suspend fun getDocumentById(id: Long): DocumentItem? = documentDao.getDocumentById(id)

    suspend fun insertDocument(document: DocumentItem): Long = documentDao.insertDocument(document)

    suspend fun insertDocuments(documents: List<DocumentItem>): List<Long> = documentDao.insertDocuments(documents)

    suspend fun updateDocument(document: DocumentItem) = documentDao.updateDocument(document)

    suspend fun updateDocuments(documents: List<DocumentItem>) = documentDao.updateDocuments(documents)

    suspend fun deleteDocument(document: DocumentItem) {
        FileManager.deleteInternalFile(document.internalPath)
        if (!document.thumbnailPath.isNullOrBlank()) {
            FileManager.deleteInternalFile(document.thumbnailPath)
        }
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocuments(documents: List<DocumentItem>) {
        documents.forEach {
            FileManager.deleteInternalFile(it.internalPath)
            if (!it.thumbnailPath.isNullOrBlank()) {
                FileManager.deleteInternalFile(it.thumbnailPath)
            }
        }
        documentDao.deleteDocumentsByIds(documents.map { it.id })
    }

    suspend fun getUserProfileSync(): UserProfile {
        val current = userProfileDao.getUserProfileSync()
        return if (current != null) {
            current
        } else {
            val defaultProfile = UserProfile()
            userProfileDao.insertOrUpdateProfile(defaultProfile)
            defaultProfile
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile)
    }
}

