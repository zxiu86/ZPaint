package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.sync.CloudSyncState
import com.example.model.ActiveSlidingSheet
import com.example.model.BrushType
import com.example.ui.components.AnimationTimeline
import com.example.ui.components.DrawingCanvas
import com.example.ui.components.ProjectsGalleryDialog
import com.example.ui.components.QuickSlidersRail
import com.example.ui.components.SlidingStudioDrawer
import com.example.ui.components.StudioHeader
import com.example.ui.components.ToolPalette
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.viewmodel.DrawingViewModel

@Composable
fun DrawingScreen(
    viewModel: DrawingViewModel,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsStateWithLifecycle()
    val activeLayerId by viewModel.activeLayerId.collectAsStateWithLifecycle()
    val brushConfig by viewModel.brushConfig.collectAsStateWithLifecycle()
    val activeStroke by viewModel.activeStroke.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlayingAnimation.collectAsStateWithLifecycle()
    val onionSkinEnabled by viewModel.onionSkinEnabled.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()

    // Studio Guides states
    val canvasPaper by viewModel.canvasPaper.collectAsStateWithLifecycle()
    val showGrid by viewModel.showGrid.collectAsStateWithLifecycle()
    val gridSize by viewModel.gridSize.collectAsStateWithLifecycle()
    val symmetryMode by viewModel.symmetryMode.collectAsStateWithLifecycle()
    val flipHorizontal by viewModel.flipHorizontal.collectAsStateWithLifecycle()
    val flipVertical by viewModel.flipVertical.collectAsStateWithLifecycle()
    val activeSlidingSheet by viewModel.activeSlidingSheet.collectAsStateWithLifecycle()

    // Overlay visibility states
    var showProjectsGallery by remember { mutableStateOf(false) }
    var showAnimationTimeline by remember { mutableStateOf(false) }
    var showQuickSliders by remember { mutableStateOf(false) }

    // Memory for toggle eraser/smudge return
    var previousBrushType by remember { mutableStateOf(BrushType.PEN) }

    val currentLayers = viewModel.getCurrentFrameLayers()
    val onionSkinLayers = remember(currentProject, currentFrameIndex, onionSkinEnabled) {
        if (onionSkinEnabled && currentFrameIndex > 0) {
            currentProject.frames.getOrNull(currentFrameIndex - 1)?.layers
        } else {
            null
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Zero-Latency Canvas with Butter-Smooth Bézier Splines, Grid, Paper & Symmetry
                DrawingCanvas(
                    layers = currentLayers,
                    activeStroke = activeStroke,
                    canvasWidth = currentProject.width,
                    canvasHeight = currentProject.height,
                    canvasPaper = canvasPaper,
                    showGrid = showGrid,
                    gridSize = gridSize,
                    symmetryMode = symmetryMode,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical,
                    onionSkinLayers = onionSkinLayers,
                    onStartStroke = { viewModel.startStroke(it) },
                    onAppendPoint = { viewModel.appendPointToStroke(it) },
                    onEndStroke = { viewModel.endStroke() },
                    onTwoFingerTapUndo = { viewModel.undo() },
                    onThreeFingerTapRedo = { viewModel.redo() },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Dynamic, Responsive Top Header Bar (StudioHeader)
                StudioHeader(
                    projectTitle = currentProject.title,
                    canvasWidth = currentProject.width,
                    canvasHeight = currentProject.height,
                    syncState = syncState,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    showQuickSliders = showQuickSliders,
                    showAnimationTimeline = showAnimationTimeline,
                    activeSlidingSheet = activeSlidingSheet,
                    showGrid = showGrid,
                    symmetryMode = symmetryMode,
                    layerCount = currentLayers.size,
                    frameCount = currentProject.frames.size,
                    currentFrameIndex = currentFrameIndex,
                    isWideScreen = isWideScreen,
                    onNavigateBack = onNavigateToHome,
                    onOpenGallery = { showProjectsGallery = true },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onToggleQuickSliders = { showQuickSliders = !showQuickSliders },
                    onToggleAnimationTimeline = { showAnimationTimeline = !showAnimationTimeline },
                    onOpenToolsSheet = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.TOOLS) },
                    onOpenLayersSheet = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.LAYERS) },
                    onOpenExportSheet = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.EXPORT) },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // 3. Quick Sliders Rail (Floating for quick thumb adjustments)
                AnimatedVisibility(
                    visible = showQuickSliders,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 14.dp),
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    QuickSlidersRail(
                        brushSize = brushConfig.size,
                        brushOpacity = brushConfig.opacity,
                        onSizeChange = { viewModel.setBrushSize(it) },
                        onOpacityChange = { viewModel.setBrushOpacity(it) }
                    )
                }

                // 4. Floating Studio Dock (Responsive bottom tool palette)
                ToolPalette(
                    brushConfig = brushConfig,
                    layerCount = currentLayers.size,
                    onOpenBrushStudio = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.BRUSHES) },
                    onOpenLayers = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.LAYERS) },
                    onOpenTools = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.TOOLS) },
                    onOpenColorPicker = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.COLOR) },
                    onToggleEraser = {
                        if (brushConfig.type == BrushType.ERASER) {
                            viewModel.setBrushType(previousBrushType)
                        } else {
                            previousBrushType = brushConfig.type
                            viewModel.setBrushType(BrushType.ERASER)
                        }
                    },
                    onToggleSmudge = {
                        if (brushConfig.type == BrushType.SMUDGE) {
                            viewModel.setBrushType(previousBrushType)
                        } else {
                            previousBrushType = brushConfig.type
                            viewModel.setBrushType(BrushType.SMUDGE)
                        }
                    },
                    onClearLayer = { viewModel.clearActiveLayer() },
                    onQuickSizeChange = { viewModel.setBrushSize(it) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showAnimationTimeline) 140.dp else 16.dp)
                )

                // 5. Animation Timeline Panel (Bottom)
                AnimatedVisibility(
                    visible = showAnimationTimeline,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    AnimationTimeline(
                        frames = currentProject.frames,
                        currentFrameIndex = currentFrameIndex,
                        isPlaying = isPlaying,
                        onionSkinEnabled = onionSkinEnabled,
                        fps = currentProject.fps,
                        onSelectFrame = { viewModel.setFrameIndex(it) },
                        onTogglePlay = { viewModel.toggleAnimationPlayback() },
                        onAddFrame = { viewModel.addFrame() },
                        onDuplicateFrame = { viewModel.duplicateFrame(it) },
                        onDeleteFrame = { viewModel.deleteFrame(it) },
                        onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                        onFpsChange = { viewModel.setFps(it) }
                    )
                }

                // 6. Sliding Studio Drawer (Responsive sliding panel with Brushes, Layers, Tools, Color, Export)
                SlidingStudioDrawer(
                    activeSheet = activeSlidingSheet,
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissSlidingSheet() }
                )

                // 7. Projects Gallery Dialog
                if (showProjectsGallery) {
                    ProjectsGalleryDialog(
                        projects = allProjects,
                        currentProjectId = currentProject.id,
                        syncState = syncState,
                        onSelectProject = { viewModel.loadProject(it) },
                        onCreateNewProject = { title, w, h ->
                            viewModel.createNewProject(title, w, h)
                        },
                        onDeleteProject = { viewModel.deleteProject(it) },
                        onTriggerSync = { viewModel.syncManager.triggerSync(currentProject) },
                        onDismiss = { showProjectsGallery = false }
                    )
                }
            }
        }
    }
}
