package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Data class representing a manhwa/image slice region
 */
data class ImageSliceInfo(
    val index: Int,
    val startY: Int,
    val endY: Int,
    val width: Int,
    val height: Int
) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
}

/**
 * Data class representing an imported image for stitching
 */
data class StitchImageItem(
    val id: String,
    val uri: Uri,
    val fileName: String,
    val width: Int,
    val height: Int,
    val fileSize: Long = 0L
)

/**
 * Natural Alphanumeric Comparator for sorting filenames like:
 * page1.png, page2.png, page10.png -> ordered 1, 2, 10 (not 1, 10, 2)
 */
object NaturalOrderComparator : Comparator<String> {
    private val pattern = Pattern.compile("(\\d+)|(\\D+)")

    override fun compare(s1: String?, s2: String?): Int {
        if (s1 == null || s2 == null) return (s1 ?: "").compareTo(s2 ?: "")

        val matcher1 = pattern.matcher(s1)
        val matcher2 = pattern.matcher(s2)

        while (matcher1.find() && matcher2.find()) {
            val chunk1 = matcher1.group()
            val chunk2 = matcher2.group()

            val isDigit1 = Character.isDigit(chunk1[0])
            val isDigit2 = Character.isDigit(chunk2[0])

            if (isDigit1 && isDigit2) {
                // Compare numeric values
                val num1 = chunk1.toLongOrNull()
                val num2 = chunk2.toLongOrNull()
                if (num1 != null && num2 != null) {
                    val cmp = num1.compareTo(num2)
                    if (cmp != 0) return cmp
                } else {
                    val cmp = chunk1.compareTo(chunk2)
                    if (cmp != 0) return cmp
                }
            } else {
                val cmp = chunk1.compareTo(chunk2, ignoreCase = true)
                if (cmp != 0) return cmp
            }
        }

        return s1.length.compareTo(s2.length)
    }
}

/**
 * High Performance Memory-Safe Long & Giant Image Processor
 * Specifically designed for Manhwa/Webtoon panels (up to 800x10000+ px).
 */
object LongImageProcessor {

