package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.example.engine.CanvasRenderer
import com.example.engine.UltraSmoothStrokeEngine
import com.example.model.CanvasPaper
import com.example.model.DrawingLayer
import com.example.model.DrawingStroke
import com.example.model.SymmetryMode
import com.example.model.TouchPoint
import com.example.ui.theme.DarkBg
import kotlin.math.hypot

@Composable
fun DrawingCanvas(
    layers: List<DrawingLayer>,
    activeStroke: DrawingStroke?,
    canvasWidth: Int,
    canvasHeight: Int,
    canvasPaper: CanvasPaper = CanvasPaper.WHITE,
    showGrid: Boolean = false,
    gridSize: Float = 40f,
    symmetryMode: SymmetryMode = SymmetryMode.NONE,
    flipHorizontal: Boolean = false,
    flipVertical: Boolean = false,
    onionSkinLayers: List<DrawingLayer>? = null,
    onStartStroke: (TouchPoint) -> Unit,
    onAppendPoint: (TouchPoint) -> Unit,
    onEndStroke: () -> Unit,
    onTwoFingerTapUndo: () -> Unit = {},
    onThreeFingerTapRedo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Zoom and Pan transform states
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Double-buffered native bitmap cache for the current frame layers
    var cachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cachedLayersHash by remember { mutableStateOf(0) }

    // Recompute layers hash to know when to redraw the cached bitmap
    val currentHash = remember(layers, canvasPaper) {
        var h = 17
        h = 31 * h + canvasPaper.hashCode()
        layers.forEach { l ->
            h = 31 * h + l.id.hashCode()
            h = 31 * h + l.isVisible.hashCode()
            h = 31 * h + (l.opacity * 1000).toInt()
            h = 31 * h + l.strokes.size
            if (l.strokes.isNotEmpty()) {
                h = 31 * h + l.strokes.last().id.hashCode()
            }
        }
        h
    }

    if (cachedBitmap == null || cachedBitmap?.width != canvasWidth || cachedBitmap?.height != canvasHeight || cachedLayersHash != currentHash) {
        val bmp = cachedBitmap?.takeIf { it.width == canvasWidth && it.height == canvasHeight }
            ?: Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)

        CanvasRenderer.renderLayersToBitmap(
            bitmap = bmp,
            layers = layers,
            backgroundColor = canvasPaper.color.toArgb(),
            includeBackground = true
        )
        cachedBitmap = bmp
        cachedLayersHash = currentHash
    }

    val touchStabilizer = remember { UltraSmoothStrokeEngine.LiveTouchStabilizer() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .pointerInput(flipHorizontal, flipVertical) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    var isDrawing = false
                    var isTransforming = false
                    var maxPointersSeen = 1
                    var lastAddedPos = Offset(-999f, -999f)

                    val startCanvasPos = toCanvasCoordinates(
                        firstDown.position,
                        size.width.toFloat(),
                        size.height.toFloat(),
                        canvasWidth,
                        canvasHeight,
                        scale,
                        panOffset,
                        flipHorizontal,
                        flipVertical
                    )

                    do {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.filter { it.pressed }
                        if (pressedPointers.size > maxPointersSeen) {
                            maxPointersSeen = pressedPointers.size
                        }

                        if (pressedPointers.size >= 2) {
                            // Multi-finger gesture (Pinch zoom & pan)
                            if (isDrawing) {
                                isDrawing = false
                                touchStabilizer.reset()
                                onEndStroke()
                            }
                            isTransforming = true
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            scale = (scale * zoomChange).coerceIn(0.2f, 8.0f)
                            panOffset += panChange
                            event.changes.forEach { it.consume() }
                        } else if (pressedPointers.size == 1 && !isTransforming) {
                            val change = pressedPointers.first()
                            val canvasPos = toCanvasCoordinates(
                                change.position,
                                size.width.toFloat(),
                                size.height.toFloat(),
                                canvasWidth,
                                canvasHeight,
                                scale,
                                panOffset,
                                flipHorizontal,
                                flipVertical
                            )

                            if (!isDrawing) {
                                isDrawing = true
                                lastAddedPos = canvasPos
                                touchStabilizer.reset()
                                val smoothed = touchStabilizer.onDown(TouchPoint(canvasPos.x, canvasPos.y, 1.0f))
                                onStartStroke(smoothed)
                                change.consume()
                            } else if (change.positionChange() != Offset.Zero) {
                                val smoothed = touchStabilizer.onMove(TouchPoint(canvasPos.x, canvasPos.y, 1.0f))
                                if (smoothed != null) {
                                    lastAddedPos = Offset(smoothed.x, smoothed.y)
                                    onAppendPoint(smoothed)
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    val gestureDuration = System.currentTimeMillis() - downTime

                    // Procreate gestures: 2-finger tap = Undo, 3-finger tap = Redo
                    if (isTransforming && gestureDuration < 320) {
                        if (maxPointersSeen == 2) {
                            onTwoFingerTapUndo()
                        } else if (maxPointersSeen >= 3) {
                            onThreeFingerTapRedo()
                        }
                    }

                    if (isDrawing) {
                        touchStabilizer.reset()
                        onEndStroke()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val viewWidth = size.width
            val viewHeight = size.height

            // Center canvas in viewport with zoom and pan
            val baseScale = (viewWidth / canvasWidth.toFloat()).coerceAtMost(viewHeight / canvasHeight.toFloat()) * 0.92f
            val finalScale = baseScale * scale

            val drawLeft = (viewWidth - canvasWidth * finalScale) / 2f + panOffset.x
            val paddingTop = (viewHeight - canvasHeight * finalScale) / 2f + panOffset.y

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.translate(drawLeft, paddingTop)
                nativeCanvas.scale(finalScale, finalScale)

                // Apply Viewport Flips if toggled
                if (flipHorizontal) {
                    nativeCanvas.scale(-1f, 1f, canvasWidth / 2f, canvasHeight / 2f)
                }
                if (flipVertical) {
                    nativeCanvas.scale(1f, -1f, canvasWidth / 2f, canvasHeight / 2f)
                }

                // 1. Canvas outer boundary & chosen paper color
                val borderPaint = AndroidPaint().apply {
                    color = if (canvasPaper.isDark) AndroidColor.LTGRAY else AndroidColor.DKGRAY
                    style = AndroidPaint.Style.STROKE
                    strokeWidth = 2f / finalScale
                    alpha = 60
                }
                val bgPaint = AndroidPaint().apply {
                    color = canvasPaper.color.toArgb()
                    style = AndroidPaint.Style.FILL
                }
                nativeCanvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)

                // 2. Guide Grid (if enabled)
                if (showGrid) {
                    val gridPaint = AndroidPaint().apply {
                        color = if (canvasPaper.isDark) AndroidColor.WHITE else AndroidColor.DKGRAY
                        strokeWidth = 1f / finalScale
                        alpha = 45
                    }
                    var gx = gridSize
                    while (gx < canvasWidth) {
                        nativeCanvas.drawLine(gx, 0f, gx, canvasHeight.toFloat(), gridPaint)
                        gx += gridSize
                    }
                    var gy = gridSize
                    while (gy < canvasHeight) {
                        nativeCanvas.drawLine(0f, gy, canvasWidth.toFloat(), gy, gridPaint)
                        gy += gridSize
                    }
                }

                // 3. Onion skin layer (if enabled)
                if (onionSkinLayers != null && onionSkinLayers.isNotEmpty()) {
                    val onionBmp = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
                    CanvasRenderer.renderLayersToBitmap(
                        bitmap = onionBmp,
                        layers = onionSkinLayers,
                        backgroundColor = AndroidColor.TRANSPARENT,
                        includeBackground = false
                    )
                    val onionPaint = AndroidPaint().apply {
                        alpha = 65
                    }
                    nativeCanvas.drawBitmap(onionBmp, 0f, 0f, onionPaint)
                    onionBmp.recycle()
                }

                // 4. Draw cached layers bitmap (HWUI accelerated)
                cachedBitmap?.let { bmp ->
                    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
                    nativeCanvas.drawBitmap(bmp, 0f, 0f, paint)
                }

                // 5. Draw active in-progress stroke & live symmetry mirrors
                if (activeStroke != null && activeStroke.points.isNotEmpty()) {
                    CanvasRenderer.drawStroke(nativeCanvas, activeStroke)

                    // Draw live mirrored stroke
                    val cw = canvasWidth.toFloat()
                    val ch = canvasHeight.toFloat()
                    when (symmetryMode) {
                        SymmetryMode.VERTICAL -> {
                            val mPts = activeStroke.points.map { it.copy(x = cw - it.x) }
                            CanvasRenderer.drawStroke(nativeCanvas, activeStroke.copy(points = mPts))
                        }
                        SymmetryMode.HORIZONTAL -> {
                            val mPts = activeStroke.points.map { it.copy(y = ch - it.y) }
                            CanvasRenderer.drawStroke(nativeCanvas, activeStroke.copy(points = mPts))
                        }
                        SymmetryMode.QUAD -> {
                            val vPts = activeStroke.points.map { it.copy(x = cw - it.x) }
                            val hPts = activeStroke.points.map { it.copy(y = ch - it.y) }
                            val qPts = activeStroke.points.map { it.copy(x = cw - it.x, y = ch - it.y) }
                            CanvasRenderer.drawStroke(nativeCanvas, activeStroke.copy(points = vPts))
                            CanvasRenderer.drawStroke(nativeCanvas, activeStroke.copy(points = hPts))
                            CanvasRenderer.drawStroke(nativeCanvas, activeStroke.copy(points = qPts))
                        }
                        SymmetryMode.NONE -> {}
                    }
                }

                // 6. Draw symmetry guide dashed lines
                if (symmetryMode != SymmetryMode.NONE) {
                    val symPaint = AndroidPaint().apply {
                        color = AndroidColor.CYAN
                        alpha = 130
                        strokeWidth = 2f / finalScale
                        pathEffect = DashPathEffect(floatArrayOf(12f / finalScale, 8f / finalScale), 0f)
                    }
                    if (symmetryMode == SymmetryMode.VERTICAL || symmetryMode == SymmetryMode.QUAD) {
                        nativeCanvas.drawLine(canvasWidth / 2f, 0f, canvasWidth / 2f, canvasHeight.toFloat(), symPaint)
                    }
                    if (symmetryMode == SymmetryMode.HORIZONTAL || symmetryMode == SymmetryMode.QUAD) {
                        nativeCanvas.drawLine(0f, canvasHeight / 2f, canvasWidth.toFloat(), canvasHeight / 2f, symPaint)
                    }
                }

                // 7. Draw canvas outer subtle border
                nativeCanvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), borderPaint)

                nativeCanvas.restore()
            }
        }
    }
}

private fun toCanvasCoordinates(
    screenPos: Offset,
    viewWidth: Float,
    viewHeight: Float,
    canvasWidth: Int,
    canvasHeight: Int,
    scale: Float,
    panOffset: Offset,
    flipHorizontal: Boolean = false,
    flipVertical: Boolean = false
): Offset {
    val baseScale = (viewWidth / canvasWidth.toFloat()).coerceAtMost(viewHeight / canvasHeight.toFloat()) * 0.92f
    val finalScale = baseScale * scale
    val drawLeft = (viewWidth - canvasWidth * finalScale) / 2f + panOffset.x
    val paddingTop = (viewHeight - canvasHeight * finalScale) / 2f + panOffset.y

    var canvasX = (screenPos.x - drawLeft) / finalScale
    var canvasY = (screenPos.y - paddingTop) / finalScale

    if (flipHorizontal) {
        canvasX = canvasWidth - canvasX
    }
    if (flipVertical) {
        canvasY = canvasHeight - canvasY
    }

    return Offset(canvasX, canvasY)
}
