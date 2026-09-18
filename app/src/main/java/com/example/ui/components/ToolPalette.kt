package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.GrayBorderActive
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.theme.WhiteSoft

/**
 * Reimagined, ultra-responsive Studio Footer Dock.
 * Features an ergonomic floating island with instant 0ms touch feedback,
 * tactile tool segment switchers, inline quick size adjusters, and chromatic color dial.
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
    val isBrushActive = !isEraserActive && !isSmudgeActive

    BoxWithConstraints(modifier = modifier) {
        val isWideScreen = maxWidth >= 640.dp
        val isCompactScreen = maxWidth < 420.dp

        Surface(
            modifier = Modifier
                .padding(
                    horizontal = if (isWideScreen) 24.dp else 8.dp,
                    vertical = 6.dp
                )
                .widthIn(max = 880.dp)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            WhitePure.copy(alpha = 0.22f),
                            GrayBorderComfortable.copy(alpha = 0.5f)
                        )
                    ),
                    RoundedCornerShape(32.dp)
                )
                .testTag("tool_palette_dock"),
            color = DarkSurface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = if (isWideScreen) 16.dp else 8.dp,
                        vertical = 6.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 10.dp else 6.dp)
            ) {
                // 1. HERO ACTIVE BRUSH CAPSULE (Click opens sliding brush studio)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isBrushActive) DarkSurfaceHighlight else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isBrushActive) WhitePure.copy(alpha = 0.85f) else GrayBorderSubtle,
                            RoundedCornerShape(22.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenBrushStudio() }
                        .padding(
                            horizontal = if (isWideScreen) 12.dp else 8.dp,
                            vertical = 6.dp
                        )
                        .testTag("brush_selector_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isBrushActive) WhitePure else DarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getBrushCategoryIcon(brushConfig.type.category),
                            contentDescription = "Brush Studio",
                            tint = if (isBrushActive) Color(0xFF101014) else WhitePure,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = brushConfig.type.titleAr,
                            color = WhitePure,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${brushConfig.size.toInt()}px • ${(brushConfig.opacity * 100).toInt()}%",
                            color = if (isBrushActive) AccentGreen else WhiteMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 2. TACTILE QUICK SIZE ADJUSTER
                if (isWideScreen) {
                    // Wide screen: smooth high-fidelity size slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp)
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
                    // Phone screen: micro-stepper buttons [-] size [+]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, GrayBorderSubtle, RoundedCornerShape(18.dp))
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
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
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "${brushConfig.size.toInt()}",
                            color = WhitePure,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(30.dp)
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
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // 3. TACTILE TOOL TRIO (Segmented control: Eraser & Smudge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    // Eraser Pill
                    val eraserBg by animateColorAsState(
                        targetValue = if (isEraserActive) WhitePure else Color.Transparent,
                        label = "eraserBg"
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Smudge / Blend Pill
                    val smudgeBg by animateColorAsState(
                        targetValue = if (isSmudgeActive) WhitePure else Color.Transparent,
                        label = "smudgeBg"
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4. CHROMATIC COLOR ORB (Concentric ring color dial)
                Box(
                    modifier = Modifier
                        .size(38.dp)
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
                        .padding(2.5.dp)
                        .testTag("color_swatch_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brushConfig.color)
                            .border(1.dp, Color(0xFF18181B), CircleShape)
                    )
                }

                // 5. LAYERS QUICK ACCESS BUTTON
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenLayers() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("bottom_layers_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                // 6. TOOLS & GUIDES BUTTON
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GrayBorderSubtle, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenTools() }
                        .testTag("bottom_tools_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Tools & Guides",
                        tint = WhitePure,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 7. CLEAR LAYER BUTTON
                Box(
                    modifier = Modifier
                        .size(34.dp)
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
                        modifier = Modifier.size(16.dp)
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
