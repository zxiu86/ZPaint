package com.example.engine

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.graphics.toArgb
import com.example.model.BrushType
import com.example.model.DrawingLayer
import com.example.model.DrawingStroke
import com.example.model.TouchPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object CanvasRenderer {

    fun renderLayersToBitmap(
        bitmap: Bitmap,
        layers: List<DrawingLayer>,
        backgroundColor: Int = AndroidColor.WHITE,
        includeBackground: Boolean = true
    ) {
        val canvas = Canvas(bitmap)
        if (includeBackground) {
            canvas.drawColor(backgroundColor, PorterDuff.Mode.SRC)
        } else {
            canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        layers.forEach { layer ->
            if (layer.isVisible && layer.opacity > 0.001f) {
                val layerBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val layerCanvas = Canvas(layerBitmap)

                layer.strokes.forEach { stroke ->
                    drawStroke(layerCanvas, stroke)
                }

                layerPaint.alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
                val blendModeXfer = when (layer.blendMode) {
                    "مضاعفة" -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    "شاشة" -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                    "إضافة" -> PorterDuffXfermode(PorterDuff.Mode.ADD)
                    "تراكب" -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                    "إضاءة" -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
                    else -> null
                }
                layerPaint.xfermode = blendModeXfer
                canvas.drawBitmap(layerBitmap, 0f, 0f, layerPaint)
                layerPaint.xfermode = null
                layerBitmap.recycle()
            }
        }
    }

    fun drawStroke(canvas: Canvas, stroke: DrawingStroke) {
        if (stroke.points.isEmpty()) return

        when (stroke.brushType) {
            BrushType.PEN -> drawStandardSmoothStroke(canvas, stroke)
            BrushType.PENCIL -> drawPencilStroke(canvas, stroke)
            BrushType.MECHANICAL_PENCIL -> drawMechanicalPencilStroke(canvas, stroke)
            BrushType.STUDIO_PEN -> drawStudioPenStroke(canvas, stroke)
            BrushType.TECHNICAL_PEN -> drawTechnicalPenStroke(canvas, stroke)
            BrushType.CALLIGRAPHY -> drawCalligraphyStroke(canvas, stroke)
            BrushType.COMIC_INKER -> drawComicInkerStroke(canvas, stroke)

            BrushType.ACRYLIC_WET -> drawAcrylicStroke(canvas, stroke)
            BrushType.OIL -> drawOilStroke(canvas, stroke)
            BrushType.OIL_IMPASTO -> drawOilImpastoStroke(canvas, stroke)
            BrushType.DRY_BRISTLE -> drawDryBristleStroke(canvas, stroke)
            BrushType.PALETTE_KNIFE -> drawPaletteKnifeStroke(canvas, stroke)
            BrushType.WET_SPONGE -> drawWetSpongeStroke(canvas, stroke)
            BrushType.WATERCOLOR -> drawWatercolorStroke(canvas, stroke)
            BrushType.WATERCOLOR_BLEED -> drawWatercolorBleedStroke(canvas, stroke)
            BrushType.GOUACHE_OPAQUE -> drawGouacheStroke(canvas, stroke)
            BrushType.SUMI_E -> drawSumiEStroke(canvas, stroke)

            BrushType.PAINT_SPLATTER -> drawPaintSplatterStroke(canvas, stroke)
            BrushType.INK_DRIP -> drawInkDripStroke(canvas, stroke)
            BrushType.SPRAY_PAINT -> drawSprayPaintStroke(canvas, stroke)
            BrushType.GRUNGE_ROLLER -> drawGrungeRollerStroke(canvas, stroke)
            BrushType.STIPPLE -> drawStippleStroke(canvas, stroke)
            BrushType.CHARCOAL -> drawCharcoalStroke(canvas, stroke)
            BrushType.SOFT_PASTEL -> drawSoftPastelStroke(canvas, stroke)

            BrushType.AIRBRUSH -> drawAirbrushStroke(canvas, stroke)
            BrushType.MARKER -> drawMarkerStroke(canvas, stroke)
            BrushType.NEON -> drawNeonStroke(canvas, stroke)
            BrushType.BOKEH -> drawBokehStroke(canvas, stroke)
            BrushType.SPARKLES -> drawSparklesStroke(canvas, stroke)
            BrushType.STARS -> drawStarsStroke(canvas, stroke)
            BrushType.CLOUD_MIST -> drawCloudMistStroke(canvas, stroke)
            BrushType.HAIR_FUR -> drawHairFurStroke(canvas, stroke)

            BrushType.SMUDGE -> drawSmudgeStroke(canvas, stroke)
            BrushType.ERASER -> drawEraserStroke(canvas, stroke)
            BrushType.ERASER_SOFT -> drawSoftEraserStroke(canvas, stroke)
        }
    }

    private fun createSmoothPath(points: List<TouchPoint>): Path {
        return UltraSmoothStrokeEngine.createUltraSmoothPath(points)
    }

    private fun drawStandardSmoothStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = CornerPathEffect(stroke.size / 2f)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawStudioPenStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.05f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawTechnicalPenStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size.coerceAtLeast(1.5f)
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.MITER
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawMechanicalPencilStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 230).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = (stroke.size * 0.7f).coerceAtLeast(1.2f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawComicInkerStroke(canvas: Canvas, stroke: DrawingStroke) {
        if (stroke.points.size < 2) {
            drawStandardSmoothStroke(canvas, stroke)
            return
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            val dt = (p1.timestamp - p0.timestamp).coerceAtLeast(1)
            val dist = kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)
            val speed = dist / dt.toFloat()
            val dynamicWidth = (stroke.size * (1.4f - (speed * 0.35f).coerceIn(0f, 0.75f))).coerceAtLeast(1.5f)
            paint.strokeWidth = dynamicWidth
            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, paint)
        }
    }

    private fun drawCalligraphyStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        val angle = Math.toRadians(45.0)
        val dx = (stroke.size * cos(angle)).toFloat()
        val dy = (stroke.size * sin(angle)).toFloat()

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            val ribbon = Path().apply {
                moveTo(p0.x - dx / 2, p0.y - dy / 2)
                lineTo(p0.x + dx / 2, p0.y + dy / 2)
                lineTo(p1.x + dx / 2, p1.y + dy / 2)
                lineTo(p1.x - dx / 2, p1.y - dy / 2)
                close()
            }
            canvas.drawPath(ribbon, paint)
        }
    }

    private fun drawPencilStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 200).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    // --- Paint & Acrylic (Brusheezy Collection) ---

    private fun drawAcrylicStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 240).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = createSmoothPath(stroke.points)
        canvas.drawPath(path, bodyPaint)

        // Wet pigment sheen
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            alpha = (stroke.opacity * 45).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 0.35f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(path, sheenPaint)
    }

    private fun drawOilStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 245).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = createSmoothPath(stroke.points)
        canvas.drawPath(path, bodyPaint)
    }

    private fun drawOilImpastoStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val path = createSmoothPath(stroke.points)

        // Raised shadow ridge
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            alpha = (stroke.opacity * 50).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.15f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, shadowPaint)

        // Heavy paint body
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, bodyPaint)

        // Top highlight ridge
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            alpha = (stroke.opacity * 70).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 0.28f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, lightPaint)
    }

    private fun drawDryBristleStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 110).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val numBristles = 6
        val spread = stroke.size * 0.5f

        for (b in 0 until numBristles) {
            val offset = (b - numBristles / 2f) * (spread / numBristles)
            val bristlePath = Path()
            stroke.points.forEachIndexed { i, p ->
                val jitterX = ((p.timestamp + b * 13) % 7 - 3) / 2f
                val jitterY = (((p.timestamp / 7) + b * 17) % 7 - 3) / 2f
                val x = p.x + offset + jitterX
                val y = p.y + offset + jitterY
                if (i == 0) bristlePath.moveTo(x, y) else bristlePath.lineTo(x, y)
            }
            paint.strokeWidth = (stroke.size * 0.18f).coerceAtLeast(1.2f)
            canvas.drawPath(bristlePath, paint)
        }
    }

    private fun drawPaletteKnifeStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 240).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        val halfW = stroke.size * 0.7f
        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            val dx = p1.x - p0.x
            val dy = p1.y - p0.y
            val len = kotlin.math.hypot(dx, dy).coerceAtLeast(0.001f)
            val nx = -dy / len * halfW
            val ny = dx / len * halfW

            val quad = Path().apply {
                moveTo(p0.x - nx, p0.y - ny)
                lineTo(p0.x + nx, p0.y + ny)
                lineTo(p1.x + nx, p1.y + ny)
                lineTo(p1.x - nx, p1.y - ny)
                close()
            }
            canvas.drawPath(quad, paint)
        }
    }

    private fun drawWetSpongeStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
        }
        val step = (stroke.size * 0.4f).coerceAtLeast(10f)
        var accumulated = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            accumulated += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (accumulated >= step || i == 1) {
                accumulated = 0f
                // Stamped porous cluster
                val seed = p1.timestamp.toInt()
                val rng = Random(seed)
                for (dot in 0 until 14) {
                    val angle = rng.nextFloat() * 2f * PI.toFloat()
                    val dist = rng.nextFloat() * (stroke.size * 0.6f)
                    val cx = p1.x + cos(angle) * dist
                    val cy = p1.y + sin(angle) * dist
                    val r = (rng.nextFloat() * stroke.size * 0.16f).coerceAtLeast(1.5f)
                    paint.alpha = (stroke.opacity * (100 + rng.nextInt(120))).toInt().coerceIn(0, 255)
                    canvas.drawCircle(cx, cy, r, paint)
                }
            }
        }
    }

    private fun drawWatercolorStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val path = createSmoothPath(stroke.points)

        // Soft outer pool
        val poolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 65).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.25f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.35f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, poolPaint)

        // Dark pigment edge
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 135).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 0.7f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, edgePaint)
    }

    private fun drawWatercolorBleedStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val path = createSmoothPath(stroke.points)
        val bleedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 55).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.6f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.6f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, bleedPaint)
    }

    private fun drawGouacheStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 250).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawSumiEStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val path = createSmoothPath(stroke.points)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 190).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(1.5f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, paint)
    }

    // --- Splatters, Textures & Grunge (Brusheezy Collection) ---

    private fun drawPaintSplatterStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
        }
        val step = (stroke.size * 0.55f).coerceAtLeast(16f)
        var acc = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            acc += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (acc >= step || i == 1) {
                acc = 0f
                val rng = Random(p1.timestamp.toInt() + i * 31)

                // Main central splatter blob
                val mainR = (stroke.size * (0.25f + rng.nextFloat() * 0.35f)).coerceAtLeast(3f)
                paint.alpha = (stroke.opacity * 240).toInt().coerceIn(0, 255)
                canvas.drawCircle(p1.x, p1.y, mainR, paint)

                // Satellite splatter droplets flung outward
                val drops = 6 + rng.nextInt(10)
                for (d in 0 until drops) {
                    val angle = rng.nextFloat() * 2f * PI.toFloat()
                    val dist = rng.nextFloat() * (stroke.size * 1.8f)
                    val dropX = p1.x + cos(angle) * dist
                    val dropY = p1.y + sin(angle) * dist
                    val dropR = (rng.nextFloat() * stroke.size * 0.12f).coerceAtLeast(1f)
                    paint.alpha = (stroke.opacity * (120 + rng.nextInt(130))).toInt().coerceIn(0, 255)
                    canvas.drawCircle(dropX, dropY, dropR, paint)
                }
            }
        }
    }

    private fun drawInkDripStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
        }
        // Draw base ink line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 230).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 0.45f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), linePaint)

        // Downward vertical drips
        stroke.points.forEachIndexed { idx, p ->
            if (idx % 6 == 0) {
                val rng = Random(p.timestamp.toInt() + idx * 7)
                val dripLen = (rng.nextFloat() * stroke.size * 2.2f).coerceAtLeast(10f)
                paint.alpha = (stroke.opacity * 220).toInt().coerceIn(0, 255)

                // Drip path (tear drop)
                val dripPath = Path().apply {
                    moveTo(p.x, p.y)
                    lineTo(p.x - 2f, p.y + dripLen * 0.7f)
                    quadTo(p.x, p.y + dripLen, p.x + 2f, p.y + dripLen * 0.7f)
                    close()
                }
                canvas.drawPath(dripPath, paint)
                canvas.drawCircle(p.x, p.y + dripLen, 3f, paint)
            }
        }
    }

    private fun drawSprayPaintStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
        }
        val step = (stroke.size * 0.35f).coerceAtLeast(8f)
        var acc = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            acc += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (acc >= step || i == 1) {
                acc = 0f
                val rng = Random(p1.timestamp.toInt() + i * 29)
                val particles = 30 + rng.nextInt(35)
                val radius = stroke.size * 1.1f

                for (p in 0 until particles) {
                    val angle = rng.nextFloat() * 2f * PI.toFloat()
                    val dist = (rng.nextFloat() * rng.nextFloat()) * radius
                    val px = p1.x + cos(angle) * dist
                    val py = p1.y + sin(angle) * dist
                    val r = (rng.nextFloat() * 1.8f).coerceAtLeast(0.8f)
                    paint.alpha = (stroke.opacity * (60 + rng.nextInt(140))).toInt().coerceIn(0, 255)
                    canvas.drawCircle(px, py, r, paint)
                }
            }
        }
    }

    private fun drawGrungeRollerStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
        }
        val halfW = stroke.size * 0.8f
        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            val rng = Random(p1.timestamp.toInt() + i)

            for (seg in 0 until 5) {
                val t = seg / 5f
                val cx = p0.x + (p1.x - p0.x) * t
                val cy = p0.y + (p1.y - p0.y) * t
                val w = halfW * (0.6f + rng.nextFloat() * 0.6f)
                val h = 4f + rng.nextFloat() * 8f
                paint.alpha = (stroke.opacity * (110 + rng.nextInt(120))).toInt().coerceIn(0, 255)
                canvas.drawRect(cx - w, cy - h / 2, cx + w, cy + h / 2, paint)
            }
        }
    }

    private fun drawCharcoalStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 170).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawSoftPastelStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 150).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.1f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.25f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawStippleStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 230).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        val dotRadius = (stroke.size * 0.12f).coerceAtLeast(1.5f)
        val scatter = stroke.size * 0.6f

        stroke.points.forEachIndexed { index, pt ->
            val offsetX = ((pt.timestamp % 17) - 8) / 8f * scatter
            val offsetY = (((pt.timestamp / 17) % 17) - 8) / 8f * scatter
            canvas.drawCircle(pt.x + offsetX, pt.y + offsetY, dotRadius, paint)
        }
    }

    // --- Special FX & Lights ---

    private fun drawAirbrushStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 90).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.5f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.55f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawMarkerStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 135).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.BEVEL
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawNeonStroke(canvas: Canvas, stroke: DrawingStroke) {
        val baseColor = stroke.color.toArgb()
        val path = createSmoothPath(stroke.points)

        // Outer glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            alpha = (stroke.opacity * 170).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 2.5f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.85f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(path, glowPaint)

        // Core white streak
        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 0.45f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawPath(path, corePaint)
    }

    private fun drawBokehStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
        }
        val step = (stroke.size * 0.8f).coerceAtLeast(20f)
        var acc = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            acc += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (acc >= step || i == 1) {
                acc = 0f
                val rng = Random(p1.timestamp.toInt() + i * 23)
                val bokehR = stroke.size * (0.4f + rng.nextFloat() * 0.6f)
                val offX = (rng.nextFloat() - 0.5f) * stroke.size * 0.7f
                val offY = (rng.nextFloat() - 0.5f) * stroke.size * 0.7f

                paint.alpha = (stroke.opacity * (80 + rng.nextInt(90))).toInt().coerceIn(0, 255)
                canvas.drawCircle(p1.x + offX, p1.y + offY, bokehR, paint)
            }
        }
    }

    private fun drawSparklesStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        val step = (stroke.size * 1.8f).coerceAtLeast(15f)
        var acc = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            acc += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (acc >= step || i == 1) {
                acc = 0f
                drawSparkleDiamond(canvas, p1.x, p1.y, stroke.size, paint)
            }
        }
    }

    private fun drawSparkleDiamond(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(cx, cy - radius)
            quadTo(cx, cy, cx + radius, cy)
            quadTo(cx, cy, cx, cy + radius)
            quadTo(cx, cy, cx - radius, cy)
            quadTo(cx, cy, cx, cy - radius)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawStarsStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 255).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        val step = (stroke.size * 2.2f).coerceAtLeast(20f)
        var acc = 0f

        for (i in 1 until stroke.points.size) {
            val p0 = stroke.points[i - 1]
            val p1 = stroke.points[i]
            acc += kotlin.math.hypot(p1.x - p0.x, p1.y - p0.y)

            if (acc >= step || i == 1) {
                acc = 0f
                drawStarShape(canvas, p1.x, p1.y, stroke.size, stroke.size * 0.45f, paint)
            }
        }
    }

    private fun drawStarShape(canvas: Canvas, cx: Float, cy: Float, outerR: Float, innerR: Float, paint: Paint) {
        val path = Path()
        val numPoints = 5
        val angleStep = PI / numPoints
        var angle = -PI / 2.0

        path.moveTo((cx + outerR * cos(angle)).toFloat(), (cy + outerR * sin(angle)).toFloat())

        for (i in 1 until numPoints * 2) {
            angle += angleStep
            val r = if (i % 2 == 0) outerR else innerR
            path.lineTo((cx + r * cos(angle)).toFloat(), (cy + r * sin(angle)).toFloat())
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawCloudMistStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 45).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 2.0f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.75f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawHairFurStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 200).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            strokeCap = Paint.Cap.ROUND
        }
        val offsets = listOf(-6f, -3f, 0f, 3f, 6f)
        offsets.forEach { off ->
            val strand = Path()
            stroke.points.forEachIndexed { i, p ->
                if (i == 0) strand.moveTo(p.x + off, p.y + off) else strand.lineTo(p.x + off, p.y + off)
            }
            canvas.drawPath(strand, paint)
        }
    }

    // --- Utility Brushes ---

    private fun drawSmudgeStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stroke.color.toArgb()
            alpha = (stroke.opacity * 100).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.25f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.45f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawEraserStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }

    private fun drawSoftEraserStroke(canvas: Canvas, stroke: DrawingStroke) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.STROKE
            strokeWidth = stroke.size * 1.3f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(stroke.size * 0.4f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawPath(createSmoothPath(stroke.points), paint)
    }
}
