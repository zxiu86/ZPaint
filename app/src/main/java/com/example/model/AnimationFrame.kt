package com.example.model

import java.util.UUID

data class AnimationFrame(
    val id: String = UUID.randomUUID().toString(),
    val index: Int = 0,
    val layers: List<DrawingLayer> = listOf(DrawingLayer(name = "Layer 1"))
)
