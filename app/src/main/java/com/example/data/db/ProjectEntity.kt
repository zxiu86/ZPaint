package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean,
    val thumbnailPath: String?,
    val dataJson: String
)
