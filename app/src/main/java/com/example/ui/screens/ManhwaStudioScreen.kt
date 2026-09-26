package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.viewmodel.ManhwaStudioTab
import com.example.ui.viewmodel.ManhwaStudioViewModel
import com.example.util.ExportFormat
import com.example.util.ExportUtil
import com.example.util.ImageSliceInfo
import com.example.util.StitchImageItem
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ManhwaStudioScreen(
    viewModel: ManhwaStudioViewModel,
    onNavigateBack: () -> Unit,
    onOpenInDrawingCanvas: (width: Int, height: Int) -> Unit = { _, _ -> },
    onEditSlices: (List<File>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar
            ManhwaStudioTopBar(
                onNavigateBack = onNavigateBack,
                selectedTab = selectedTab
            )

            // Tabs Selector
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = DarkSurface,
                contentColor = WhitePure,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = AccentGreen,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GrayBorderSubtle))
                }
            ) {
                Tab(
                    selected = selectedTab == ManhwaStudioTab.SLICER,
                    onClick = { viewModel.selectTab(ManhwaStudioTab.SLICER) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أداة القص الحر للصور الطويلة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = AccentGreen,
                    unselectedContentColor = WhiteMuted,
                    modifier = Modifier.testTag("tab_manhwa_slicer")
                )
                Tab(
                    selected = selectedTab == ManhwaStudioTab.STITCHER,
                    onClick = { viewModel.selectTab(ManhwaStudioTab.STITCHER) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewAgenda, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تجميع ودمج الصور عمودياً", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = AccentGreen,
                    unselectedContentColor = WhiteMuted,
                    modifier = Modifier.testTag("tab_manhwa_stitcher")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    ManhwaStudioTab.SLICER -> ManhwaSlicerContent(
                        viewModel = viewModel,
                        onEditSlices = onEditSlices
                    )
                    ManhwaStudioTab.STITCHER -> ManhwaStitcherContent(
                        viewModel = viewModel,
                        onOpenInDrawingCanvas = onOpenInDrawingCanvas
                    )
                }
            }
        }
    }
}

