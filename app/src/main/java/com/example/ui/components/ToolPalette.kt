package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun ToolPalette(
    brushConfig: BrushConfig,
    onOpenBrushStudio: () -> Unit,
    onToggleEraser: () -> Unit,
    onToggleSmudge: () -> Unit,
    onOpenColorPicker: () -> Unit,
    onClearLayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEraserActive = brushConfig.type == BrushType.ERASER || brushConfig.type == BrushType.ERASER_SOFT
    val isSmudgeActive = brushConfig.type == BrushType.SMUDGE

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(26.dp)),
        color = DarkSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Active Brush Capsule (Tapping opens the full Brusheezy Studio Drawer)
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
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("brush_selector_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getBrushCategoryIcon(brushConfig.type.category),
                    contentDescription = "Brush Studio",
                    tint = WhitePure,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = brushConfig.type.titleAr,
                        color = WhitePure,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${brushConfig.size.toInt()}px • ${(brushConfig.opacity * 100).toInt()}%",
                        color = WhiteMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Eraser Tool Switch
            IconButton(
                onClick = onToggleEraser,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isEraserActive) WhitePure else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (isEraserActive) WhitePure else GrayBorderSubtle,
                        CircleShape
                    )
                    .testTag("eraser_btn")
            ) {
                Text(
                    text = "⌫",
                    color = if (isEraserActive) Color(0xFF101014) else WhiteComfortable,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Smudge Blender Switch
            IconButton(
                onClick = onToggleSmudge,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSmudgeActive) WhitePure else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (isSmudgeActive) WhitePure else GrayBorderSubtle,
                        CircleShape
                    )
                    .testTag("smudge_btn")
            ) {
                Text(
                    text = "≈",
                    color = if (isSmudgeActive) Color(0xFF101014) else WhiteComfortable,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Color Swatch with outer platinum ring
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(brushConfig.color)
                    .border(2.5.dp, WhitePure, CircleShape)
                    .clickable { onOpenColorPicker() }
                    .testTag("color_swatch_btn")
            )

            // Clear Layer Trash Can
            IconButton(
                onClick = onClearLayer,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("clear_layer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear layer",
                    tint = WhiteMuted,
                    modifier = Modifier.size(18.dp)
                )
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
