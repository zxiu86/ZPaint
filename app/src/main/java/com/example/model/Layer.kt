package com.example.model

import android.graphics.Bitmap
import java.util.UUID

data class DrawingLayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Layer",
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f, // 0.0 to 1.0
    val strokes: List<DrawingStroke> = emptyList(),
    @Transient var cachedBitmap: Bitmap? = null
)
