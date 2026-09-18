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

                // 2. Dynamic, Responsive Top Header Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 900.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = if (isWideScreen) 24.dp else 10.dp, vertical = 8.dp)
                        .border(1.dp, GrayBorderComfortable, RoundedCornerShape(22.dp))
                        .testTag("top_header_bar"),
                    color = DarkSurface.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isWideScreen) 14.dp else 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Return to Home & Projects Gallery Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onNavigateToHome,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkSurfaceElevated, CircleShape)
                                    .testTag("nav_home_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "العودة للرئيسية",
                                    tint = WhitePure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showProjectsGallery = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("open_gallery_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Projects Gallery",
                                    tint = WhiteComfortable,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = currentProject.title,
                                        color = WhitePure,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (syncState) {
                                                CloudSyncState.SYNCED -> Icons.Default.CloudDone
                                                CloudSyncState.SYNCING -> Icons.Default.CloudSync
                                                else -> Icons.Default.Sync
                                            },
                                            contentDescription = "Sync",
                                            tint = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteMuted,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (syncState == CloudSyncState.SYNCED) "سحابي ✓" else "محلي",
                                            color = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteMuted,
                                            fontSize = 10.sp
                                        )
                                        if (isWideScreen) {
                                            Text(
                                                text = " • ${currentProject.width}×${currentProject.height}",
                                                color = WhiteMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Center: Undo and Redo & Sliders Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.undo() },
                                enabled = canUndo,
                                modifier = Modifier.size(34.dp).testTag("undo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (canUndo) WhitePure else WhiteMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.redo() },
                                enabled = canRedo,
                                modifier = Modifier.size(34.dp).testTag("redo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (canRedo) WhitePure else WhiteMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { showQuickSliders = !showQuickSliders },
                                modifier = Modifier.size(34.dp).testTag("quick_sliders_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Quick Sliders",
                                    tint = if (showQuickSliders) AccentGreen else WhiteComfortable,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Right: Animation Timeline, Tools & Guides Sliding Drawer, Layers, Export
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 6.dp else 4.dp)
                        ) {
                            // Animation Mode Toggle
                            IconButton(
                                onClick = { showAnimationTimeline = !showAnimationTimeline },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (showAnimationTimeline) DarkSurfaceHighlight else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("animation_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = "Animation Timeline",
                                    tint = if (showAnimationTimeline) WhitePure else WhiteComfortable,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Sliding Tools & Guides Sheet
                            IconButton(
                                onClick = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.TOOLS) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (activeSlidingSheet == ActiveSlidingSheet.TOOLS) DarkSurfaceHighlight else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("header_tools_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Tools and Guides",
                                    tint = if (showGrid || symmetryMode != com.example.model.SymmetryMode.NONE) AccentGreen else WhitePure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Sliding Layers Sheet
                            IconButton(
                                onClick = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.LAYERS) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (activeSlidingSheet == ActiveSlidingSheet.LAYERS) DarkSurfaceHighlight else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("layers_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Layers",
                                    tint = WhitePure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Sliding Export & Share Sheet
                            IconButton(
                                onClick = { viewModel.setActiveSlidingSheet(ActiveSlidingSheet.EXPORT) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(WhitePure, CircleShape)
                                    .testTag("export_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export & Share",
                                    tint = Color(0xFF101014),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

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
