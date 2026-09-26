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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import java.util.zip.Deflater
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
 * Ultra-Fast High Performance Memory-Safe Long & Giant Image Processor
 * Specifically designed for Manhwa/Webtoon panels (up to 800x10000+ px).
 * Features multi-core parallel processing, buffered streams, and Skia-accelerated region decoding.
 */
object LongImageProcessor {

    private const val BUFFER_SIZE = 65536 // 64 KB high-speed I/O buffer

    /**
     * Inspect image dimensions and metadata instantly without loading pixel data.
     */
    suspend fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedInputStream(stream, BUFFER_SIZE).use { bufferedStream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(bufferedStream, null, options)
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        Pair(options.outWidth, options.outHeight)
                    } else null
                }
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
     * Creates an ultra-fast, memory-safe downsampled preview bitmap for the interactive UI.
     * Uses inSampleSize and RGB_565 to keep memory footprint minimal and rendering instantaneous.
     */
    suspend fun createPreviewBitmap(
        context: Context,
        uri: Uri,
        maxPreviewDimension: Int = 2400
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var inSample = 1
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedInputStream(stream, BUFFER_SIZE).use { bufferedStream ->
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(bufferedStream, null, boundsOptions)
                    val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
                    while ((maxDim / inSample) > maxPreviewDimension) {
                        inSample *= 2
                    }
                }
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedInputStream(stream, BUFFER_SIZE).use { bufferedStream ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = inSample
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeStream(bufferedStream, null, decodeOptions)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * High-speed parallel slicer for giant images.
     * Uses a local file cache for direct native Skia decoding + multi-core parallel compression.
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
        var tempSourceFile: File? = null

        try {
            // Step 1: Copy to local temp file to allow instant, direct Skia file-based region decoding.
            val localSourceFile: File = if (uri.scheme == "file" && uri.path != null) {
                File(uri.path!!)
            } else {
                val temp = File(context.cacheDir, "slice_source_${System.currentTimeMillis()}.tmp")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BufferedInputStream(input, BUFFER_SIZE).use { bin ->
                        BufferedOutputStream(FileOutputStream(temp), BUFFER_SIZE).use { bout ->
                            bin.copyTo(bout, BUFFER_SIZE)
                        }
                    }
                }
                tempSourceFile = temp
                temp
            }

            if (!localSourceFile.exists() || localSourceFile.length() == 0L) {
                return@withContext emptyList()
            }

            // Step 2: Read dimensions directly from the fast local file
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(localSourceFile.absolutePath, boundsOptions)
            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight

            if (origWidth <= 0 || origHeight <= 0) return@withContext emptyList()

            // Step 3: Compute intervals from cut points
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
            if (totalSlices == 0) return@withContext emptyList()

            // Step 4: Initialize native BitmapRegionDecoder
            @Suppress("DEPRECATION")
            val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.graphics.BitmapRegionDecoder.newInstance(localSourceFile.absolutePath)
            } else {
                android.graphics.BitmapRegionDecoder.newInstance(localSourceFile.absolutePath, false)
            }

            if (decoder != null) {
                val decodeOptions = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                // Step 5: Multi-core pipelined processing
                // Decode region sequentially (safe for decoder), but compress & write to disk in parallel across CPU cores!
                val completedCount = AtomicInteger(0)
                val maxParallelism = Runtime.getRuntime().availableProcessors().coerceIn(2, 6)
                val semaphore = Semaphore(maxParallelism)
                val compressionJobs = mutableListOf<kotlinx.coroutines.Deferred<File?>>()

                for ((idx, interval) in intervals.withIndex()) {
                    val top = interval.first
                    val bottom = interval.second
                    val rect = Rect(0, top, origWidth, bottom)

                    // Synchronously decode the region bitmap (fast)
                    val regionBitmap = synchronized(decoder) {
                        decoder.decodeRegion(rect, decodeOptions)
                    }

                    if (regionBitmap != null) {
                        val sliceNum = String.format("%03d", idx + 1)
                        val targetFile = File(outputDir, "slice_${sliceNum}.${format.extension}")

                        // Launch parallel compression on CPU thread pool
                        val job = async(Dispatchers.Default) {
                            semaphore.withPermit {
                                try {
                                    BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { out ->
                                        when (format) {
                                            ExportFormat.PNG -> regionBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                            ExportFormat.JPG -> regionBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                            ExportFormat.WEBP -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    regionBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    regionBitmap.compress(Bitmap.CompressFormat.WEBP, 92, out)
                                                }
                                            }
                                            else -> regionBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                    }
                                    targetFile
                                } finally {
                                    regionBitmap.recycle()
                                    val done = completedCount.incrementAndGet()
                                    withContext(Dispatchers.Main) {
                                        onProgress(done, totalSlices, done.toFloat() / totalSlices.toFloat())
                                    }
                                }
                            }
                        }
                        compressionJobs.add(job)
                    }
                }

                // Await all parallel compression tasks
                val results = compressionJobs.awaitAll()
                for (file in results) {
                    if (file != null && file.exists()) {
                        resultFiles.add(file)
                    }
                }

                decoder.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Clean up temporary source file
            tempSourceFile?.delete()
        }

        resultFiles.sortedBy { it.name }
    }

    /**
     * Ultra-fast image stitcher and combiner.
     * Stitches multiple images vertically with:
     * - Automatic width normalization (smart inSampleSize scaling for speed and low RAM).
     * - Direct Canvas tile rendering.
     * - 64KB high-speed buffered output streaming.
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

            // Allocate master canvas bitmap
            val masterBitmap = Bitmap.createBitmap(normalizedWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(masterBitmap)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

            var currentY = 0f

            for ((idx, item) in items.withIndex()) {
                val targetH = scaledHeights[idx]

                // Optimized bitmap decoding with inSampleSize if the source image is oversized
                context.contentResolver.openInputStream(item.uri)?.use { stream ->
                    BufferedInputStream(stream, BUFFER_SIZE).use { bin ->
                        var inSample = 1
                        if (item.width > normalizedWidth * 2) {
                            inSample = (item.width / normalizedWidth).coerceAtLeast(1)
                        }

                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = inSample
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }

                        val srcBmp = BitmapFactory.decodeStream(bin, null, decodeOptions)
                        if (srcBmp != null) {
                            val srcRect = Rect(0, 0, srcBmp.width, srcBmp.height)
                            val dstRect = Rect(0, currentY.toInt(), normalizedWidth, (currentY + targetH).toInt())
                            canvas.drawBitmap(srcBmp, srcRect, dstRect, paint)
                            srcBmp.recycle()
                        }
                    }
                }

                currentY += targetH + gapPx
                val prog = (idx + 1).toFloat() / items.size.toFloat()
                onProgress(idx + 1, items.size, prog)
            }

            // Save master bitmap to output file using high-speed 64KB buffer
            BufferedOutputStream(FileOutputStream(outputFile), BUFFER_SIZE).use { out ->
                when (format) {
                    ExportFormat.PNG -> masterBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    ExportFormat.JPG -> masterBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    ExportFormat.WEBP -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            masterBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                        } else {
                            @Suppress("DEPRECATION")
                            masterBitmap.compress(Bitmap.CompressFormat.WEBP, 92, out)
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
     * Fast PDF exporter using buffered writes.
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
                    BufferedInputStream(stream, BUFFER_SIZE).use { bin ->
                        val bmp = BitmapFactory.decodeStream(bin)
                        if (bmp != null) {
                            page.canvas.drawBitmap(bmp, 0f, 0f, null)
                            bmp.recycle()
                        }
                    }
                }

                pdfDocument.finishPage(page)
            }

            BufferedOutputStream(FileOutputStream(outputFile), BUFFER_SIZE).use { out ->
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
     * High-speed ZIP archiver with BEST_SPEED deflater and 64KB buffer.
     * Completes in a fraction of a second.
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

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile), BUFFER_SIZE)).use { zos ->
                // Image slices are already compressed (PNG/JPG); use BEST_SPEED to avoid redundant CPU work!
                zos.setLevel(Deflater.BEST_SPEED)
                val buffer = ByteArray(BUFFER_SIZE)

                for (file in files) {
                    if (file.exists()) {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        BufferedInputStream(FileInputStream(file), BUFFER_SIZE).use { bis ->
                            var count: Int
                            while (bis.read(buffer).also { count = it } != -1) {
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
     * Fast batch saving to Android MediaStore Pictures album using 64KB buffered stream copies.
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
                        BufferedOutputStream(out, BUFFER_SIZE).use { bout ->
                            BufferedInputStream(FileInputStream(file), BUFFER_SIZE).use { bin ->
                                bin.copyTo(bout, BUFFER_SIZE)
                            }
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
