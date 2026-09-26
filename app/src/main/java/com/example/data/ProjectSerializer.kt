package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.model.AnimationFrame
import com.example.model.BrushType
import com.example.model.DrawingLayer
import com.example.model.DrawingStroke
import com.example.model.Project
import com.example.model.TouchPoint
import org.json.JSONArray
import org.json.JSONObject

object ProjectSerializer {

    fun serialize(project: Project): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("title", project.title)
        root.put("width", project.width)
        root.put("height", project.height)
        root.put("fps", project.fps)
        root.put("createdAt", project.createdAt)
        root.put("updatedAt", project.updatedAt)

        val framesArray = JSONArray()
        project.frames.forEach { frame ->
            val frameObj = JSONObject()
            frameObj.put("id", frame.id)
            frameObj.put("index", frame.index)

            val layersArray = JSONArray()
            frame.layers.forEach { layer ->
                val layerObj = JSONObject()
                layerObj.put("id", layer.id)
                layerObj.put("name", layer.name)
                layerObj.put("isVisible", layer.isVisible)
                layerObj.put("isLocked", layer.isLocked)
                layerObj.put("opacity", layer.opacity.toDouble())
                layerObj.put("blendMode", layer.blendMode)
                if (layer.imagePath != null) {
                    layerObj.put("imagePath", layer.imagePath)
                }

                val strokesArray = JSONArray()
                layer.strokes.forEach { stroke ->
                    val strokeObj = JSONObject()
                    strokeObj.put("id", stroke.id)
                    strokeObj.put("brush", stroke.brushType.name)
                    strokeObj.put("color", stroke.color.toArgb())
                    strokeObj.put("size", stroke.size.toDouble())
                    strokeObj.put("opacity", stroke.opacity.toDouble())

                    val pointsArray = JSONArray()
                    stroke.points.forEach { pt ->
                        val ptObj = JSONObject()
                        ptObj.put("x", pt.x.toDouble())
                        ptObj.put("y", pt.y.toDouble())
                        ptObj.put("p", pt.pressure.toDouble())
                        pointsArray.put(ptObj)
                    }
                    strokeObj.put("pts", pointsArray)
                    strokesArray.put(strokeObj)
                }
                layerObj.put("strokes", strokesArray)
                layersArray.put(layerObj)
            }
            frameObj.put("layers", layersArray)
            framesArray.put(frameObj)
        }
        root.put("frames", framesArray)
        return root.toString()
    }

    fun deserialize(jsonStr: String): Project {
        return try {
            val root = JSONObject(jsonStr)
            val id = root.optString("id")
            val title = root.optString("title", "Untitled Artwork")
            val width = root.optInt("width", 1080)
            val height = root.optInt("height", 1080)
            val fps = root.optInt("fps", 12)
            val createdAt = root.optLong("createdAt", System.currentTimeMillis())
            val updatedAt = root.optLong("updatedAt", System.currentTimeMillis())

            val framesList = mutableListOf<AnimationFrame>()
            val framesArray = root.optJSONArray("frames")
            if (framesArray != null) {
                for (i in 0 until framesArray.length()) {
                    val frameObj = framesArray.getJSONObject(i)
                    val frameId = frameObj.optString("id")
                    val frameIndex = frameObj.optInt("index", i)

                    val layersList = mutableListOf<DrawingLayer>()
                    val layersArray = frameObj.optJSONArray("layers")
                    if (layersArray != null) {
                        for (j in 0 until layersArray.length()) {
                            val layerObj = layersArray.getJSONObject(j)
                            val layerId = layerObj.optString("id")
                            val layerName = layerObj.optString("name", "Layer ${j + 1}")
                            val isVisible = layerObj.optBoolean("isVisible", true)
                            val isLocked = layerObj.optBoolean("isLocked", false)
                            val opacity = layerObj.optDouble("opacity", 1.0).toFloat()
                            val blendMode = layerObj.optString("blendMode", "عادي")
                            val imagePath = if (layerObj.has("imagePath")) layerObj.optString("imagePath") else null

                            val strokesList = mutableListOf<DrawingStroke>()
                            val strokesArray = layerObj.optJSONArray("strokes")
                            if (strokesArray != null) {
                                for (k in 0 until strokesArray.length()) {
                                    val strokeObj = strokesArray.getJSONObject(k)
                                    val strokeId = strokeObj.optString("id")
                                    val brushName = strokeObj.optString("brush", BrushType.PEN.name)
                                    val brushType = try {
                                        BrushType.valueOf(brushName)
                                    } catch (e: Exception) {
                                        BrushType.PEN
                                    }
                                    val colorArgb = strokeObj.optInt("color", android.graphics.Color.WHITE)
                                    val size = strokeObj.optDouble("size", 8.0).toFloat()
                                    val strokeOpacity = strokeObj.optDouble("opacity", 1.0).toFloat()

                                    val pointsList = mutableListOf<TouchPoint>()
                                    val pointsArray = strokeObj.optJSONArray("pts")
                                    if (pointsArray != null) {
                                        for (p in 0 until pointsArray.length()) {
                                            val ptObj = pointsArray.getJSONObject(p)
                                            pointsList.add(
                                                TouchPoint(
                                                    x = ptObj.optDouble("x").toFloat(),
                                                    y = ptObj.optDouble("y").toFloat(),
                                                    pressure = ptObj.optDouble("p", 1.0).toFloat()
                                                )
                                            )
                                        }
                                    }

                                    strokesList.add(
                                        DrawingStroke(
                                            id = strokeId,
                                            points = pointsList,
                                            brushType = brushType,
                                            color = Color(colorArgb),
                                            size = size,
                                            opacity = strokeOpacity
                                        )
                                    )
                                }
                            }

                            layersList.add(
                                DrawingLayer(
                                    id = layerId,
                                    name = layerName,
                                    isVisible = isVisible,
                                    isLocked = isLocked,
                                    opacity = opacity,
                                    blendMode = blendMode,
                                    strokes = strokesList,
                                    imagePath = imagePath
                                )
                            )
                        }
                    }

                    if (layersList.isEmpty()) {
                        layersList.add(DrawingLayer(name = "Layer 1"))
                    }

                    framesList.add(
                        AnimationFrame(
                            id = frameId,
                            index = frameIndex,
                            layers = layersList
                        )
                    )
                }
            }

            if (framesList.isEmpty()) {
                framesList.add(AnimationFrame())
            }

            Project(
                id = id,
                title = title,
                width = width,
                height = height,
                fps = fps,
                createdAt = createdAt,
                updatedAt = updatedAt,
                frames = framesList,
                isSynced = true
            )
        } catch (e: Exception) {
            Project(title = "Restored Canvas")
        }
    }
}
