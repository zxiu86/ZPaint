package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object VideoEncoder {

    suspend fun encodeFramesToMp4(
        frames: List<Bitmap>,
        outputFile: File,
        fps: Int = 12,
        repeatCount: Int = 2, // Repeat animation cycle for smooth video playback
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        if (frames.isEmpty()) return@withContext false

        // Dimensions must be even for H.264 encoding
        val originalWidth = frames[0].width
        val originalHeight = frames[0].height
        val width = (originalWidth / 2) * 2
        val height = (originalHeight / 2) * 2

        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val bitRate = 4_000_000 // 4 Mbps high quality
        val frameRate = fps.coerceIn(1, 60)
        val iFrameInterval = 1 // 1 second between keyframes

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
        }

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        try {
            codec = MediaCodec.createEncoderByType(mimeType)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val frameDurationUs = 1_000_000L / frameRate
            var presentationTimeUs = 0L

            val totalFrames = frames.size * repeatCount
            var frameCounter = 0

            val srcRect = Rect(0, 0, originalWidth, originalHeight)
            val dstRect = Rect(0, 0, width, height)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            for (repeat in 0 until repeatCount) {
                for (frameBitmap in frames) {
                    // Draw frame onto codec input surface
                    val canvas: Canvas = inputSurface.lockCanvas(null)
                    try {
                        canvas.drawBitmap(frameBitmap, srcRect, dstRect, paint)
                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }

                    // Drain encoder output
                    drainEncoder(codec, muxer, bufferInfo, false) { newTrack ->
                        trackIndex = newTrack
                        muxerStarted = true
                    }

                    presentationTimeUs += frameDurationUs
                    frameCounter++
                    onProgress(frameCounter.toFloat() / totalFrames)
                }
            }

            // Signal End of Stream
            codec.signalEndOfInputStream()

            // Drain remaining output until EOS
            drainEncoder(codec, muxer, bufferInfo, true) { newTrack ->
                trackIndex = newTrack
                muxerStarted = true
            }

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        } finally {
            try {
                inputSurface?.release()
                codec?.stop()
                codec?.release()
                muxer?.stop()
                muxer?.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer?,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onFormatChanged: (Int) -> Unit
    ) {
        val timeoutUs = 10_000L
        var trackIndex = -1
        var muxerStarted = false

        while (true) {
            val encoderStatus = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxer != null) {
                    val newFormat = codec.outputFormat
                    trackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                    onFormatChanged(trackIndex)
                }
            } else if (encoderStatus >= 0) {
                val encodedData = codec.getOutputBuffer(encoderStatus)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0 && muxer != null) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(encoderStatus, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }
}
