package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.ExportFormat
import com.example.util.ImageSliceInfo
import com.example.util.LongImageProcessor
import com.example.util.NaturalOrderComparator
import com.example.util.StitchImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class ManhwaStudioTab {
    SLICER,    // قص وتقطيع الصور الطويلة
    STITCHER   // تجميع ودمج الصور عمودياً
}

class ManhwaStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _selectedTab = MutableStateFlow(ManhwaStudioTab.SLICER)
    val selectedTab: StateFlow<ManhwaStudioTab> = _selectedTab.asStateFlow()

    // -------------------------------------------------------------
    // SLICER STATE
    // -------------------------------------------------------------
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _imageFileName = MutableStateFlow("")
    val imageFileName: StateFlow<String> = _imageFileName.asStateFlow()

    private val _originalWidth = MutableStateFlow(0)
    val originalWidth: StateFlow<Int> = _originalWidth.asStateFlow()

    private val _originalHeight = MutableStateFlow(0)
    val originalHeight: StateFlow<Int> = _originalHeight.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isLoadingImage = MutableStateFlow(false)
    val isLoadingImage: StateFlow<Boolean> = _isLoadingImage.asStateFlow()

    // Custom horizontal cut lines (in original image pixels Y coordinates)
    private val _cutLinesY = MutableStateFlow<List<Int>>(emptyList())
    val cutLinesY: StateFlow<List<Int>> = _cutLinesY.asStateFlow()

    // Computed slice regions
    private val _slicesList = MutableStateFlow<List<ImageSliceInfo>>(emptyList())
    val slicesList: StateFlow<List<ImageSliceInfo>> = _slicesList.asStateFlow()

    // Slicer Export State
    private val _isExportingSlices = MutableStateFlow(false)
    val isExportingSlices: StateFlow<Boolean> = _isExportingSlices.asStateFlow()

    private val _sliceExportProgress = MutableStateFlow(0f)
    val sliceExportProgress: StateFlow<Float> = _sliceExportProgress.asStateFlow()

    private val _sliceExportStatusText = MutableStateFlow("")
    val sliceExportStatusText: StateFlow<String> = _sliceExportStatusText.asStateFlow()

    private val _exportedSliceFiles = MutableStateFlow<List<File>>(emptyList())
    val exportedSliceFiles: StateFlow<List<File>> = _exportedSliceFiles.asStateFlow()

    private val _exportedZipFile = MutableStateFlow<File?>(null)
    val exportedZipFile: StateFlow<File?> = _exportedZipFile.asStateFlow()

    // -------------------------------------------------------------
    // STITCHER STATE
    // -------------------------------------------------------------
    private val _stitchItems = MutableStateFlow<List<StitchImageItem>>(emptyList())
    val stitchItems: StateFlow<List<StitchImageItem>> = _stitchItems.asStateFlow()

    private val _gapPx = MutableStateFlow(0)
    val gapPx: StateFlow<Int> = _gapPx.asStateFlow()

    private val _showSeams = MutableStateFlow(true)
    val showSeams: StateFlow<Boolean> = _showSeams.asStateFlow()

    private val _isStitching = MutableStateFlow(false)
    val isStitching: StateFlow<Boolean> = _isStitching.asStateFlow()

    private val _stitchProgress = MutableStateFlow(0f)
    val stitchProgress: StateFlow<Float> = _stitchProgress.asStateFlow()

    private val _stitchedResultFile = MutableStateFlow<File?>(null)
    val stitchedResultFile: StateFlow<File?> = _stitchedResultFile.asStateFlow()

    private val _pdfResultFile = MutableStateFlow<File?>(null)
    val pdfResultFile: StateFlow<File?> = _pdfResultFile.asStateFlow()

    fun selectTab(tab: ManhwaStudioTab) {
        _selectedTab.value = tab
    }

    // -------------------------------------------------------------
    // SLICER METHODS
    // -------------------------------------------------------------

    fun loadSourceImage(uri: Uri) {
        viewModelScope.launch {
            _isLoadingImage.value = true
            _selectedImageUri.value = uri
            _imageFileName.value = LongImageProcessor.getFileName(context, uri)
            _exportedSliceFiles.value = emptyList()
            _exportedZipFile.value = null

            val dims = LongImageProcessor.getImageDimensions(context, uri)
            if (dims != null) {
                _originalWidth.value = dims.first
                _originalHeight.value = dims.second

                // Generate downsampled preview for smooth display
                val preview = LongImageProcessor.createPreviewBitmap(context, uri, maxPreviewDimension = 2400)
                _previewBitmap.value = preview

                // Default: 3 balanced slices or auto-split if very tall
                val defaultCuts = mutableListOf<Int>()
                val h = dims.second
                if (h > 3000) {
                    // split every ~2000px
                    var y = 2000
                    while (y < h - 500) {
                        defaultCuts.add(y)
                        y += 2000
                    }
                } else if (h > 1200) {
                    defaultCuts.add(h / 2)
                }
                _cutLinesY.value = defaultCuts
                recalculateSlices()
            }
            _isLoadingImage.value = false
        }
    }

    fun addCutLine(yCoord: Int) {
        val maxH = _originalHeight.value
        if (maxH <= 0) return
        val clampedY = yCoord.coerceIn(50, maxH - 50)
        val current = _cutLinesY.value.toMutableList()
        if (!current.contains(clampedY)) {
            current.add(clampedY)
            current.sort()
            _cutLinesY.value = current
            recalculateSlices()
        }
    }

    fun updateCutLine(index: Int, newY: Int) {
        val maxH = _originalHeight.value
        if (maxH <= 0 || index !in _cutLinesY.value.indices) return
        val clampedY = newY.coerceIn(50, maxH - 50)
        val current = _cutLinesY.value.toMutableList()
        current[index] = clampedY
        current.sort()
        _cutLinesY.value = current
        recalculateSlices()
    }

    fun removeCutLine(cutY: Int) {
        val current = _cutLinesY.value.toMutableList()
        current.remove(cutY)
        _cutLinesY.value = current
        recalculateSlices()
    }

    fun clearAllCuts() {
        _cutLinesY.value = emptyList()
        recalculateSlices()
    }

    fun autoSplitByCount(partsCount: Int) {
        val maxH = _originalHeight.value
        if (maxH <= 0 || partsCount < 2) return
        val step = maxH / partsCount
        val cuts = mutableListOf<Int>()
        for (i in 1 until partsCount) {
            cuts.add(i * step)
        }
        _cutLinesY.value = cuts
        recalculateSlices()
    }

    fun autoSplitByMaxHeight(maxSliceHeight: Int) {
        val maxH = _originalHeight.value
        if (maxH <= 0 || maxSliceHeight <= 100) return
        val cuts = mutableListOf<Int>()
        var y = maxSliceHeight
        while (y < maxH - 100) {
            cuts.add(y)
            y += maxSliceHeight
        }
        _cutLinesY.value = cuts
        recalculateSlices()
    }

    private fun recalculateSlices() {
        val origW = _originalWidth.value
        val origH = _originalHeight.value
        if (origW <= 0 || origH <= 0) {
            _slicesList.value = emptyList()
            return
        }

        val cuts = _cutLinesY.value.sorted()
        val list = mutableListOf<ImageSliceInfo>()
        var prevY = 0

        for ((idx, cutY) in cuts.withIndex()) {
            if (cutY > prevY) {
                list.add(
                    ImageSliceInfo(
                        index = idx + 1,
                        startY = prevY,
                        endY = cutY,
                        width = origW,
                        height = cutY - prevY
                    )
                )
                prevY = cutY
            }
        }

        if (prevY < origH) {
            list.add(
                ImageSliceInfo(
                    index = list.size + 1,
                    startY = prevY,
                    endY = origH,
                    width = origW,
                    height = origH - prevY
                )
            )
        }

        _slicesList.value = list
    }

    fun exportSlices(
        format: ExportFormat = ExportFormat.PNG,
        saveToGallery: Boolean = true,
        createZip: Boolean = true,
        onComplete: (files: List<File>, zip: File?, savedCount: Int) -> Unit = { _, _, _ -> }
    ) {
        val uri = _selectedImageUri.value ?: return
        val cuts = _cutLinesY.value

        viewModelScope.launch {
            _isExportingSlices.value = true
            _sliceExportProgress.value = 0f
            _sliceExportStatusText.value = "جارٍ قص الأجزاء بدقة كاملة وبدون فقد..."

            val files = LongImageProcessor.sliceImage(
                context = context,
                uri = uri,
                cutLinesY = cuts,
                format = format,
                onProgress = { current, total, progress ->
                    _sliceExportProgress.value = progress
                    _sliceExportStatusText.value = "جارٍ معالجة الجزء $current من $total (${(progress * 100).toInt()}%)"
                }
            )

            _exportedSliceFiles.value = files

            var savedCount = 0
            if (saveToGallery && files.isNotEmpty()) {
                _sliceExportStatusText.value = "جارٍ حفظ القصاصات في المعرض (ZPaint_Manhwa)..."
                savedCount = LongImageProcessor.saveSlicesToGallery(context, files)
            }

            var zip: File? = null
            if (createZip && files.isNotEmpty()) {
                _sliceExportStatusText.value = "جارٍ إنشاء أرشيف مضغوط ZIP..."
                zip = LongImageProcessor.createZipArchive(context, files, "manhwa_slices_${System.currentTimeMillis()}")
                _exportedZipFile.value = zip
            }

            _sliceExportStatusText.value = "اكتمل القص بنجاح! تم إنشاء ${files.size} قصاصة."
            _isExportingSlices.value = false
            onComplete(files, zip, savedCount)
        }
    }

    // -------------------------------------------------------------
    // STITCHER METHODS
    // -------------------------------------------------------------

    fun addStitchImages(uris: List<Uri>) {
        viewModelScope.launch {
            val newItems: List<StitchImageItem> = withContext(Dispatchers.IO) {
                coroutineScope {
                    uris.map { uri ->
                        async {
                            val dims = LongImageProcessor.getImageDimensions(context, uri)
                            val fileName = LongImageProcessor.getFileName(context, uri)
                            StitchImageItem(
                                id = UUID.randomUUID().toString(),
                                uri = uri,
                                fileName = fileName,
                                width = dims?.first ?: 800,
                                height = dims?.second ?: 1200
                            )
                        }
                    }.awaitAll()
                }
            }

            val combined = _stitchItems.value + newItems
            // Auto sort initially if empty
            if (_stitchItems.value.isEmpty()) {
                _stitchItems.value = combined.sortedWith { a, b ->
                    NaturalOrderComparator.compare(a.fileName, b.fileName)
                }
            } else {
                _stitchItems.value = combined
            }
        }
    }

    fun sortByFileNameNaturally() {
        _stitchItems.value = _stitchItems.value.sortedWith { a, b ->
            NaturalOrderComparator.compare(a.fileName, b.fileName)
        }
    }

    fun moveStitchItemUp(index: Int) {
        if (index <= 0 || index >= _stitchItems.value.size) return
        val list = _stitchItems.value.toMutableList()
        val temp = list[index]
        list[index] = list[index - 1]
        list[index - 1] = temp
        _stitchItems.value = list
    }

    fun moveStitchItemDown(index: Int) {
        if (index < 0 || index >= _stitchItems.value.size - 1) return
        val list = _stitchItems.value.toMutableList()
        val temp = list[index]
        list[index] = list[index + 1]
        list[index + 1] = temp
        _stitchItems.value = list
    }

    fun removeStitchItem(id: String) {
        _stitchItems.value = _stitchItems.value.filter { it.id != id }
    }

    fun clearAllStitchItems() {
        _stitchItems.value = emptyList()
        _stitchedResultFile.value = null
        _pdfResultFile.value = null
    }

    fun setGapPx(gap: Int) {
        _gapPx.value = gap.coerceIn(0, 100)
    }

    fun toggleShowSeams() {
        _showSeams.value = !_showSeams.value
    }

    fun stitchAndExport(
        format: ExportFormat = ExportFormat.PNG,
        onComplete: (file: File?) -> Unit = {}
    ) {
        val items = _stitchItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            _isStitching.value = true
            _stitchProgress.value = 0f

            val result = LongImageProcessor.stitchImages(
                context = context,
                items = items,
                gapPx = _gapPx.value,
                format = format,
                onProgress = { current, total, prog ->
                    _stitchProgress.value = prog
                }
            )

            _stitchedResultFile.value = result
            _isStitching.value = false
            onComplete(result)
        }
    }

    fun exportToPdf(onComplete: (file: File?) -> Unit = {}) {
        val items = _stitchItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            _isStitching.value = true
            _stitchProgress.value = 0.5f

            val result = LongImageProcessor.exportToPdf(
                context = context,
                items = items
            )

            _pdfResultFile.value = result
            _isStitching.value = false
            onComplete(result)
        }
    }
}
