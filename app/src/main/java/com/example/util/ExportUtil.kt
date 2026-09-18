package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.engine.VideoEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ExportFormat(val extension: String, val mimeType: String, val title: String) {
    PNG("png", "image/png", "PNG (Lossless & Alpha)"),
    JPG("jpg", "image/jpeg", "JPG (High Quality)"),
    WEBP("webp", "image/webp", "WebP (Modern Compact)"),
    MP4("mp4", "video/mp4", "MP4 (Animation Video)")
}

object ExportUtil {

    suspend fun exportImage(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat,
        fileName: String = "ZPaint_${System.currentTimeMillis()}"
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val outputFile = File(exportDir, "$fileName.${format.extension}")

        FileOutputStream(outputFile).use { out ->
            when (format) {
                ExportFormat.PNG -> {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                ExportFormat.JPG -> {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                ExportFormat.WEBP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                    } else {
                        @Suppress("DEPRECATION")
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 95, out)
                    }
                }
                else -> {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }
        outputFile
    }

    suspend fun exportAnimation(
        context: Context,
        frames: List<Bitmap>,
        fps: Int = 12,
        fileName: String = "ZPaint_Anim_${System.currentTimeMillis()}",
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val outputFile = File(exportDir, "$fileName.mp4")

        val success = VideoEncoder.encodeFramesToMp4(
            frames = frames,
            outputFile = outputFile,
            fps = fps,
            repeatCount = 3,
            onProgress = onProgress
        )
        if (success && outputFile.exists() && outputFile.length() > 0) {
            outputFile
        } else {
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, subject: String = "Share Artwork") {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share via ZPaint")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
