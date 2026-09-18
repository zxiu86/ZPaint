package com.example.model

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Artwork",
    val width: Int = 1080,
    val height: Int = 1080,
    val fps: Int = 12,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val frames: List<AnimationFrame> = listOf(AnimationFrame()),
    val isSynced: Boolean = true,
    val thumbnailPath: String? = null
)
