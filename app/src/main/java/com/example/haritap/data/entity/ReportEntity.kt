package com.example.haritap.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,

    val status: String,
    val createdAt: Long = System.currentTimeMillis()

)