@Composable
private fun ManhwaStudioTopBar(
    onNavigateBack: () -> Unit,
    selectedTab: ManhwaStudioTab
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .background(DarkSurfaceElevated, CircleShape)
                        .testTag("manhwa_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = WhitePure,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "أستوديو المانهوا والصور العملاقة",
                            color = WhitePure,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen)
                        ) {
                            Text(
                                text = "v1.3.1 Pro",
                                color = AccentGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "معالجة دقيقة بدون فقد حتى 800×10000+ بكسل مع تحكم كامل بالذاكرة",
                        color = WhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// 1. FREE CUT / SLICER SCREEN COMPONENT
// =============================================================================

@Composable
private fun ManhwaSlicerContent(
    viewModel: ManhwaStudioViewModel,
    onEditSlices: (List<File>) -> Unit = {}
) {
    val context = LocalContext.current
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val imageFileName by viewModel.imageFileName.collectAsStateWithLifecycle()
    val origW by viewModel.originalWidth.collectAsStateWithLifecycle()
    val origH by viewModel.originalHeight.collectAsStateWithLifecycle()
    val previewBitmap by viewModel.previewBitmap.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingImage.collectAsStateWithLifecycle()
    val cutLines by viewModel.cutLinesY.collectAsStateWithLifecycle()
    val slicesList by viewModel.slicesList.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExportingSlices.collectAsStateWithLifecycle()
    val exportProgress by viewModel.sliceExportProgress.collectAsStateWithLifecycle()
    val exportStatus by viewModel.sliceExportStatusText.collectAsStateWithLifecycle()
    val exportedFiles by viewModel.exportedSliceFiles.collectAsStateWithLifecycle()
    val exportedZip by viewModel.exportedZipFile.collectAsStateWithLifecycle()

    var selectedExportFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedCountMessage by remember { mutableStateOf(0) }

    // System Photo Picker (Zero-permission safe Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadSourceImage(uri)
        }
    }

    if (selectedImageUri == null) {
        // Empty state: Hero Card to pick image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(AccentGreen.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, AccentGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "أداة القص الحر للمانهوا والصور الطويلة",
                        color = WhitePure,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "قم بقص أي صورة طويلة إلى أي عدد من الأجزاء وبأحجام مختلفة تماماً وبدون فرض أي ارتفاع موحد، مع الحفاظ على أصل الدقة بالكامل وعدم استخدام التقاط الشاشة (Screenshots).",
                        color = WhiteComfortable,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color(0xFF101014)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("slicer_pick_image_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختيار صورة مانهوا أو صورة طويلة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        // Main Slicer Layout with Live Interactive Preview + Controls
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Info Bar
            Surface(
                color = DarkSurfaceHighlight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = imageFileName,
                            color = WhitePure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$origW × $origH بكسل • ${slicesList.size} قصاصات مقترحة",
                            color = AccentGreen,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = WhitePure),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("تغيير الصورة", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Quick Auto-Split Toolbar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text("أدوات سريعة:", color = WhiteMuted, fontSize = 11.sp)
                }
                item {
                    QuickActionPill(
                        text = "+ إضافة خط بالمنتصف",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.addCutLine(origH / 2) }
                    )
                }
                item {
                    QuickActionPill(
                        text = "تقسيم لـ 3 أجزاء",
                        icon = Icons.Default.CallSplit,
                        onClick = { viewModel.autoSplitByCount(3) }
                    )
                }
                item {
                    QuickActionPill(
                        text = "تقسيم لـ 4 أجزاء",
                        icon = Icons.Default.CallSplit,
                        onClick = { viewModel.autoSplitByCount(4) }
                    )
                }
                item {
                    QuickActionPill(
                        text = "كل 2000px",
                        icon = Icons.Default.CallSplit,
                        onClick = { viewModel.autoSplitByMaxHeight(2000) }
                    )
                }
                item {
                    QuickActionPill(
                        text = "مسح جميع الخطوط",
                        icon = Icons.Default.Clear,
                        color = Color(0xFFE57373),
                        onClick = { viewModel.clearAllCuts() }
                    )
                }
            }

            // Main Visual Slicing Viewport (Scrollable with draggable cut lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkBg)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentGreen)
                    }
                } else if (previewBitmap != null) {
                    InteractiveSlicerCanvas(
                        previewBitmap = previewBitmap!!,
                        origWidth = origW,
                        origHeight = origH,
                        cutLines = cutLines,
                        onAddCutLine = { y -> viewModel.addCutLine(y) },
                        onUpdateCutLine = { idx, newY -> viewModel.updateCutLine(idx, newY) },
                        onRemoveCutLine = { y -> viewModel.removeCutLine(y) }
                    )
                }
            }

            // Slices Info & Export Bottom Sheet / Dock
            SlicesExportDock(
                slices = slicesList,
                isExporting = isExporting,
                exportProgress = exportProgress,
                exportStatus = exportStatus,
                selectedFormat = selectedExportFormat,
                onSelectFormat = { selectedExportFormat = it },
                onEditClick = {
                    if (exportedFiles.isNotEmpty()) {
                        onEditSlices(exportedFiles)
                    } else {
                        viewModel.exportSlices(
                            format = selectedExportFormat,
                            saveToGallery = false,
                            createZip = false,
                            onComplete = { files, _, _ ->
                                if (files.isNotEmpty()) {
                                    onEditSlices(files)
                                }
                            }
                        )
                    }
                },
                onExportClick = {
                    viewModel.exportSlices(
                        format = selectedExportFormat,
                        saveToGallery = true,
                        createZip = true,
                        onComplete = { files, zip, saved ->
                            savedCountMessage = saved
                            showSuccessDialog = true
                        }
                    )
                }
            )
        }
    }

    // Export Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = AccentGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اكتمل قص وتصدير المانهوا!", color = WhitePure, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تم إنشاء ${exportedFiles.size} قصاصة بدقة أصلية كاملة وبدون أي فقدان للجودة.",
                        color = WhiteComfortable,
                        fontSize = 13.sp
                    )
                    if (savedCountMessage > 0) {
                        Text(
                            text = "✓ تم حفظ $savedCountMessage قصاصة في مجلد المعرض (Pictures/ZPaint_Manhwa).",
                            color = AccentGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (exportedZip != null) {
                        Text(
                            text = "✓ تم تجهيز ملف مضغوط ZIP يحتوي على جميع القصاصات.",
                            color = WhitePure,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            containerColor = DarkSurface,
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Direct Edit Button as requested by user
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onEditSlices(exportedFiles)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                        modifier = Modifier.testTag("slicer_success_edit_btn")
                    ) {
                        Icon(Icons.Default.Brush, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحرير القصاصات", fontWeight = FontWeight.Bold)
                    }

                    // Share button
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            if (exportedZip != null) {
                                ExportUtil.shareFile(context, exportedZip!!, "application/zip", "Manhwa Slices ZIP")
                            } else if (exportedFiles.isNotEmpty()) {
                                ExportUtil.shareFile(context, exportedFiles.first(), "image/png", "Manhwa Slice")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = WhitePure)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("تم", color = WhiteComfortable)
                }
            }
        )
    }
}

