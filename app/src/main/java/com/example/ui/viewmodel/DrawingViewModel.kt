package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.CloudSyncState
import com.example.engine.CanvasRenderer
import androidx.compose.ui.graphics.toArgb
import com.example.model.ActiveSlidingSheet
import com.example.model.AnimationFrame
import com.example.model.BrushConfig
import com.example.model.BrushType
import com.example.model.CanvasPaper
import com.example.model.DrawingLayer
import com.example.model.DrawingStroke
import com.example.model.Project
import com.example.model.SymmetryMode
import com.example.model.TouchPoint
import com.example.util.ExportFormat
import com.example.util.ExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DrawingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    val syncManager = CloudSyncManager(application, repository, viewModelScope)

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<CloudSyncState> = syncManager.syncState

    private val _currentProject = MutableStateFlow(Project())
    val currentProject: StateFlow<Project> = _currentProject.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _activeLayerId = MutableStateFlow("")
    val activeLayerId: StateFlow<String> = _activeLayerId.asStateFlow()

    private val _brushConfig = MutableStateFlow(
        BrushConfig(
            type = BrushType.PEN,
            size = 12f,
            opacity = 1.0f,
            color = Color.White
        )
    )
    val brushConfig: StateFlow<BrushConfig> = _brushConfig.asStateFlow()

    // Undo / Redo stacks: stores snapshot of active frame layers
    private val undoStack = mutableListOf<List<DrawingLayer>>()
    private val redoStack = mutableListOf<List<DrawingLayer>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Animation playback state
    private val _isPlayingAnimation = MutableStateFlow(false)
    val isPlayingAnimation: StateFlow<Boolean> = _isPlayingAnimation.asStateFlow()

    private val _onionSkinEnabled = MutableStateFlow(false)
    val onionSkinEnabled: StateFlow<Boolean> = _onionSkinEnabled.asStateFlow()

    // Active in-progress stroke (rendered instantly without recomposing baked layers)
    private val _activeStroke = MutableStateFlow<DrawingStroke?>(null)
    val activeStroke: StateFlow<DrawingStroke?> = _activeStroke.asStateFlow()

    // Export progress and exported file
    private val _exportProgress = MutableStateFlow<Float?>(null)
    val exportProgress: StateFlow<Float?> = _exportProgress.asStateFlow()

    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    // Studio Guides & Canvas States
    private val _canvasPaper = MutableStateFlow(CanvasPaper.WHITE)
    val canvasPaper: StateFlow<CanvasPaper> = _canvasPaper.asStateFlow()

    private val _showGrid = MutableStateFlow(false)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private val _gridSize = MutableStateFlow(40f)
    val gridSize: StateFlow<Float> = _gridSize.asStateFlow()

    private val _symmetryMode = MutableStateFlow(SymmetryMode.NONE)
    val symmetryMode: StateFlow<SymmetryMode> = _symmetryMode.asStateFlow()

    private val _flipHorizontal = MutableStateFlow(false)
    val flipHorizontal: StateFlow<Boolean> = _flipHorizontal.asStateFlow()

    private val _flipVertical = MutableStateFlow(false)
    val flipVertical: StateFlow<Boolean> = _flipVertical.asStateFlow()

    private val _recentColors = MutableStateFlow(
        listOf(
            Color(0xFF101014),
            Color.White,
            Color(0xFFE53935),
            Color(0xFF1E88E5),
            Color(0xFF43A047),
            Color(0xFFFDD835),
            Color(0xFF8E24AA),
            Color(0xFFFF9800),
            Color(0xFF00BCD4),
            Color(0xFF795548)
        )
    )
    val recentColors: StateFlow<List<Color>> = _recentColors.asStateFlow()

    private val _activeSlidingSheet = MutableStateFlow(ActiveSlidingSheet.NONE)
    val activeSlidingSheet: StateFlow<ActiveSlidingSheet> = _activeSlidingSheet.asStateFlow()

    // Sliced Images Editing States
    private val _slicedEditingFiles = MutableStateFlow<List<File>>(emptyList())
    val slicedEditingFiles: StateFlow<List<File>> = _slicedEditingFiles.asStateFlow()

    private val _activeSliceIndex = MutableStateFlow(0)
    val activeSliceIndex: StateFlow<Int> = _activeSliceIndex.asStateFlow()

    private val _isSlicedEditingMode = MutableStateFlow(false)
    val isSlicedEditingMode: StateFlow<Boolean> = _isSlicedEditingMode.asStateFlow()

    private var playbackJob: Job? = null
    private var autoSaveJob: Job? = null

    init {
        initializeInitialProject()
    }

    private fun initializeInitialProject() {
        viewModelScope.launch {
            val initial = Project(
                title = "Artwork 1",
                width = 1080,
                height = 1080,
                fps = 12,
                frames = listOf(
                    AnimationFrame(
                        layers = listOf(DrawingLayer(name = "Background Layer"))
                    )
                )
            )
            _currentProject.value = initial
            _activeLayerId.value = initial.frames[0].layers[0].id
            // Save initial project
            repository.saveProject(initial)
        }
    }

    // --- Drawing Stroke Handling ---

    fun startStroke(point: TouchPoint) {
        if (_isPlayingAnimation.value) stopPlayback()

        val activeLayer = getActiveLayer()
        if (activeLayer == null || activeLayer.isLocked || !activeLayer.isVisible) return

        // Push state to undo stack before starting new stroke
        pushUndoState()

        val stroke = DrawingStroke(
            id = UUID.randomUUID().toString(),
            points = listOf(point),
            brushType = _brushConfig.value.type,
            color = _brushConfig.value.color,
            size = _brushConfig.value.size,
            opacity = _brushConfig.value.opacity
        )
        _activeStroke.value = stroke
    }

    fun appendPointToStroke(point: TouchPoint) {
        val current = _activeStroke.value ?: return
        _activeStroke.value = current.copy(points = current.points + point)
    }

    fun endStroke() {
        val completedStroke = _activeStroke.value ?: return
        _activeStroke.value = null

        val currentProj = _currentProject.value
        val frameIdx = _currentFrameIndex.value.coerceIn(0, currentProj.frames.size - 1)
        val frame = currentProj.frames[frameIdx]

        val strokesToAdd = mutableListOf(completedStroke)
        val symMode = _symmetryMode.value
        val cw = currentProj.width.toFloat()
        val ch = currentProj.height.toFloat()

        when (symMode) {
            SymmetryMode.VERTICAL -> {
                val mirroredPts = completedStroke.points.map { pt -> pt.copy(x = cw - pt.x) }
                strokesToAdd.add(completedStroke.copy(id = UUID.randomUUID().toString(), points = mirroredPts))
            }
            SymmetryMode.HORIZONTAL -> {
                val mirroredPts = completedStroke.points.map { pt -> pt.copy(y = ch - pt.y) }
                strokesToAdd.add(completedStroke.copy(id = UUID.randomUUID().toString(), points = mirroredPts))
            }
            SymmetryMode.QUAD -> {
                val vPts = completedStroke.points.map { pt -> pt.copy(x = cw - pt.x) }
                val hPts = completedStroke.points.map { pt -> pt.copy(y = ch - pt.y) }
                val qPts = completedStroke.points.map { pt -> pt.copy(x = cw - pt.x, y = ch - pt.y) }
                strokesToAdd.add(completedStroke.copy(id = UUID.randomUUID().toString(), points = vPts))
                strokesToAdd.add(completedStroke.copy(id = UUID.randomUUID().toString(), points = hPts))
                strokesToAdd.add(completedStroke.copy(id = UUID.randomUUID().toString(), points = qPts))
            }
            SymmetryMode.NONE -> {}
        }

        val updatedLayers = frame.layers.map { layer ->
            if (layer.id == _activeLayerId.value) {
                layer.copy(strokes = layer.strokes + strokesToAdd)
            } else {
                layer
            }
        }

        val updatedFrames = currentProj.frames.toMutableList().apply {
            this[frameIdx] = frame.copy(layers = updatedLayers)
        }

        _currentProject.value = currentProj.copy(
            frames = updatedFrames,
            updatedAt = System.currentTimeMillis()
        )

        scheduleAutoSave()
    }

    // --- Undo / Redo ---

    private fun pushUndoState() {
        val currentLayers = getCurrentFrameLayers()
        undoStack.add(currentLayers)
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        updateUndoRedoStates()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentLayers = getCurrentFrameLayers()
        redoStack.add(currentLayers)

        val previousLayers = undoStack.removeAt(undoStack.size - 1)
        applyLayersToCurrentFrame(previousLayers)
        updateUndoRedoStates()
        scheduleAutoSave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentLayers = getCurrentFrameLayers()
        undoStack.add(currentLayers)

        val nextLayers = redoStack.removeAt(redoStack.size - 1)
        applyLayersToCurrentFrame(nextLayers)
        updateUndoRedoStates()
        scheduleAutoSave()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    // --- Brush Configuration ---

    fun setBrushType(type: BrushType) {
        _brushConfig.value = _brushConfig.value.copy(
            type = type,
            size = type.defaultSize.coerceAtLeast(_brushConfig.value.size.coerceIn(2f, 150f)),
            opacity = type.defaultOpacity
        )
    }

    fun setBrushSize(size: Float) {
        _brushConfig.value = _brushConfig.value.copy(size = size.coerceIn(1f, 200f))
    }

    fun setBrushOpacity(opacity: Float) {
        _brushConfig.value = _brushConfig.value.copy(opacity = opacity.coerceIn(0.01f, 1.0f))
    }

    fun setBrushColor(color: Color) {
        _brushConfig.value = _brushConfig.value.copy(color = color)
        addRecentColor(color)
    }

    fun addRecentColor(color: Color) {
        val filtered = _recentColors.value.filter { it != color }.toMutableList()
        filtered.add(0, color)
        _recentColors.value = filtered.take(12)
    }

    fun setActiveSlidingSheet(sheet: ActiveSlidingSheet) {
        _activeSlidingSheet.value = sheet
    }

    fun dismissSlidingSheet() {
        _activeSlidingSheet.value = ActiveSlidingSheet.NONE
    }

    fun setCanvasPaper(paper: CanvasPaper) {
        _canvasPaper.value = paper
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    fun setGridSize(size: Float) {
        _gridSize.value = size.coerceIn(15f, 120f)
    }

    fun setSymmetryMode(mode: SymmetryMode) {
        _symmetryMode.value = mode
    }

    fun toggleFlipHorizontal() {
        _flipHorizontal.value = !_flipHorizontal.value
    }

    fun toggleFlipVertical() {
        _flipVertical.value = !_flipVertical.value
    }

    fun setLayerBlendMode(layerId: String, mode: String) {
        val currentLayers = getCurrentFrameLayers()
        val updatedLayers = currentLayers.map {
            if (it.id == layerId) it.copy(blendMode = mode) else it
        }
        applyLayersToCurrentFrame(updatedLayers)
        scheduleAutoSave()
    }

    fun setBrushSmoothing(smoothing: Float) {
        _brushConfig.value = _brushConfig.value.copy(smoothing = smoothing.coerceIn(0f, 1f))
    }

    fun toggleZeroLatency() {
        _brushConfig.value = _brushConfig.value.copy(zeroLatency = !_brushConfig.value.zeroLatency)
    }

    // --- Layer Operations ---

    fun getCurrentFrameLayers(): List<DrawingLayer> {
        val proj = _currentProject.value
        val idx = _currentFrameIndex.value.coerceIn(0, (proj.frames.size - 1).coerceAtLeast(0))
        return if (proj.frames.isNotEmpty()) proj.frames[idx].layers else emptyList()
    }

    fun getActiveLayer(): DrawingLayer? {
        val layers = getCurrentFrameLayers()
        return layers.find { it.id == _activeLayerId.value } ?: layers.firstOrNull()
    }

    fun setActiveLayer(layerId: String) {
        _activeLayerId.value = layerId
    }

    fun addLayer() {
        pushUndoState()
        val currentLayers = getCurrentFrameLayers()
        val newLayer = DrawingLayer(
            name = "Layer ${currentLayers.size + 1}",
            opacity = 1.0f
        )
        val updatedLayers = currentLayers + newLayer
        applyLayersToCurrentFrame(updatedLayers)
        _activeLayerId.value = newLayer.id
        scheduleAutoSave()
    }

    fun duplicateLayer(layerId: String) {
        pushUndoState()
        val currentLayers = getCurrentFrameLayers()
        val target = currentLayers.find { it.id == layerId } ?: return
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            name = "${target.name} Copy"
        )
        val updatedLayers = currentLayers + copy
        applyLayersToCurrentFrame(updatedLayers)
        _activeLayerId.value = copy.id
        scheduleAutoSave()
    }

    fun deleteLayer(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        if (currentLayers.size <= 1) return // Keep at least one layer
        pushUndoState()
        val updatedLayers = currentLayers.filter { it.id != layerId }
        applyLayersToCurrentFrame(updatedLayers)
        if (_activeLayerId.value == layerId) {
            _activeLayerId.value = updatedLayers.last().id
        }
        scheduleAutoSave()
    }

    fun toggleLayerVisibility(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        val updatedLayers = currentLayers.map {
            if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
        }
        applyLayersToCurrentFrame(updatedLayers)
        scheduleAutoSave()
    }

    fun toggleLayerLock(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        val updatedLayers = currentLayers.map {
            if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
        }
        applyLayersToCurrentFrame(updatedLayers)
        scheduleAutoSave()
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        val currentLayers = getCurrentFrameLayers()
        val updatedLayers = currentLayers.map {
            if (it.id == layerId) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
        }
        applyLayersToCurrentFrame(updatedLayers)
        scheduleAutoSave()
    }

    fun mergeLayerDown(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        val index = currentLayers.indexOfFirst { it.id == layerId }
        if (index <= 0) return // Cannot merge lowest layer down

        pushUndoState()
        val upper = currentLayers[index]
        val lower = currentLayers[index - 1]

        val merged = lower.copy(
            strokes = lower.strokes + upper.strokes
        )

        val updatedLayers = currentLayers.toMutableList().apply {
            removeAt(index)
            this[index - 1] = merged
        }
        applyLayersToCurrentFrame(updatedLayers)
        _activeLayerId.value = merged.id
        scheduleAutoSave()
    }

    fun moveLayerUp(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        val index = currentLayers.indexOfFirst { it.id == layerId }
        // In layer stack, higher index means drawn on top. Moving up means index + 1
        if (index < 0 || index >= currentLayers.size - 1) return
        pushUndoState()
        val updated = currentLayers.toMutableList()
        val item = updated.removeAt(index)
        updated.add(index + 1, item)
        applyLayersToCurrentFrame(updated)
        scheduleAutoSave()
    }

    fun moveLayerDown(layerId: String) {
        val currentLayers = getCurrentFrameLayers()
        val index = currentLayers.indexOfFirst { it.id == layerId }
        // Lower index means drawn towards bottom. Moving down means index - 1
        if (index <= 0) return
        pushUndoState()
        val updated = currentLayers.toMutableList()
        val item = updated.removeAt(index)
        updated.add(index - 1, item)
        applyLayersToCurrentFrame(updated)
        scheduleAutoSave()
    }

    fun clearActiveLayer() {
        val active = getActiveLayer() ?: return
        pushUndoState()
        val currentLayers = getCurrentFrameLayers()
        val updatedLayers = currentLayers.map {
            if (it.id == active.id) it.copy(strokes = emptyList()) else it
        }
        applyLayersToCurrentFrame(updatedLayers)
        scheduleAutoSave()
    }

    private fun applyLayersToCurrentFrame(layers: List<DrawingLayer>) {
        val currentProj = _currentProject.value
        val frameIdx = _currentFrameIndex.value.coerceIn(0, currentProj.frames.size - 1)
        val updatedFrames = currentProj.frames.toMutableList().apply {
            this[frameIdx] = this[frameIdx].copy(layers = layers)
        }
        _currentProject.value = currentProj.copy(frames = updatedFrames)
    }

    // --- Animation & Frames Operations ---

    fun toggleAnimationPlayback() {
        if (_isPlayingAnimation.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        if (_currentProject.value.frames.size <= 1) return
        _isPlayingAnimation.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val fps = _currentProject.value.fps.coerceIn(1, 60)
            val delayMs = (1000L / fps)
            while (isActive && _isPlayingAnimation.value) {
                val nextIdx = (_currentFrameIndex.value + 1) % _currentProject.value.frames.size
                _currentFrameIndex.value = nextIdx
                delay(delayMs)
            }
        }
    }

    fun stopPlayback() {
        _isPlayingAnimation.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun setFrameIndex(index: Int) {
        if (_isPlayingAnimation.value) stopPlayback()
        val total = _currentProject.value.frames.size
        if (total == 0) return
        _currentFrameIndex.value = index.coerceIn(0, total - 1)
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()
        // Ensure active layer is valid
        val layers = getCurrentFrameLayers()
        if (layers.none { it.id == _activeLayerId.value }) {
            _activeLayerId.value = layers.firstOrNull()?.id ?: ""
        }
    }

    fun addFrame() {
        val proj = _currentProject.value
        val newFrame = AnimationFrame(
            index = proj.frames.size,
            layers = listOf(DrawingLayer(name = "Layer 1"))
        )
        val updatedFrames = proj.frames + newFrame
        _currentProject.value = proj.copy(frames = updatedFrames)
        setFrameIndex(updatedFrames.size - 1)
        scheduleAutoSave()
    }

    fun duplicateFrame(index: Int) {
        val proj = _currentProject.value
        val target = proj.frames.getOrNull(index) ?: return
        val duplicated = target.copy(
            id = UUID.randomUUID().toString(),
            index = index + 1,
            layers = target.layers.map { layer ->
                layer.copy(id = UUID.randomUUID().toString(), strokes = layer.strokes.toList())
            }
        )
        val updated = proj.frames.toMutableList().apply {
            add(index + 1, duplicated)
        }
        _currentProject.value = proj.copy(frames = updated)
        setFrameIndex(index + 1)
        scheduleAutoSave()
    }

    fun deleteFrame(index: Int) {
        val proj = _currentProject.value
        if (proj.frames.size <= 1) return
        val updated = proj.frames.toMutableList().apply {
            removeAt(index)
        }
        _currentProject.value = proj.copy(frames = updated)
        val newIdx = (index - 1).coerceAtLeast(0)
        setFrameIndex(newIdx)
        scheduleAutoSave()
    }

    fun toggleOnionSkin() {
        _onionSkinEnabled.value = !_onionSkinEnabled.value
    }

    fun setFps(fps: Int) {
        _currentProject.value = _currentProject.value.copy(fps = fps.coerceIn(1, 30))
        scheduleAutoSave()
    }

    // --- Export Operations ---

    fun exportCurrentArtwork(format: ExportFormat, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            _exportProgress.value = 0.1f
            val proj = _currentProject.value
            val context = getApplication<Application>()

            try {
                if (format == ExportFormat.MP4) {
                    // Render all animation frames to bitmaps
                    val frameBitmaps = mutableListOf<Bitmap>()
                    val totalFrames = proj.frames.size
                    proj.frames.forEachIndexed { idx, frame ->
                        val bmp = Bitmap.createBitmap(proj.width, proj.height, Bitmap.Config.ARGB_8888)
                        CanvasRenderer.renderLayersToBitmap(
                            bitmap = bmp,
                            layers = frame.layers,
                            backgroundColor = AndroidColor.WHITE,
                            includeBackground = true
                        )
                        frameBitmaps.add(bmp)
                        _exportProgress.value = 0.1f + 0.3f * ((idx + 1f) / totalFrames)
                    }

                    val mp4File = ExportUtil.exportAnimation(
                        context = context,
                        frames = frameBitmaps,
                        fps = proj.fps,
                        fileName = "${proj.title.replace(" ", "_")}_video"
                    ) { progress ->
                        _exportProgress.value = 0.4f + 0.6f * progress
                    }

                    // Recycle bitmaps
                    frameBitmaps.forEach { it.recycle() }

                    _lastExportedFile.value = mp4File
                    _exportProgress.value = null
                    onComplete(mp4File)
                } else {
                    // Export single frame image (PNG, JPG, WebP)
                    _exportProgress.value = 0.5f
                    val currentLayers = getCurrentFrameLayers()
                    val bmp = Bitmap.createBitmap(proj.width, proj.height, Bitmap.Config.ARGB_8888)
                    val includeBg = format != ExportFormat.PNG // PNG supports transparency
                    CanvasRenderer.renderLayersToBitmap(
                        bitmap = bmp,
                        layers = currentLayers,
                        backgroundColor = if (includeBg) AndroidColor.WHITE else AndroidColor.TRANSPARENT,
                        includeBackground = includeBg
                    )

                    val file = ExportUtil.exportImage(
                        context = context,
                        bitmap = bmp,
                        format = format,
                        fileName = "${proj.title.replace(" ", "_")}"
                    )
                    bmp.recycle()

                    _lastExportedFile.value = file
                    _exportProgress.value = null
                    onComplete(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _exportProgress.value = null
                onComplete(null)
            }
        }
    }

    fun shareExportedFile(file: File, mimeType: String) {
        val context = getApplication<Application>()
        ExportUtil.shareFile(context, file, mimeType, "Share artwork from ZPaint")
    }

    // --- Projects Management ---

    fun loadProject(id: String) {
        viewModelScope.launch {
            if (_isPlayingAnimation.value) stopPlayback()
            val loaded = repository.loadProject(id)
            if (loaded != null) {
                _currentProject.value = loaded
                _currentFrameIndex.value = 0
                _activeLayerId.value = loaded.frames.firstOrNull()?.layers?.firstOrNull()?.id ?: ""
                undoStack.clear()
                redoStack.clear()
                updateUndoRedoStates()
            }
        }
    }

    fun createNewProject(title: String = "Artwork ${System.currentTimeMillis() % 1000}", width: Int = 1080, height: Int = 1080) {
        viewModelScope.launch {
            if (_isPlayingAnimation.value) stopPlayback()
            val newProject = Project(
                id = UUID.randomUUID().toString(),
                title = title,
                width = width,
                height = height,
                fps = 12,
                frames = listOf(AnimationFrame(layers = listOf(DrawingLayer(name = "Layer 1"))))
            )
            repository.saveProject(newProject)
            _currentProject.value = newProject
            _currentFrameIndex.value = 0
            _activeLayerId.value = newProject.frames[0].layers[0].id
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_currentProject.value.id == id) {
                val remaining = repository.loadProject(allProjects.value.firstOrNull { it.id != id }?.id ?: "")
                if (remaining != null) {
                    _currentProject.value = remaining
                } else {
                    createNewProject()
                }
            }
        }
    }

    fun duplicateProject(id: String) {
        viewModelScope.launch {
            val original = repository.loadProject(id) ?: return@launch
            val copyId = UUID.randomUUID().toString()
            val duplicated = original.copy(
                id = copyId,
                title = "${original.title} (نسخة)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            repository.saveProject(duplicated)
        }
    }

    // --- Auto-Save and Cloud Sync ---

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            val proj = _currentProject.value
            // Generate quick preview thumbnail
            val thumb = try {
                val bmp = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
                CanvasRenderer.renderLayersToBitmap(
                    bitmap = bmp,
                    layers = getCurrentFrameLayers(),
                    backgroundColor = AndroidColor.WHITE,
                    includeBackground = true
                )
                bmp
            } catch (e: Exception) {
                null
            }

            repository.saveProject(proj, thumb)
            thumb?.recycle()

            // Trigger cloud synchronization
            syncManager.triggerSync(proj)
        }
    }

    // --- Sliced Images Interactive Editing Mode ---

    fun loadSlicedImagesForEditing(files: List<File>, initialIndex: Int = 0) {
        if (files.isEmpty()) return
        _slicedEditingFiles.value = files
        _isSlicedEditingMode.value = true
        val safeIndex = initialIndex.coerceIn(0, files.size - 1)
        _activeSliceIndex.value = safeIndex
        loadSliceAtIndex(safeIndex)
    }

    fun switchActiveSlice(newIndex: Int) {
        val files = _slicedEditingFiles.value
        if (newIndex !in files.indices) return
        saveCurrentSliceEdits()
        _activeSliceIndex.value = newIndex
        loadSliceAtIndex(newIndex)
    }

    fun exitSlicedEditingMode() {
        saveCurrentSliceEdits()
        _isSlicedEditingMode.value = false
        _slicedEditingFiles.value = emptyList()
    }

    private fun loadSliceAtIndex(index: Int) {
        val files = _slicedEditingFiles.value
        if (index !in files.indices) return
        val file = files[index]

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        val w = if (options.outWidth > 0) options.outWidth else 1080
        val h = if (options.outHeight > 0) options.outHeight else 1920

        val sliceProjectTitle = "قصاصة #${index + 1} (${file.nameWithoutExtension})"

        viewModelScope.launch {
            if (_isPlayingAnimation.value) stopPlayback()

            val existing = allProjects.value.firstOrNull { it.title == sliceProjectTitle }
            if (existing != null) {
                val loaded = repository.loadProject(existing.id)
                if (loaded != null) {
                    _currentProject.value = loaded
                    _currentFrameIndex.value = 0
                    _activeLayerId.value = loaded.frames.firstOrNull()?.layers?.lastOrNull()?.id ?: ""
                    undoStack.clear()
                    redoStack.clear()
                    updateUndoRedoStates()
                    return@launch
                }
            }

            // Create canvas with base slice image layer and top editing layer
            val baseLayer = DrawingLayer(
                id = UUID.randomUUID().toString(),
                name = "الصورة المقصوصة #${index + 1}",
                imagePath = file.absolutePath
            )
            val drawingLayer = DrawingLayer(
                id = UUID.randomUUID().toString(),
                name = "طبقة الرسم والتعديل"
            )
            val project = Project(
                id = UUID.randomUUID().toString(),
                title = sliceProjectTitle,
                width = w,
                height = h,
                frames = listOf(AnimationFrame(layers = listOf(baseLayer, drawingLayer)))
            )
            repository.saveProject(project)
            _currentProject.value = project
            _currentFrameIndex.value = 0
            _activeLayerId.value = drawingLayer.id
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    fun saveCurrentSliceEdits() {
        val proj = _currentProject.value
        viewModelScope.launch {
            repository.saveProject(proj)
        }
    }

    // --- Direct Phone Image Import ---

    fun importImageFromUri(uri: Uri, asNewLayer: Boolean = true, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val importedDir = File(context.filesDir, "imported_images").apply { mkdirs() }
                val destFile = File(importedDir, "imported_${System.currentTimeMillis()}.png")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(destFile.absolutePath, opts)
                val imgW = opts.outWidth
                val imgH = opts.outHeight

                withContext(Dispatchers.Main) {
                    if (asNewLayer) {
                        pushUndoState()
                        val currentLayers = getCurrentFrameLayers()
                        val layerName = "صورة مستوردة (${imgW}×${imgH})"
                        val newLayer = DrawingLayer(
                            id = UUID.randomUUID().toString(),
                            name = layerName,
                            imagePath = destFile.absolutePath
                        )
                        val updatedLayers = currentLayers + newLayer
                        applyLayersToCurrentFrame(updatedLayers)
                        _activeLayerId.value = newLayer.id
                        scheduleAutoSave()
                        onComplete(true)
                    } else {
                        val baseLayer = DrawingLayer(
                            id = UUID.randomUUID().toString(),
                            name = "صورة مستوردة",
                            imagePath = destFile.absolutePath
                        )
                        val drawLayer = DrawingLayer(
                            id = UUID.randomUUID().toString(),
                            name = "طبقة الرسم"
                        )
                        val project = Project(
                            id = UUID.randomUUID().toString(),
                            title = "صورة مستوردة ${System.currentTimeMillis() % 1000}",
                            width = if (imgW > 0) imgW else 1080,
                            height = if (imgH > 0) imgH else 1080,
                            frames = listOf(AnimationFrame(layers = listOf(baseLayer, drawLayer)))
                        )
                        repository.saveProject(project)
                        _currentProject.value = project
                        _currentFrameIndex.value = 0
                        _activeLayerId.value = drawLayer.id
                        undoStack.clear()
                        redoStack.clear()
                        updateUndoRedoStates()
                        onComplete(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}
