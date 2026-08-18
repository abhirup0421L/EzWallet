package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY orderIndex ASC, createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentItem>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentItem?

    @Query("""
        SELECT * FROM documents 
        WHERE title LIKE '%' || :query || '%' 
           OR details LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR contactName LIKE '%' || :query || '%'
           OR textContent LIKE '%' || :query || '%'
        ORDER BY orderIndex ASC, createdAt DESC
    """)
    fun searchDocuments(query: String): Flow<List<DocumentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentItem>): List<Long>

    @Update
    suspend fun updateDocument(document: DocumentItem)

    @Update
    suspend fun updateDocuments(documents: List<DocumentItem>)

    @Delete
    suspend fun deleteDocument(document: DocumentItem)

    @Query("DELETE FROM documents WHERE id IN (:ids)")
    suspend fun deleteDocumentsByIds(ids: List<Long>)
}