@Composable
private fun QuickActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = WhiteComfortable,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Interactive Visual Slicing Viewport:
 * Renders the downsampled preview bitmap, detects taps to add cut lines,
 * and renders draggable horizontal cut lines with coordinates.
 */
@Composable
private fun InteractiveSlicerCanvas(
    previewBitmap: Bitmap,
    origWidth: Int,
    origHeight: Int,
    cutLines: List<Int>,
    onAddCutLine: (y: Int) -> Unit,
    onUpdateCutLine: (index: Int, newY: Int) -> Unit,
    onRemoveCutLine: (y: Int) -> Unit
) {
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                var displayedHeightPx by remember { mutableFloatStateOf(1f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            displayedHeightPx = coordinates.size.height.toFloat()
                        }
                        .pointerInput(origHeight) {
                            detectTapGestures { tapOffset ->
                                if (displayedHeightPx > 0) {
                                    val scale = origHeight.toFloat() / displayedHeightPx
                                    val originalY = (tapOffset.y * scale).toInt()
                                    onAddCutLine(originalY)
                                }
                            }
                        }
                ) {
                    // 1. Manhwa Image Preview
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Manhwa Preview",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )

                    // 2. Interactive Cut Lines Overlays
                    if (displayedHeightPx > 10 && origHeight > 0) {
                        val scale = displayedHeightPx / origHeight.toFloat()

                        cutLines.forEachIndexed { index, cutY ->
                            val lineOffsetY = cutY * scale

                            // Cut Line UI Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset { IntOffset(0, (lineOffsetY - 16.dp.toPx()).roundToInt()) }
                                    .height(32.dp)
                            ) {
                                // Red dashed cut line
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                ) {
                                    val y = size.height / 2
                                    drawLine(
                                        color = Color(0xFFFF334B),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 3.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                                    )
                                }

                                // Interactive Draggable Badge & Delete Button
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .shadow(4.dp, RoundedCornerShape(20.dp))
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFD32F2F))
                                        .pointerInput(cutY) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val deltaOrigY = (dragAmount.y / scale).toInt()
                                                onUpdateCutLine(index, cutY + deltaOrigY)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = "Cut",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "قص #${index + 1} (${cutY}px)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Delete Cut Line
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                            .clickable { onRemoveCutLine(cutY) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlicesExportDock(
    slices: List<ImageSliceInfo>,
    isExporting: Boolean,
    exportProgress: Float,
    exportStatus: String,
    selectedFormat: ExportFormat,
    onSelectFormat: (ExportFormat) -> Unit,
    onEditClick: () -> Unit = {},
    onExportClick: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Slices preview strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفاصيل القصاصات الناتجة (${slices.size} أجزاء):",
                    color = WhiteComfortable,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Format Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(ExportFormat.PNG, ExportFormat.WEBP, ExportFormat.JPG).forEach { fmt ->
                        val isSelected = fmt == selectedFormat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WhitePure else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { onSelectFormat(fmt) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = fmt.extension.uppercase(),
                                color = if (isSelected) Color.Black else WhiteComfortable,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Slices Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(slices) { slice ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrayBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("قصاصة #${slice.index}", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${slice.height}px", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("${slice.startY} → ${slice.endY}", color = WhiteMuted, fontSize = 9.sp)
                        }
                    }
                }
            }

            if (isExporting) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { exportProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentGreen,
                    trackColor = DarkBg
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exportStatus,
                    color = AccentGreen,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Direct Edit in Canvas & Cut Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary Direct Edit Button
                Button(
                    onClick = onEditClick,
                    enabled = !isExporting && slices.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = WhitePure),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.38f)
                        .height(48.dp)
                        .border(1.dp, GrayBorderComfortable, RoundedCornerShape(14.dp))
                        .testTag("slicer_dock_edit_btn")
                ) {
                    Icon(Icons.Default.Brush, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "تحرير", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Main Export Button
                Button(
                    onClick = onExportClick,
                    enabled = !isExporting && slices.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.62f)
                        .height(48.dp)
                        .testTag("slicer_export_btn")
                ) {
                    Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قص وتصدير (${slices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. IMPORT & STITCH SCREEN COMPONENT
// =============================================================================

@Composable
private fun ManhwaStitcherContent(
    viewModel: ManhwaStudioViewModel,
    onOpenInDrawingCanvas: (width: Int, height: Int) -> Unit
) {
    val context = LocalContext.current
    val stitchItems by viewModel.stitchItems.collectAsStateWithLifecycle()
    val gapPx by viewModel.gapPx.collectAsStateWithLifecycle()
    val showSeams by viewModel.showSeams.collectAsStateWithLifecycle()
    val isStitching by viewModel.isStitching.collectAsStateWithLifecycle()
    val stitchProgress by viewModel.stitchProgress.collectAsStateWithLifecycle()
    val stitchedResultFile by viewModel.stitchedResultFile.collectAsStateWithLifecycle()
    val pdfResultFile by viewModel.pdfResultFile.collectAsStateWithLifecycle()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Multi Image Picker (Zero-permission safe Android Photo Picker)
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addStitchImages(uris)
        }
    }

    if (stitchItems.isEmpty()) {
        // Empty State: Hero Card to import images
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(AccentGreen.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, AccentGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "استيراد وتجميع الصور عمودياً (Stitcher)",
                        color = WhitePure,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "استورد عدة صور مقصوصة مسبقاً (مثل page1.png, page2.png...) وسيقوم التطبيق بترتيبها وتجميعها تلقائياً من الأعلى للأسفل حسب أسمائها، مع إمكانية إعادة الترتيب اليدوي وتصدير مانهوا واحدة طويلة فائقة الوضوح.",
                        color = WhiteComfortable,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            multiPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color(0xFF101014)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("stitcher_pick_images_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استيراد الصور المقصوصة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    } else {
        // Main Stitcher Screen with Reorderable List + Realtime Stream Preview + Actions
        val totalHeight = remember(stitchItems, gapPx) {
            val maxW = if (stitchItems.isNotEmpty()) stitchItems.maxOf { it.width } else 800
            val heights = stitchItems.map {
                val scale = maxW.toFloat() / it.width.coerceAtLeast(1).toFloat()
                (it.height * scale).roundToInt()
            }
            heights.sum() + (stitchItems.size - 1) * gapPx
        }
        val targetWidth = remember(stitchItems) {
            if (stitchItems.isNotEmpty()) stitchItems.maxOf { it.width } else 800
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header stats & quick actions
            Surface(
                color = DarkSurfaceHighlight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${stitchItems.size} صفحات مجمعة",
                            color = WhitePure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الأبعاد المجمعة: $targetWidth × $totalHeight بكسل",
                            color = AccentGreen,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.sortByFileNameNaturally() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = AccentGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.SortByAlpha, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ترتيب بالأسماء", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                multiPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gap & Seams Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("فراغ التجميع:", color = WhiteMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${gapPx}px", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = gapPx.toFloat(),
                    onValueChange = { viewModel.setGapPx(it.toInt()) },
                    valueRange = 0f..40f,
                    colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen),
                    modifier = Modifier.width(140.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleShowSeams() }
                ) {
                    Text("حدود الصفحات", color = WhiteComfortable, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = showSeams,
                        onCheckedChange = { viewModel.toggleShowSeams() },
                        colors = SwitchDefaults.colors(checkedThumbColor = WhitePure, checkedTrackColor = AccentGreen)
                    )
                }
            }

            // Reorderable Pages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkBg),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(stitchItems, key = { _, item -> item.id }) { index, item ->
                    StitchItemRow(
                        index = index,
                        totalCount = stitchItems.size,
                        item = item,
                        showSeams = showSeams,
                        onMoveUp = { viewModel.moveStitchItemUp(index) },
                        onMoveDown = { viewModel.moveStitchItemDown(index) },
                        onDelete = { viewModel.removeStitchItem(item.id) }
                    )
                }
            }

            // Stitch & Export Bottom Bar
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Export Format Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صيغة التجميع والتصدير:", color = WhiteComfortable, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(ExportFormat.PNG, ExportFormat.WEBP, ExportFormat.JPG).forEach { fmt ->
                                val isSelected = fmt == selectedFormat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) WhitePure else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { selectedFormat = fmt }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = fmt.extension.uppercase(),
                                        color = if (isSelected) Color.Black else WhiteComfortable,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isStitching) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { stitchProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentGreen,
                            trackColor = DarkBg
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "جارٍ دمج ومعالجة الصفحات بدقة عالية...",
                            color = AccentGreen,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Export Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.stitchAndExport(
                                    format = selectedFormat,
                                    onComplete = { file ->
                                        if (file != null) showSuccessDialog = true
                                    }
                                )
                            },
                            enabled = !isStitching,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("stitch_export_single_btn")
                        ) {
                            Icon(Icons.Default.Download, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تجميع لصورة واحدة طويلة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.exportToPdf { file ->
                                    if (file != null) showSuccessDialog = true
                                }
                            },
                            enabled = !isStitching,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = WhitePure),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Export Success Dialog
    if (showSuccessDialog) {
        val resultFile = stitchedResultFile ?: pdfResultFile
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = AccentGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اكتمل تجميع الصور بنجاح!", color = WhitePure, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "تم تجميع ${stitchItems.size} صفحات عمودياً في ملف واحد فائق الوضوح.",
                        color = WhiteComfortable,
                        fontSize = 13.sp
                    )
                    if (resultFile != null) {
                        Text(
                            text = "الملف: ${resultFile.name}",
                            color = AccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            containerColor = DarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        if (resultFile != null) {
                            val mime = if (resultFile.name.endsWith(".pdf")) "application/pdf" else "image/png"
                            ExportUtil.shareFile(context, resultFile, mime, "Stitched Manhwa")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("إغلاق", color = WhiteComfortable)
                }
            }
        )
    }
}

@Composable
private fun StitchItemRow(
    index: Int,
    totalCount: Int,
    item: StitchImageItem,
    showSeams: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (showSeams) GrayBorderActive else GrayBorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Index & Thumbnail
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = AccentGreen.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            color = AccentGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.fileName,
                        color = WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.width} × ${item.height} px",
                        color = WhiteMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Actions: Move Up, Move Down, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (index > 0) WhitePure else WhiteMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (index < totalCount - 1) WhitePure else WhiteMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
