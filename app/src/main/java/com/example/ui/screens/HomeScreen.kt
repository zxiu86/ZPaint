package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ProjectEntity
import com.example.data.sync.CloudSyncState
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderActive
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.theme.WhiteSoft
import com.example.ui.viewmodel.DrawingViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ViewAgenda
import com.example.ui.viewmodel.ManhwaStudioTab

@Composable
fun HomeScreen(
    viewModel: DrawingViewModel,
    onOpenProject: (String) -> Unit,
    onOpenNewCanvas: (title: String, width: Int, height: Int) -> Unit,
    onOpenManhwaStudio: (ManhwaStudioTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = WhitePure,
                contentColor = DarkBg,
                shape = CircleShape,
                modifier = Modifier.testTag("home_fab_create")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "لوحة جديدة")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لوحة جديدة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Studio Header Banner (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeStudioHeader(
                    syncState = syncState,
                    onTriggerSync = {
                        val current = viewModel.currentProject.value
                        viewModel.syncManager.triggerSync(current)
                    }
                )
            }

            // 2. Dedicated Manhwa & Giant Image Studio Hub (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                ManhwaStudioHubCard(
                    onOpenSlicer = { onOpenManhwaStudio(ManhwaStudioTab.SLICER) },
                    onOpenStitcher = { onOpenManhwaStudio(ManhwaStudioTab.STITCHER) }
                )
            }

            // 3. Quick Canvas Size Presets (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickPresetsRow(
                    onSelectPreset = { title, w, h ->
                        onOpenNewCanvas(title, w, h)
                    },
                    onCustomClick = { showCreateDialog = true }
                )
            }

            // 4. Engine & Brushes Quality Badges (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                EngineQualityHighlights()
            }

            // 5. Section Title: Recent Projects
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "لوحاتي ومشاريعي",
                            color = WhitePure,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = DarkSurfaceHighlight
                        ) {
                            Text(
                                text = "${allProjects.size}",
                                color = WhiteComfortable,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 6. Empty State or Projects Grid Cards
            if (allProjects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProjectsCard(onCreateClick = { showCreateDialog = true })
                }
            } else {
                items(allProjects, key = { it.id }) { project ->
                    ProjectArtworkCard(
                        project = project,
                        onOpen = { onOpenProject(project.id) },
                        onDuplicate = { viewModel.duplicateProject(project.id) },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, w, h ->
                showCreateDialog = false
                onOpenNewCanvas(title, w, h)
            }
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("حذف اللوحة", color = WhitePure) },
            text = { Text("هل أنت متأكد من حذف \"${proj.title}\"؟ لن تتمكن من التراجع عن هذه الخطوة.", color = WhiteComfortable) },
            containerColor = DarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(proj.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("حذف", color = WhitePure)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("إلغاء", color = WhiteMuted)
                }
            }
        )
    }
}

