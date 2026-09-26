package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveSlidingSheet
import com.example.model.BrushCategory
import com.example.model.BrushConfig
import com.example.model.BrushType
import com.example.model.CanvasPaper
import com.example.model.DrawingLayer
import com.example.model.SymmetryMode
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
import com.example.ui.viewmodel.DrawingViewModel
import com.example.util.ExportFormat
import java.io.File

/**
 * Modern, responsive sliding drawer system for Studio controls:
 * - Automatically adapts dimensions for compact phones and wide tablets.
 * - Houses Brushes, Layers, Studio Tools & Guides, Color Palettes, and Export options.
 */
@Composable
fun SlidingStudioDrawer(
    activeSheet: ActiveSlidingSheet,
    viewModel: DrawingViewModel,
    onOpenManhwaStudio: () -> Unit = {},
    onImportImage: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = activeSheet != ActiveSlidingSheet.NONE,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        // Scrim background for tap-to-dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Main Sliding Sheet Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .fillMaxHeight(0.86f)
                    .navigationBarsPadding()
                    .clickable(enabled = false) {} // Prevent click-through to scrim
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .testTag("sliding_sheet_container"),
                color = DarkSurface.copy(alpha = 0.98f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Tactile Drag Handle Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(WhiteMuted.copy(alpha = 0.45f))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sliding Sheet Header with dynamic title and close button
                    SlidingSheetHeader(
                        activeSheet = activeSheet,
                        onDismiss = onDismiss
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Content based on active sliding drawer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (activeSheet) {
                            ActiveSlidingSheet.BRUSHES -> SlidingBrushesContent(viewModel)
                            ActiveSlidingSheet.LAYERS -> SlidingLayersContent(viewModel, onImportImage)
                            ActiveSlidingSheet.TOOLS -> SlidingToolsAndGuidesContent(viewModel, onOpenManhwaStudio, onImportImage)
                            ActiveSlidingSheet.COLOR -> SlidingColorPaletteContent(viewModel)
                            ActiveSlidingSheet.EXPORT -> SlidingExportContent(viewModel, onDismiss)
                            ActiveSlidingSheet.NONE -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlidingSheetHeader(
    activeSheet: ActiveSlidingSheet,
    onDismiss: () -> Unit
) {
    val (title, subtitle, icon) = when (activeSheet) {
        ActiveSlidingSheet.BRUSHES -> Triple("استوديو الفرش والأقلام", "مكتبة الفرش الاحترافية وضبط انسيابية الخط", Icons.Default.Brush)
        ActiveSlidingSheet.LAYERS -> Triple("إدارة الطبقات والمزج", "ترتيب، عتامة، وأنماط دمج الطبقات", Icons.Default.Layers)
        ActiveSlidingSheet.TOOLS -> Triple("أدوات واستوديو الرسم", "الشبكة الإرشادية، التناظر، وقلب اللوحة", Icons.Default.GridOn)
        ActiveSlidingSheet.COLOR -> Triple("لوحة الألوان والباليتات", "تحديد الألوان، مجموعات جاهزة، وتاريخ الاستخدام", Icons.Default.Palette)
        ActiveSlidingSheet.EXPORT -> Triple("تصدير ومشاركة العمل الفني", "حفظ بصيغ PNG، JPG، وفيديو MP4 عالي الجودة", Icons.Default.Share)
        ActiveSlidingSheet.NONE -> Triple("", "", Icons.Default.Brush)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WhitePure,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = WhitePure,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = WhiteMuted,
                    fontSize = 11.sp
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(34.dp)
                .background(DarkSurfaceElevated, CircleShape)
                .testTag("close_sliding_sheet_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = WhitePure,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 1. Sliding Brushes Content (Dynamic Category tabs & Stroke preview)
// -------------------------------------------------------------
@Composable
fun SlidingBrushesContent(viewModel: DrawingViewModel) {
    val brushConfig = viewModel.brushConfig.value
    var selectedCategory by remember { mutableStateOf(brushConfig.type.category) }

    val categoryBrushes = remember(selectedCategory) {
        BrushType.values().filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BrushCategory.values()) { category ->
                    val isSelected = category == selectedCategory
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) WhitePure else DarkSurfaceElevated)
                            .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("sliding_cat_${category.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.titleAr,
                            color = if (isSelected) Color(0xFF101014) else WhiteComfortable,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Live Dynamic Stroke Preview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معاينة حية للمسار والفرشاة",
                            color = WhiteMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${brushConfig.type.titleAr} • ${brushConfig.size.toInt()}px • ${(brushConfig.opacity * 100).toInt()}%",
                            color = WhitePure,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Canvas rendering live sample stroke curve
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBg)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val path = Path().apply {
                                moveTo(w * 0.08f, h * 0.5f)
                                cubicTo(
                                    w * 0.35f, h * 0.1f,
                                    w * 0.65f, h * 0.9f,
                                    w * 0.92f, h * 0.5f
                                )
                            }
                            drawPath(
                                path = path,
                                color = brushConfig.color.copy(alpha = brushConfig.opacity),
                                style = Stroke(
                                    width = brushConfig.size.coerceIn(2f, 48f),
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Size Pills
        item {
            Column {
                Text(text = "مقاسات سريعة جاهزة", color = WhiteMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(2f, 6f, 12f, 24f, 48f, 96f)
                    items(presets) { preset ->
                        val isSelected = brushConfig.size.toInt() == preset.toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WhitePure else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setBrushSize(preset) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset.toInt()}px",
                                color = if (isSelected) Color(0xFF101014) else WhitePure,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Sliders: Size, Opacity, Smoothing Level
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Size slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "حجم الخط", color = WhiteComfortable, fontSize = 12.sp)
                    Text(text = "${brushConfig.size.toInt()} px", color = WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = brushConfig.size,
                    onValueChange = { viewModel.setBrushSize(it) },
                    valueRange = 1f..200f,
                    colors = SliderDefaults.colors(thumbColor = WhitePure, activeTrackColor = WhitePure),
                    modifier = Modifier.testTag("sliding_brush_size_slider")
                )

                // Opacity slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "الشفافية والعتامة", color = WhiteComfortable, fontSize = 12.sp)
                    Text(text = "${(brushConfig.opacity * 100).toInt()}%", color = WhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = brushConfig.opacity,
                    onValueChange = { viewModel.setBrushOpacity(it) },
                    valueRange = 0.02f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = WhitePure, activeTrackColor = WhitePure),
                    modifier = Modifier.testTag("sliding_brush_opacity_slider")
                )

                // Ultra Smoothing Strength (1-Euro Filter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "قوة التنعيم الحريري (Catmull-Rom & 1-Euro)", color = WhiteComfortable, fontSize = 12.sp)
                    Text(text = "${(brushConfig.smoothing * 100).toInt()}%", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = brushConfig.smoothing,
                    onValueChange = { viewModel.setBrushSmoothing(it) },
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen),
                    modifier = Modifier.testTag("sliding_smoothing_slider")
                )
            }
        }

        // Brushes in active category
        item {
            Text(
                text = "اختر نوع الفرشاة (${categoryBrushes.size})",
                color = WhiteMuted,
                fontSize = 11.sp
            )
        }

        items(categoryBrushes) { brush ->
            val isCurrent = brush == brushConfig.type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCurrent) DarkSurfaceHighlight else DarkSurfaceElevated)
                    .border(1.dp, if (isCurrent) WhitePure else GrayBorderSubtle, RoundedCornerShape(16.dp))
                    .clickable { viewModel.setBrushType(brush) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("sliding_brush_${brush.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) WhitePure else DarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brush.category.icon,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = brush.titleAr,
                            color = WhitePure,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = brush.descriptionAr,
                            color = WhiteMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(AccentGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Sliding Layers Content (Blend modes, Opacity, Actions)
// -------------------------------------------------------------
@Composable
fun SlidingLayersContent(
    viewModel: DrawingViewModel,
    onImportImage: () -> Unit = {}
) {
    val layers = viewModel.getCurrentFrameLayers().asReversed()
    val activeLayerId = viewModel.activeLayerId.value

    val blendModes = listOf("عادي", "مضاعفة", "شاشة", "تراكب", "إضافة", "إضاءة")

    Column(modifier = Modifier.fillMaxSize()) {
        // Top action bar: New Layer and Import Photo buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الطبقات (${layers.size})",
                color = WhiteComfortable,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onImportImage,
                    border = BorderStroke(1.dp, GrayBorderComfortable),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                    modifier = Modifier.testTag("sliding_import_photo_btn")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "استيراد صورة", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { viewModel.addLayer() },
                    colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color(0xFF101014)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sliding_add_layer_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "طبقة جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Layers list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(layers) { _, layer ->
                val isActive = layer.id == activeLayerId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, if (isActive) WhitePure else GrayBorderSubtle, RoundedCornerShape(18.dp))
                        .clickable { viewModel.setActiveLayer(layer.id) }
                        .testTag("sliding_layer_card_${layer.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) DarkSurfaceHighlight else DarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Row 1: Visibility, Name, Active Chip, and Lock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleLayerVisibility(layer.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = if (layer.isVisible) WhitePure else WhiteMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = layer.name,
                                    color = WhitePure,
                                    fontSize = 14.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )

                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentGreen)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "نشطة", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleLayerLock(layer.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Toggle Lock",
                                        tint = if (layer.isLocked) Color(0xFFEF5350) else WhiteMuted,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Row 2: Opacity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الشفافية: ${(layer.opacity * 100).toInt()}%",
                                color = WhiteMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = layer.opacity,
                                onValueChange = { viewModel.setLayerOpacity(layer.id, it) },
                                valueRange = 0f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = WhitePure, activeTrackColor = WhitePure),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: Blend Mode Chips
                        Text(text = "نمط المزج والدمج:", color = WhiteMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(blendModes) { mode ->
                                val isSelected = layer.blendMode == mode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) WhitePure else DarkBg)
                                        .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setLayerBlendMode(layer.id, mode) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        color = if (isSelected) Color(0xFF101014) else WhiteComfortable,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 4: Layer Actions (Duplicate, Merge down, Delete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Duplicate
                            IconButton(
                                onClick = { viewModel.duplicateLayer(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Duplicate",
                                    tint = WhiteComfortable,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Merge Down
                            IconButton(
                                onClick = { viewModel.mergeLayerDown(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallMerge,
                                    contentDescription = "Merge Down",
                                    tint = WhiteComfortable,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Delete
                            if (layers.size > 1) {
                                IconButton(
                                    onClick = { viewModel.deleteLayer(layer.id) },
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
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Sliding Tools & Guides Content (Symmetry, Grid, Paper, Flip, Manhwa Studio)
// -------------------------------------------------------------
@Composable
fun SlidingToolsAndGuidesContent(
    viewModel: DrawingViewModel,
    onOpenManhwaStudio: () -> Unit = {},
    onImportImage: () -> Unit = {}
) {
    val showGrid = viewModel.showGrid.value
    val gridSize = viewModel.gridSize.value
    val symmetryMode = viewModel.symmetryMode.value
    val canvasPaper = viewModel.canvasPaper.value
    val flipH = viewModel.flipHorizontal.value
    val flipV = viewModel.flipVertical.value

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 0: Import Photo from Phone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "استيراد الصور مباشرة من الهاتف 📷", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(text = "جديد", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "استورد أي صورة من معرض هاتفك مباشرة إلى اللوحة الحالية كطبقة رسم جديدة للتعديل عليها.",
                        color = WhiteMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onImportImage,
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("tools_import_photo_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "اختيار صورة من المعرض وإدراجها", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 0.5: Manhwa & Giant Image Studio Launcher Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "أستوديو المانهوا والصور العملاقة ✂️", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(text = "v1.3.2", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(
                        text = "قص حر للصور حتى 800×10000+ بكسل بدون فقد + تجميع ذكي للصور بالاسم",
                        color = WhiteMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenManhwaStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "فتح أستوديو قص ودمج المانهوا", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        // Section 1: Intelligent Symmetry Guides
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "مرآة التناظر الذكية (Symmetry Engine)", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = symmetryMode.titleAr, color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "رسم متطابق فوري في نفس اللحظة للماندالا والوجوه والتصاميم الهندسية", color = WhiteMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SymmetryMode.values().forEach { mode ->
                            val isSelected = mode == symmetryMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) WhitePure else DarkBg)
                                    .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setSymmetryMode(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = mode.iconLabel,
                                        color = if (isSelected) Color(0xFF101014) else WhitePure,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mode.titleAr,
                                        color = if (isSelected) Color(0xFF101014) else WhiteMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Drawing Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "شبكة الرسم الإرشادية", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "تساعد في ضبط الأبعاد والنسب الصحيحة", color = WhiteMuted, fontSize = 11.sp)
                        }

                        Switch(
                            checked = showGrid,
                            onCheckedChange = { viewModel.toggleGrid() },
                            colors = SwitchDefaults.colors(checkedThumbColor = WhitePure, checkedTrackColor = AccentGreen)
                        )
                    }

                    if (showGrid) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "مقاس خلايا الشبكة", color = WhiteComfortable, fontSize = 11.sp)
                            Text(text = "${gridSize.toInt()} px", color = WhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = gridSize,
                            onValueChange = { viewModel.setGridSize(it) },
                            valueRange = 20f..100f,
                            colors = SliderDefaults.colors(thumbColor = WhitePure, activeTrackColor = WhitePure)
                        )
                    }
                }
            }
        }

        // Section 3: Canvas Paper Texture / Background Color
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "لون وخامة ورق اللوحة (Canvas Paper)", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "اختر لون الخلفية المناسب للرسم والرؤية المريحة", color = WhiteMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CanvasPaper.values().forEach { paper ->
                            val isSelected = paper == canvasPaper
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DarkSurfaceHighlight else DarkBg)
                                    .border(1.dp, if (isSelected) WhitePure else GrayBorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setCanvasPaper(paper) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(paper.color)
                                        .border(1.dp, GrayBorderActive, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = paper.titleAr,
                                    color = if (isSelected) WhitePure else WhiteMuted,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Canvas Transformation Quick Tools (Flip Horizontal/Vertical)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "معاينة اللوحة والقلب البصري", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "لقياس التوازن واكتشاف أخطاء الرسم والتشريح", color = WhiteMuted, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleFlipHorizontal() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (flipH) WhitePure else DarkBg,
                                contentColor = if (flipH) Color(0xFF101014) else WhitePure
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (flipH) "✓ مقلوب أفقياً" else "قلب أفقي ⇆", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.toggleFlipVertical() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (flipV) WhitePure else DarkBg,
                                contentColor = if (flipV) Color(0xFF101014) else WhitePure
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (flipV) "✓ مقلوب رأسياً" else "قلب رأسي ⇅", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Sliding Color Palette Content
// -------------------------------------------------------------
@Composable
fun SlidingColorPaletteContent(viewModel: DrawingViewModel) {
    val brushConfig = viewModel.brushConfig.value
    val recentColors = viewModel.recentColors.value

    var red by remember(brushConfig.color) { mutableFloatStateOf(brushConfig.color.red) }
    var green by remember(brushConfig.color) { mutableFloatStateOf(brushConfig.color.green) }
    var blue by remember(brushConfig.color) { mutableFloatStateOf(brushConfig.color.blue) }

    val curatedPalettes = remember {
        listOf(
            "باستيل" to listOf(Color(0xFFFFB3BA), Color(0xFFFFDFBA), Color(0xFFFFFFBA), Color(0xFFBAFFC9), Color(0xFFBAE1FF), Color(0xFFE8BAFF)),
            "بشرة" to listOf(Color(0xFF8D5524), Color(0xFFC68642), Color(0xFFE0AC69), Color(0xFFF1C27D), Color(0xFFFFDBAC)),
            "نيون" to listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFFFF007F), Color(0xFF390099), Color(0xFFFFE600)),
            "طبيعة" to listOf(Color(0xFF2B2D42), Color(0xFF8D99AE), Color(0xFF2D6A4F), Color(0xFF52B788), Color(0xFF74C69D), Color(0xFFD8F3DC)),
            "مانجا" to listOf(Color(0xFF000000), Color(0xFF2B2B2B), Color(0xFF545454), Color(0xFF8A8A8A), Color(0xFFC7C7C7), Color(0xFFFFFFFF))
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Color Swatch & Hex Display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(brushConfig.color)
                                .border(2.dp, WhitePure, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "اللون الحالي المختار",
                                color = WhiteMuted,
                                fontSize = 11.sp
                            )
                            val hex = String.format(
                                "#%02X%02X%02X",
                                (brushConfig.color.red * 255).toInt(),
                                (brushConfig.color.green * 255).toInt(),
                                (brushConfig.color.blue * 255).toInt()
                            )
                            Text(
                                text = hex,
                                color = WhitePure,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recent Colors History
        item {
            Column {
                Text(text = "آخر الألوان المستخدمة", color = WhiteMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (color == brushConfig.color) 2.5.dp else 1.dp,
                                    if (color == brushConfig.color) AccentGreen else GrayBorderComfortable,
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.setBrushColor(color)
                                    red = color.red
                                    green = color.green
                                    blue = color.blue
                                }
                        )
                    }
                }
            }
        }

        // RGB Sliders for precise tuning
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "معاير RGB الدقيق", color = WhitePure, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    // Red
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "R ${(red * 255).toInt()}", color = Color(0xFFFF5252), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                        Slider(
                            value = red,
                            onValueChange = {
                                red = it
                                viewModel.setBrushColor(Color(red, green, blue))
                            },
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252)),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Green
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "G ${(green * 255).toInt()}", color = Color(0xFF69F0AE), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                        Slider(
                            value = green,
                            onValueChange = {
                                green = it
                                viewModel.setBrushColor(Color(red, green, blue))
                            },
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF69F0AE), activeTrackColor = Color(0xFF69F0AE)),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Blue
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "B ${(blue * 255).toInt()}", color = Color(0xFF448AFF), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                        Slider(
                            value = blue,
                            onValueChange = {
                                blue = it
                                viewModel.setBrushColor(Color(red, green, blue))
                            },
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF448AFF), activeTrackColor = Color(0xFF448AFF)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Curated Artist Palettes
        item {
            Text(text = "باليتات متناسقة جاهزة للفنانين", color = WhiteMuted, fontSize = 11.sp)
        }

        items(curatedPalettes) { (name, colors) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = name, color = WhiteComfortable, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(c)
                                    .border(1.dp, GrayBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setBrushColor(c)
                                        red = c.red
                                        green = c.green
                                        blue = c.blue
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Sliding Export & Share Content
// -------------------------------------------------------------
@Composable
fun SlidingExportContent(
    viewModel: DrawingViewModel,
    onDismiss: () -> Unit
) {
    val exportProgress = viewModel.exportProgress.value
    val lastExportedFile = viewModel.lastExportedFile.value

    val formats = listOf(
        Triple(ExportFormat.PNG, "PNG فائق الدقة", "صورة عالية الوضوح تدعم الشفافية"),
        Triple(ExportFormat.JPG, "JPG عالي الجودة", "مثالي للمشاركة السريعة والشبكات الاجتماعية"),
        Triple(ExportFormat.WEBP, "WebP مدمج فائق الخفة", "حجم فائق الصغر مع جودة بصرية مذهلة"),
        Triple(ExportFormat.MP4, "فيديو MP4 متحرك", "تصدير جميع الإطارات والأنيميشن كفيديو HD")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (exportProgress != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentGreen, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "جارٍ تصدير العمل الفني...", color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentGreen,
                        trackColor = DarkBg
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${(exportProgress * 100).toInt()}%", color = AccentGreen, fontSize = 11.sp)
                }
            }
        }

        if (lastExportedFile != null && exportProgress == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentGreen, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "تم تجهيز الملف بنجاح! ✓", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = lastExportedFile.name, color = WhiteMuted, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            val mime = if (lastExportedFile.name.endsWith(".mp4")) "video/mp4" else "image/png"
                            viewModel.shareExportedFile(lastExportedFile, mime)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "مشاركة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(text = "اختر صيغة التصدير المطلوبة:", color = WhiteMuted, fontSize = 11.sp)

        formats.forEach { (format, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.exportCurrentArtwork(format) {}
                    },
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = title, color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = desc, color = WhiteMuted, fontSize = 11.sp)
                    }

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export",
                        tint = WhitePure,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
