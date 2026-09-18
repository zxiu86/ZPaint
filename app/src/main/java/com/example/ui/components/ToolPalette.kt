package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrushCategory
import com.example.model.BrushConfig
import com.example.model.BrushType
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure

/**
 * Adaptive, responsive floating studio dock:
 * Automatically scales, expands and condenses based on screen size (phone vs tablet).
 */
@Composable
fun ToolPalette(
    brushConfig: BrushConfig,
    layerCount: Int = 1,
    onOpenBrushStudio: () -> Unit,
    onOpenLayers: () -> Unit = {},
    onOpenTools: () -> Unit = {},
    onOpenColorPicker: () -> Unit,
    onToggleEraser: () -> Unit,
    onToggleSmudge: () -> Unit,
    onClearLayer: () -> Unit,
    onQuickSizeChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEraserActive = brushConfig.type == BrushType.ERASER || brushConfig.type == BrushType.ERASER_SOFT
    val isSmudgeActive = brushConfig.type == BrushType.SMUDGE

    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth >= 620.dp

        Surface(
            modifier = Modifier
                .padding(horizontal = if (isWideScreen) 24.dp else 12.dp, vertical = 8.dp)
                .widthIn(max = 840.dp)
                .border(1.dp, GrayBorderComfortable, RoundedCornerShape(26.dp))
                .testTag("tool_palette_dock"),
            color = DarkSurface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = if (isWideScreen) 16.dp else 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 12.dp else 8.dp)
            ) {
                // 1. Main Active Brush Pill (Click opens sliding brush studio)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (!isEraserActive && !isSmudgeActive) DarkSurfaceHighlight else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (!isEraserActive && !isSmudgeActive) WhitePure else GrayBorderSubtle,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { onOpenBrushStudio() }
                        .padding(horizontal = if (isWideScreen) 14.dp else 10.dp, vertical = 7.dp)
                        .testTag("brush_selector_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getBrushCategoryIcon(brushConfig.type.category),
                        contentDescription = "Brush Studio",
                        tint = WhitePure,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = brushConfig.type.titleAr,
                            color = WhitePure,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${brushConfig.size.toInt()}px • ${(brushConfig.opacity * 100).toInt()}%",
                            color = WhiteMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // If wide screen: inline quick size slider
                if (isWideScreen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(130.dp)
                    ) {
                        Slider(
                            value = brushConfig.size,
                            onValueChange = onQuickSizeChange,
                            valueRange = 1f..150f,
                            colors = SliderDefaults.colors(thumbColor = WhitePure, activeTrackColor = WhitePure),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Eraser Tool Pill
                IconButton(
                    onClick = onToggleEraser,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isEraserActive) WhitePure else DarkSurfaceElevated)
                        .border(1.dp, if (isEraserActive) WhitePure else GrayBorderSubtle, CircleShape)
                        .testTag("eraser_btn")
                ) {
                    Text(
                        text = "⌫",
                        color = if (isEraserActive) Color(0xFF101014) else WhiteComfortable,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. Smudge Tool Pill
                IconButton(
                    onClick = onToggleSmudge,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSmudgeActive) WhitePure else DarkSurfaceElevated)
                        .border(1.dp, if (isSmudgeActive) WhitePure else GrayBorderSubtle, CircleShape)
                        .testTag("smudge_btn")
                ) {
                    Text(
                        text = "≈",
                        color = if (isSmudgeActive) Color(0xFF101014) else WhiteComfortable,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 4. Color Swatch with Ring (Click opens sliding color palette)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brushConfig.color)
                        .border(2.5.dp, WhitePure, CircleShape)
                        .clickable { onOpenColorPicker() }
                        .testTag("color_swatch_btn")
                )

                // 5. Layers Sheet Button with Layer Count Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onOpenLayers() }
                        .padding(horizontal = 8.dp, vertical = 7.dp)
                        .testTag("bottom_layers_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = WhitePure,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$layerCount",
                        color = WhitePure,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 6. Tools & Guides Sliding Drawer Button
                IconButton(
                    onClick = onOpenTools,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, CircleShape)
                        .testTag("bottom_tools_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Tools & Guides",
                        tint = WhitePure,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 7. Clear Layer quick button
                IconButton(
                    onClick = onClearLayer,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("clear_layer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear layer",
                        tint = WhiteMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

fun getBrushCategoryIcon(category: BrushCategory): ImageVector {
    return when (category) {
        BrushCategory.SKETCH -> Icons.Default.Edit
        BrushCategory.PAINT -> Icons.Default.Palette
        BrushCategory.SPLATTER -> Icons.Default.Grain
        BrushCategory.FX -> Icons.Default.AutoAwesome
        BrushCategory.UTILITY -> Icons.Default.Brush
    }
}
