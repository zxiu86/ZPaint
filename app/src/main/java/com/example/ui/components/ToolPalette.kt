package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Compact, sleek Studio Footer Dock.
 * Sized appropriately without bloated dimensions.
 * Includes active brush preview, micro-size stepper, eraser/smudge, color orb,
 * clear action, and the dedicated settings menu button that opens the bottom drawer.
 */
@Composable
fun ToolPalette(
    brushConfig: BrushConfig,
    onOpenBrushStudio: () -> Unit,
    onOpenColorPicker: () -> Unit,
    onToggleEraser: () -> Unit,
    onToggleSmudge: () -> Unit,
    onClearLayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuickSizeChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEraserActive = brushConfig.type == BrushType.ERASER || brushConfig.type == BrushType.ERASER_SOFT
    val isSmudgeActive = brushConfig.type == BrushType.SMUDGE
    val isBrushActive = !isEraserActive && !isSmudgeActive

    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth >= 600.dp
        val isCompactScreen = maxWidth < 380.dp

        Surface(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .widthIn(max = 760.dp)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            WhitePure.copy(alpha = 0.20f),
                            GrayBorderComfortable.copy(alpha = 0.45f)
                        )
                    ),
                    RoundedCornerShape(26.dp)
                )
                .testTag("tool_palette_dock"),
            color = DarkSurface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 8.dp else 5.dp)
            ) {
                // 1. COMPACT ACTIVE BRUSH CAPSULE
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isBrushActive) DarkSurfaceHighlight else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isBrushActive) WhitePure.copy(alpha = 0.8f) else GrayBorderSubtle,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenBrushStudio() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("brush_selector_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isBrushActive) WhitePure else DarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getBrushCategoryIcon(brushConfig.type.category),
                            contentDescription = "Brush Studio",
                            tint = if (isBrushActive) Color(0xFF101014) else WhitePure,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    Column {
                        Text(
                            text = brushConfig.type.titleAr,
                            color = WhitePure,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${brushConfig.size.toInt()}px • ${(brushConfig.opacity * 100).toInt()}%",
                            color = if (isBrushActive) AccentGreen else WhiteMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 2. COMPACT TACTILE SIZE STEPPER
                if (isWideScreen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp)
                    ) {
                        Slider(
                            value = brushConfig.size,
                            onValueChange = onQuickSizeChange,
                            valueRange = 1f..150f,
                            colors = SliderDefaults.colors(
                                thumbColor = WhitePure,
                                activeTrackColor = WhitePure,
                                inactiveTrackColor = DarkSurfaceHighlight
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (!isCompactScreen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) {
                                    val newSize = (brushConfig.size - 4f).coerceAtLeast(1f)
                                    onQuickSizeChange(newSize)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease size",
                                tint = WhiteComfortable,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "${brushConfig.size.toInt()}",
                            color = WhitePure,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) {
                                    val newSize = (brushConfig.size + 4f).coerceAtMost(150f)
                                    onQuickSizeChange(newSize)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase size",
                                tint = WhiteComfortable,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // 3. COMPACT ERASER & SMUDGE TRIO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    val eraserBg by animateColorAsState(
                        targetValue = if (isEraserActive) WhitePure else Color.Transparent,
                        label = "eraserBg"
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(eraserBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onToggleEraser() }
                            .testTag("eraser_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⌫",
                            color = if (isEraserActive) Color(0xFF101014) else WhiteComfortable,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val smudgeBg by animateColorAsState(
                        targetValue = if (isSmudgeActive) WhitePure else Color.Transparent,
                        label = "smudgeBg"
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(smudgeBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onToggleSmudge() }
                            .testTag("smudge_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "≈",
                            color = if (isSmudgeActive) Color(0xFF101014) else WhiteComfortable,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4. COMPACT CHROMATIC COLOR ORB
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    WhitePure,
                                    Color(0xFF888888),
                                    WhitePure,
                                    Color(0xFF555555),
                                    WhitePure
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenColorPicker() }
                        .padding(2.dp)
                        .testTag("color_swatch_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(brushConfig.color)
                            .border(1.dp, Color(0xFF18181B), CircleShape)
                    )
                }

                // 5. COMPACT CLEAR LAYER BUTTON
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onClearLayer() }
                        .testTag("clear_layer_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear layer",
                        tint = WhiteMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // 6. DEDICATED STUDIO SETTINGS MENU BUTTON (Three straight lines / Hamburger icon - no text)
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceHighlight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenSettings() }
                        .testTag("open_settings_sheet_btn")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "قائمة الإعدادات والاستوديو",
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