    /**
     * Inspect image dimensions and metadata without loading pixels into RAM.
     */
    suspend fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(stream, null, options)
                if (options.outWidth > 0 && options.outHeight > 0) {
                    Pair(options.outWidth, options.outHeight)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get display name of a Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "image_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex) ?: name
                }
            }
        } catch (_: Exception) {}
        return name
    }

    /**
     * Creates a high-performance, downsampled preview bitmap for display in the interactive UI.
     * Keeps memory footprint ultra-low while maintaining crisp aspect ratio.
     */
    suspend fun createPreviewBitmap(
        context: Context,
        uri: Uri,
        maxPreviewDimension: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var inSample = 1
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, boundsOptions)
                val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
                while ((maxDim / inSample) > maxPreviewDimension) {
                    inSample *= 2
                }
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = inSample
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Slices a giant image into multiple regions based on custom horizontal cut lines [cutLinesY].
     * Uses `BitmapRegionDecoder` to decode only the needed regions directly at full lossless resolution!
     */
    suspend fun sliceImage(
        context: Context,
        uri: Uri,
        cutLinesY: List<Int>,
        format: ExportFormat = ExportFormat.PNG,
        onProgress: (current: Int, total: Int, progress: Float) -> Unit = { _, _, _ -> }
    ): List<File> = withContext(Dispatchers.IO) {
        val resultFiles = mutableListOf<File>()
        val outputDir = File(context.cacheDir, "manhwa_slices_${System.currentTimeMillis()}").apply { mkdirs() }

        try {
            // First get original dimensions
            var origWidth = 0
            var origHeight = 0
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)
                origWidth = options.outWidth
                origHeight = options.outHeight
            }

            if (origWidth <= 0 || origHeight <= 0) return@withContext emptyList()

            // Prepare slice boundary intervals
            val sortedCuts = cutLinesY
                .filter { it in 1 until origHeight }
                .distinct()
                .sorted()

            val intervals = mutableListOf<Pair<Int, Int>>()
            var prevY = 0
            for (cutY in sortedCuts) {
                if (cutY > prevY) {
                    intervals.add(Pair(prevY, cutY))
                    prevY = cutY
                }
            }
            if (prevY < origHeight) {
                intervals.add(Pair(prevY, origHeight))
            }

            val totalSlices = intervals.size

            context.contentResolver.openInputStream(uri)?.use { stream ->
                @Suppress("DEPRECATION")
                val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.graphics.BitmapRegionDecoder.newInstance(stream)
                } else {
                    android.graphics.BitmapRegionDecoder.newInstance(stream, false)
                }

                if (decoder != null) {
                    val decodeOptions = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                    for ((idx, interval) in intervals.withIndex()) {
                        val top = interval.first
                        val bottom = interval.second
                        val rect = Rect(0, top, origWidth, bottom)

                        val regionBitmap = decoder.decodeRegion(rect, decodeOptions)
                        if (regionBitmap != null) {
                            val sliceNum = String.format("%03d", idx + 1)
                            val file = File(outputDir, "slice_${sliceNum}.${format.extension}")
                            FileOutputStream(file).use { out ->
                                when (format) {
                                    ExportFormat.PNG -> regionBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    ExportFormat.JPG -> regionBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                    ExportFormat.WEBP -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            regionBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            regionBitmap.compress(Bitmap.CompressFormat.WEBP, 95, out)
                                        }
                                    }
                                    else -> regionBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                }
                            }
                            regionBitmap.recycle()
                            resultFiles.add(file)
                        }

                        val prog = (idx + 1).toFloat() / totalSlices.toFloat()
                        onProgress(idx + 1, totalSlices, prog)
                    }

                    decoder.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        resultFiles
    }

    /**
     * Stitches multiple images vertically into a single long continuous image or multi-page output.
     * Features:
     * - Automatic width normalization (all panels scaled smoothly to match target width).
     * - Zero-loss or high-fidelity output.
     * - Progress reporting.
     */
    suspend fun stitchImages(
        context: Context,
        items: List<StitchImageItem>,
        targetWidth: Int = 0,
        gapPx: Int = 0,
        format: ExportFormat = ExportFormat.PNG,
        onProgress: (current: Int, total: Int, progress: Float) -> Unit = { _, _, _ -> }
    ): File? = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext null

        try {
            // Determine unified width
            val normalizedWidth = if (targetWidth > 0) targetWidth else items.maxOf { it.width }

            // Calculate scaled heights
            val scaledHeights = items.map { item ->
                if (item.width > 0) {
                    val scale = normalizedWidth.toFloat() / item.width.toFloat()
                    (item.height * scale).roundToInt()
                } else 100
            }

            val totalHeight = scaledHeights.sum() + (items.size - 1) * gapPx
            val exportDir = File(context.cacheDir, "stitched").apply { mkdirs() }
            val outputFile = File(exportDir, "stitched_manhwa_${System.currentTimeMillis()}.${format.extension}")

            // Allocate unified canvas bitmap
            val masterBitmap = Bitmap.createBitmap(normalizedWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(masterBitmap)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

            var currentY = 0f

            for ((idx, item) in items.withIndex()) {
                val targetH = scaledHeights[idx]

                // Load source bitmap
                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    val srcBmp = BitmapFactory.decodeStream(stream)
                    if (srcBmp != null) {
                        val srcRect = Rect(0, 0, srcBmp.width, srcBmp.height)
                        val dstRect = Rect(0, currentY.toInt(), normalizedWidth, (currentY + targetH).toInt())
                        canvas.drawBitmap(srcBmp, srcRect, dstRect, paint)
                        srcBmp.recycle()
                    }
                }

                currentY += targetH + gapPx
                val prog = (idx + 1).toFloat() / items.size.toFloat()
                onProgress(idx + 1, items.size, prog)
            }

            // Save master bitmap to output file
            FileOutputStream(outputFile).use { out ->
                when (format) {
                    ExportFormat.PNG -> masterBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    ExportFormat.JPG -> masterBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    ExportFormat.WEBP -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            masterBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                        } else {
                            @Suppress("DEPRECATION")
                            masterBitmap.compress(Bitmap.CompressFormat.WEBP, 95, out)
                        }
                    }
                    else -> masterBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }

            masterBitmap.recycle()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports a list of images or slices into a multi-page PDF document.
     */
    suspend fun exportToPdf(
        context: Context,
        items: List<StitchImageItem>,
        fileName: String = "manhwa_chapter_${System.currentTimeMillis()}"
    ): File? = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext null

        try {
            val pdfDocument = PdfDocument()
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outputFile = File(exportDir, "$fileName.pdf")

            for ((idx, item) in items.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(item.width, item.height, idx + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        page.canvas.drawBitmap(bmp, 0f, 0f, null)
                        bmp.recycle()
                    }
                }

                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Bundles a list of files into a single ZIP archive.
     */
    suspend fun createZipArchive(
        context: Context,
        files: List<File>,
        zipName: String = "manhwa_slices_${System.currentTimeMillis()}"
    ): File? = withContext(Dispatchers.IO) {
        if (files.isEmpty()) return@withContext null

        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val zipFile = File(exportDir, "$zipName.zip")

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val buffer = ByteArray(8192)
                for (file in files) {
                    if (file.exists()) {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        FileInputStream(file).use { fis ->
                            var count: Int
                            while (fis.read(buffer).also { count = it } != -1) {
                                zos.write(buffer, 0, count)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a list of sliced files directly into Android MediaStore Pictures album.
     */
    suspend fun saveSlicesToGallery(
        context: Context,
        files: List<File>,
        subDirectory: String = "ZPaint_Manhwa"
    ): Int = withContext(Dispatchers.IO) {
        var savedCount = 0
        val resolver = context.contentResolver

        for (file in files) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                    put(MediaStore.Images.Media.MIME_TYPE, if (file.name.endsWith(".png")) "image/png" else "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$subDirectory")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(file).use { input ->
                            input.copyTo(out)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                    savedCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        savedCount
    }
}
