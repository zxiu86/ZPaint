package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.CloudSyncState
import com.example.model.ActiveSlidingSheet
import com.example.model.SymmetryMode
import com.example.ui.theme.AccentGreen
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
 * Reimagined, high-craft Studio Header with sculpted floating glassmorphic architecture.
 * Adapts seamlessly between Compact (phones) and Expanded (foldables, tablets) screens.
 */
@Composable
fun StudioHeader(
    projectTitle: String,
    canvasWidth: Int,
    canvasHeight: Int,
    syncState: CloudSyncState,
    canUndo: Boolean,
    canRedo: Boolean,
    showQuickSliders: Boolean,
    showAnimationTimeline: Boolean,
    activeSlidingSheet: ActiveSlidingSheet,
    showGrid: Boolean,
    symmetryMode: SymmetryMode,
    layerCount: Int,
    frameCount: Int,
    currentFrameIndex: Int,
    isWideScreen: Boolean,
    onNavigateBack: () -> Unit,
    onOpenGallery: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleQuickSliders: () -> Unit,
    onToggleAnimationTimeline: () -> Unit,
    onOpenToolsSheet: () -> Unit,
    onOpenLayersSheet: () -> Unit,
    onOpenExportSheet: () -> Unit,
    onImportImage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 940.dp)
            .padding(
                horizontal = if (isWideScreen) 20.dp else 8.dp,
                vertical = 6.dp
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        WhitePure.copy(alpha = 0.22f),
                        GrayBorderComfortable.copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .testTag("top_header_bar"),
        color = DarkSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isWideScreen) 12.dp else 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. LEFT POD: Back Button & Project Identity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
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
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenGallery() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("open_gallery_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2029)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "المشاريع",
                            tint = Color(0xFFFFD56B),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = projectTitle,
                            color = WhitePure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing sync dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(if (syncState == CloudSyncState.SYNCING) pulseScale else 1.0f)
                                    .clip(CircleShape)
                                    .background(
                                        when (syncState) {
                                            CloudSyncState.SYNCED -> AccentGreen
                                            CloudSyncState.SYNCING -> Color(0xFF42A5F5)
                                            else -> WhiteMuted
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when (syncState) {
                                    CloudSyncState.SYNCED -> "سحابي"
                                    CloudSyncState.SYNCING -> "مزامنة..."
                                    else -> "محلي"
                                },
                                color = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (isWideScreen) {
                                Text(
                                    text = " • ${canvasWidth}×${canvasHeight}",
                                    color = WhiteMuted.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. CENTER POD: Undo, Redo & Quick Controls Segment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                    .border(1.dp, GrayBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                // Undo Button
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .testTag("undo_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) WhitePure else WhiteMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(GrayBorderSubtle)
                )

                // Redo Button
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .testTag("redo_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) WhitePure else WhiteMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(GrayBorderSubtle)
                )

                // Quick Sliders Rail Toggle
                IconButton(
                    onClick = onToggleQuickSliders,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (showQuickSliders) DarkSurfaceHighlight else Color.Transparent)
                        .testTag("quick_sliders_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Quick Sliders",
                        tint = if (showQuickSliders) AccentGreen else WhiteComfortable,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 3. RIGHT POD: Studio Modules & Hero Export Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 6.dp else 4.dp)
            ) {
                // Animation Timeline Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (showAnimationTimeline) DarkSurfaceHighlight else DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(
                            1.dp,
                            if (showAnimationTimeline) AccentGreen.copy(alpha = 0.6f) else GrayBorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onToggleAnimationTimeline() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("animation_toggle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Animation Timeline",
                            tint = if (showAnimationTimeline) AccentGreen else WhiteComfortable,
                            modifier = Modifier.size(17.dp)
                        )
                        if (frameCount > 1 || isWideScreen) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentFrameIndex + 1}/$frameCount",
                                color = if (showAnimationTimeline) AccentGreen else WhiteComfortable,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tools & Guides Sheet Trigger (with indicator dot if active)
                val isGuidesActive = showGrid || symmetryMode != SymmetryMode.NONE
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (activeSlidingSheet == ActiveSlidingSheet.TOOLS) DarkSurfaceHighlight else DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(
                            1.dp,
                            if (isGuidesActive) AccentGreen.copy(alpha = 0.7f) else GrayBorderSubtle,
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenToolsSheet() }
                        .testTag("header_tools_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Tools and Guides",
                        tint = if (isGuidesActive) AccentGreen else WhitePure,
                        modifier = Modifier.size(18.dp)
                    )
                    if (isGuidesActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 5.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                    }
                }

                // Layers Sheet Trigger with Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (activeSlidingSheet == ActiveSlidingSheet.LAYERS) DarkSurfaceHighlight else DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(1.dp, GrayBorderSubtle, RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenLayersSheet() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("layers_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "الطبقات",
                            tint = WhitePure,
                            modifier = Modifier.size(17.dp)
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

                // Direct Photo Import Action Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(1.dp, GrayBorderSubtle, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onImportImage() }
                        .testTag("header_import_image_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "استيراد صورة من الهاتف",
                        tint = AccentGreen,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // HERO EXPORT & SHARE BUTTON
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(WhitePure, Color(0xFFEDEDED))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onOpenExportSheet() }
                        .padding(
                            horizontal = if (isWideScreen) 14.dp else 10.dp,
                            vertical = 7.dp
                        )
                        .testTag("export_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export & Share",
                            tint = Color(0xFF101014),
                            modifier = Modifier.size(16.dp)
                        )
                        if (isWideScreen) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تصدير",
                                color = Color(0xFF101014),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
