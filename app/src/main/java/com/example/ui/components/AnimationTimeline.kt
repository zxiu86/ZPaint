package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationFrame
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
fun AnimationTimeline(
    frames: List<AnimationFrame>,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    onionSkinEnabled: Boolean,
    fps: Int,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: (Int) -> Unit,
    onDeleteFrame: (Int) -> Unit,
    onToggleOnionSkin: () -> Unit,
    onFpsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = DarkSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            // Controls bar (Play/Pause, Onion Skin, FPS, Add, Duplicate, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isPlaying) DarkSurfaceHighlight else WhitePure, CircleShape)
                        .testTag("play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause animation" else "Play animation",
                        tint = if (isPlaying) WhitePure else Color(0xFF101014),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Onion Skin Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (onionSkinEnabled) DarkSurfaceHighlight else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (onionSkinEnabled) WhitePure else GrayBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleOnionSkin() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("onion_skin_btn"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Onion Skin",
                        tint = if (onionSkinEnabled) WhitePure else WhiteMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Onion Skin",
                        color = if (onionSkinEnabled) WhitePure else WhiteMuted,
                        fontSize = 11.sp
                    )
                }

                // FPS indicator / slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$fps FPS",
                        color = WhiteComfortable,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = fps.toFloat(),
                        onValueChange = { onFpsChange(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = WhitePure,
                            activeTrackColor = WhitePure,
                            inactiveTrackColor = GrayBorderComfortable
                        ),
                        modifier = Modifier.width(90.dp)
                    )
                }

                // Frame management actions (Add, Duplicate, Delete)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Duplicate Frame
                    IconButton(
                        onClick = { onDuplicateFrame(currentFrameIndex) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate frame",
                            tint = WhiteComfortable,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete Frame (if more than 1 frame)
                    if (frames.size > 1) {
                        IconButton(
                            onClick = { onDeleteFrame(currentFrameIndex) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete frame",
                                tint = WhiteMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Add Frame
                    IconButton(
                        onClick = onAddFrame,
                        modifier = Modifier
                            .size(32.dp)
                            .background(DarkSurfaceHighlight, CircleShape)
                            .testTag("add_frame_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add frame",
                            tint = WhitePure,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Frames Scroll Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frames.forEachIndexed { index, frame ->
                    val isSelected = index == currentFrameIndex
                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) DarkSurfaceHighlight else DarkSurfaceElevated)
                            .border(
                                1.5.dp,
                                if (isSelected) WhitePure else GrayBorderSubtle,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectFrame(index) }
                            .testTag("frame_thumb_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${index + 1}",
                                color = if (isSelected) WhitePure else WhiteMuted,
                                fontSize = 13.sp
                            )
                            val totalStrokes = frame.layers.sumOf { it.strokes.size }
                            Text(
                                text = "$totalStrokes خط",
                                color = WhiteMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
