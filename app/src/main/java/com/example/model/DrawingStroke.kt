package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class TouchPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<TouchPoint> = emptyList(),
    val brushType: BrushType = BrushType.PEN,
    val color: Color = Color.White,
    val size: Float = 8f,
    val opacity: Float = 1.0f
)
