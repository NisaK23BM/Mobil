package com.example.haritap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.haritap.data.entity.ReportEntity

@Dao
interface ReportDao {

    @Insert
    suspend fun insert(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE status = 'Açık' ORDER BY createdAt DESC")
    suspend fun getOpen(): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE type = :type ORDER BY createdAt DESC")
    suspend fun getByType(type: String): List<ReportEntity>

    @Query("""
        SELECT * FROM reports
        WHERE (title LIKE '%' || :q || '%' 
           OR description LIKE '%' || :q || '%')
        ORDER BY createdAt DESC
    """)
    suspend fun search(q: String): List<ReportEntity>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReportEntity?

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
