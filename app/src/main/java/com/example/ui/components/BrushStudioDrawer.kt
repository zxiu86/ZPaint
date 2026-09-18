package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BrushCategory
import com.example.model.BrushConfig
import com.example.model.BrushType
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
import com.example.ui.theme.WhiteSoft

@Composable
fun BrushStudioDrawer(
    currentBrushConfig: BrushConfig,
    onSelectBrush: (BrushType) -> Unit,
    onSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentBrushConfig.type.category) }

    val categoryBrushes = remember(selectedCategory) {
        BrushType.values().filter { it.category == selectedCategory }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .border(1.dp, GrayBorderComfortable, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with studio title & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "استوديو الفرش والأقلام",
                            color = WhitePure,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مجموعة احترافية متكاملة (${BrushType.values().size} فرشاة متنوعة)",
                            color = WhiteMuted,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(DarkSurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = WhitePure,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Categories Row Tabs
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
                                .border(
                                    1.dp,
                                    if (isSelected) WhitePure else GrayBorderSubtle,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("brush_cat_${category.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.titleAr,
                                color = if (isSelected) Color(0xFF121216) else WhiteComfortable,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Brushes in this Category
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryBrushes) { brush ->
                        val isCurrent = brush == currentBrushConfig.type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCurrent) DarkSurfaceHighlight else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isCurrent) WhitePure else GrayBorderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onSelectBrush(brush)
                                    onDismiss()
                                }
                                .padding(14.dp)
                                .testTag("brush_item_${brush.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Stroke / Brush Icon Capsule
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrent) WhitePure else DarkBg)
                                        .border(1.dp, GrayBorderComfortable, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getBrushTypeIcon(brush),
                                        contentDescription = brush.titleAr,
                                        tint = if (isCurrent) Color(0xFF101014) else WhitePure,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = brush.titleAr,
                                            color = WhitePure,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (brush.category == BrushCategory.PAINT || brush.category == BrushCategory.SPLATTER) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Brusheezy",
                                                color = Color(0xFFA5D6A7),
                                                fontSize = 9.sp,
                                                modifier = Modifier
                                                    .background(Color(0x2281C784), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = brush.descriptionAr,
                                        color = WhiteMuted,
                                        fontSize = 11.sp,
                                        maxLines = 2
                                    )
                                }
                            }

                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(WhitePure, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF101014),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Size & Opacity Customization Bars at bottom of studio
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Size slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "الحجم: ${currentBrushConfig.size.toInt()}px",
                                color = WhiteComfortable,
                                fontSize = 11.sp,
                                modifier = Modifier.width(76.dp)
                            )
                            Slider(
                                value = currentBrushConfig.size,
                                onValueChange = onSizeChange,
                                valueRange = 1f..150f,
                                colors = SliderDefaults.colors(
                                    thumbColor = WhitePure,
                                    activeTrackColor = WhitePure,
                                    inactiveTrackColor = GrayBorderComfortable
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Opacity slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "الشفافية: ${(currentBrushConfig.opacity * 100).toInt()}%",
                                color = WhiteComfortable,
                                fontSize = 11.sp,
                                modifier = Modifier.width(76.dp)
                            )
                            Slider(
                                value = currentBrushConfig.opacity,
                                onValueChange = onOpacityChange,
                                valueRange = 0.05f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = WhitePure,
                                    activeTrackColor = WhitePure,
                                    inactiveTrackColor = GrayBorderComfortable
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getBrushTypeIcon(brush: BrushType): ImageVector {
    return when (brush.category) {
        BrushCategory.SKETCH -> Icons.Default.Edit
        BrushCategory.PAINT -> Icons.Default.Palette
        BrushCategory.SPLATTER -> Icons.Default.Grain
        BrushCategory.FX -> Icons.Default.AutoAwesome
        BrushCategory.UTILITY -> Icons.Default.Brush
    }
}