@Composable
private fun HomeStudioHeader(
    syncState: CloudSyncState,
    onTriggerSync: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(22.dp)),
        color = DarkSurface,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(WhitePure, WhiteComfortable)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "ZPaint Logo",
                            tint = DarkBg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ZPaint Studio",
                            color = WhitePure,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أستوديو الرسم الرقمي والأنيميشن",
                            color = WhiteMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Cloud Sync Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceHighlight,
                    modifier = Modifier.clickable { onTriggerSync() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (syncState) {
                                CloudSyncState.SYNCED -> Icons.Default.CloudDone
                                CloudSyncState.SYNCING -> Icons.Default.CloudSync
                                else -> Icons.Default.Sync
                            },
                            contentDescription = "Sync Status",
                            tint = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteComfortable,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (syncState == CloudSyncState.SYNCED) "سحابي ✓" else "محلي",
                            color = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteComfortable,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManhwaStudioHubCard(
    onOpenSlicer: () -> Unit,
    onOpenStitcher: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(22.dp)),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentGreen.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, AccentGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "أستوديو المانهوا والصور الطويلة والعملاقة",
                            color = WhitePure,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "نظام متكامل للتعامل مع الصور حتى 800×10000+ بكسل بدقة أصلية 100%",
                            color = WhiteMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Free Slicer
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onOpenSlicer() }
                        .testTag("home_manhwa_slicer_card"),
                    color = DarkSurfaceHighlight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFD32F2F).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Text("قص حر", color = WhiteComfortable, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "قص وتجزئة الصور ✂️",
                            color = WhitePure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "قص الصورة إلى أي عدد من الأجزاء بأحجام مخصصة بدون قيود وبدقة أصلية كاملة",
                            color = WhiteMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Card 2: Stitcher / Merger
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onOpenStitcher() }
                        .testTag("home_manhwa_stitcher_card"),
                    color = DarkSurfaceHighlight,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AccentGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentGreen.copy(alpha = 0.15f)
                            ) {
                                Text("تجميع ذكي", color = AccentGreen, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "تجميع ودمج الصور 📑",
                            color = WhitePure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "استيراد صور مسبقة، ترتيبها بالاسم (page1, page2...) أو يدوياً ودمجها لصورة طويلة",
                            color = WhiteMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPresetsRow(
    onSelectPreset: (String, Int, Int) -> Unit,
    onCustomClick: () -> Unit
) {
    Column {
        Text(
            text = "بدء لوحة جديدة بأبعاد سريعة",
            color = WhiteMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                label = "مربعة 1:1",
                desc = "1080×1080",
                icon = Icons.Default.AspectRatio,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset("لوحة مربعة", 1080, 1080) }
            )
            PresetChip(
                label = "مانهوا ويبتون",
                desc = "800×8000",
                icon = Icons.Default.ViewAgenda,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset("شريط مانهوا", 800, 8000) }
            )
            PresetChip(
                label = "ستوري 9:16",
                desc = "1080×1920",
                icon = Icons.Default.AspectRatio,
                modifier = Modifier.weight(1f),
                onClick = { onSelectPreset("لوحة ستوري", 1080, 1920) }
            )
            PresetChip(
                label = "مخصص",
                desc = "أبعاد يدوية",
                icon = Icons.Default.Edit,
                modifier = Modifier.weight(1f),
                onClick = onCustomClick
            )
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = WhitePure,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = WhitePure,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = desc,
                color = WhiteMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EngineQualityHighlights() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(16.dp)),
        color = DarkSurfaceHighlight,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF2E7D32).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "C++ Engine",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "محرك C++ فائق السلاسة (Catmull-Rom & 1-Euro)",
                    color = WhitePure,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حركة قلم سلسة كالحرير بدون أي اهتزاز + 35 فرشاة Brusheezy",
                    color = WhiteComfortable,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectArtworkCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Load thumbnail bitmap safely
    val thumbnailBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, project.thumbnailPath, project.updatedAt) {
        val path = project.thumbnailPath
        value = if (path != null && File(path).exists()) {
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val formattedDate = remember(project.updatedAt) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(project.updatedAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
            .clickable { onOpen() }
            .testTag("project_card_${project.id}"),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Artwork Preview Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .background(Color.White)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!.asImageBitmap(),
                        contentDescription = project.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${project.width} × ${project.height}",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                // Dimension Tag (top-left)
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${project.width}×${project.height}",
                        color = Color.White,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Info & Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedDate,
                        color = WhiteMuted,
                        fontSize = 10.sp
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = WhiteComfortable,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceHighlight)
                    ) {
                        DropdownMenuItem(
                            text = { Text("فتح ومتابعة الرسم", color = WhitePure, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Brush, null, tint = WhitePure, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تكرار اللوحة", color = WhitePure, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = WhitePure, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف اللوحة", color = Color(0xFFEF5350), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyProjectsCard(onCreateClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(18.dp)),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = WhiteMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "لا توجد لوحات بعد",
                color = WhitePure,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ابدأ لوحتك الأولى الآن باستخدام محرك C++ فائق السلاسة ومكتبة فرش Brusheezy!",
                color = WhiteMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = DarkBg)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إنشاء لوحة جديدة", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, width: Int, height: Int) -> Unit
) {
    var title by remember { mutableStateOf("لوحة فنية جديدة") }
    var widthText by remember { mutableStateOf("1080") }
    var heightText by remember { mutableStateOf("1080") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إنشاء لوحة عمل جديدة", color = WhitePure, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان اللوحة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhitePure,
                        unfocusedTextColor = WhiteComfortable,
                        focusedBorderColor = WhitePure,
                        unfocusedBorderColor = GrayBorderComfortable
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("العرض (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhiteComfortable,
                            focusedBorderColor = WhitePure,
                            unfocusedBorderColor = GrayBorderComfortable
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("الارتفاع (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhitePure,
                            unfocusedTextColor = WhiteComfortable,
                            focusedBorderColor = WhitePure,
                            unfocusedBorderColor = GrayBorderComfortable
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("مقاسات سريعة:", color = WhiteMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = { widthText = "1080"; heightText = "1080" },
                        colors = ButtonDefaults.textButtonColors(contentColor = WhiteComfortable)
                    ) {
                        Text("1:1 مربع", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { widthText = "800"; heightText = "8000" },
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentGreen)
                    ) {
                        Text("مانهوا 800×8000", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { widthText = "1080"; heightText = "1920" },
                        colors = ButtonDefaults.textButtonColors(contentColor = WhiteComfortable)
                    ) {
                        Text("9:16 ستوري", fontSize = 11.sp)
                    }
                }
            }
        },
        containerColor = DarkSurface,
        confirmButton = {
            Button(
                onClick = {
                    val w = widthText.toIntOrNull()?.coerceIn(100, 4096) ?: 1080
                    val h = heightText.toIntOrNull()?.coerceIn(100, 16384) ?: 1080
                    val finalTitle = title.ifBlank { "لوحة جديدة" }
                    onCreate(finalTitle, w, h)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = DarkBg)
            ) {
                Text("بدء الرسم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = WhiteMuted)
            }
        }
    )
}
