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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DrawingLayer
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
fun LayersDialog(
    layers: List<DrawingLayer>,
    activeLayerId: String,
    onSelectLayer: (String) -> Unit,
    onAddLayer: () -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onOpacityChange: (String, Float) -> Unit,
    onMergeDown: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Show top layers first (reverse order)
    val reversedLayers = layers.asReversed()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GrayBorderComfortable, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الطبقات (${layers.size})",
                        color = WhitePure,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = onAddLayer,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        modifier = Modifier.testTag("add_layer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Layer",
                            tint = WhitePure,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "طبقة جديدة", color = WhitePure, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Layers List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(reversedLayers) { _, layer ->
                        val isActive = layer.id == activeLayerId

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isActive) DarkSurfaceHighlight else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isActive) WhitePure else GrayBorderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectLayer(layer.id) }
                                .padding(12.dp)
                                .testTag("layer_item_${layer.id}")
                        ) {
                            // Layer Title & Quick Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Visibility Eye
                                    IconButton(
                                        onClick = { onToggleVisibility(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = if (layer.isVisible) WhitePure else WhiteMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Lock Icon
                                    IconButton(
                                        onClick = { onToggleLock(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "Toggle Lock",
                                            tint = if (layer.isLocked) Color(0xFFFFB74D) else WhiteMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = layer.name,
                                        color = if (isActive) WhitePure else WhiteComfortable,
                                        fontSize = 14.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Merge Down
                                    val origIndex = layers.indexOfFirst { it.id == layer.id }
                                    if (origIndex > 0) {
                                        IconButton(
                                            onClick = { onMergeDown(layer.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CallMerge,
                                                contentDescription = "Merge down",
                                                tint = WhiteMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Duplicate
                                    IconButton(
                                        onClick = { onDuplicateLayer(layer.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Duplicate layer",
                                            tint = WhiteMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete (if more than 1 layer)
                                    if (layers.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteLayer(layer.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete layer",
                                                tint = WhiteMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Layer Opacity Slider (Accurate control)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الشفافية: ${(layer.opacity * 100).toInt()}%",
                                    color = WhiteMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(84.dp)
                                )
                                Slider(
                                    value = layer.opacity,
                                    onValueChange = { onOpacityChange(layer.id, it) },
                                    valueRange = 0f..1f,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WhitePure)
                ) {
                    Text(text = "تم", color = Color(0xFF101014))
                }
            }
        }
    }
}
