package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure

/**
 * Minimalist, ultra-clean Studio Header.
 * Kept exclusively to the essentials: Navigation Back, Undo, and Redo.
 * All studio tools, layers, and configuration are accessed via the dedicated bottom drawer.
 */
@Composable
fun StudioHeader(
    canUndo: Boolean,
    canRedo: Boolean,
    onNavigateBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("top_header_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Back to Home Navigation Button
        Surface(
            color = DarkSurface.copy(alpha = 0.92f),
            shape = CircleShape,
            border = BorderStroke(1.dp, GrayBorderComfortable),
            shadowElevation = 8.dp
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .testTag("nav_home_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "العودة للرئيسية",
                    tint = WhitePure,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // 2. Undo & Redo Capsule (زري التراجع والتقدم)
        Surface(
            color = DarkSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, GrayBorderComfortable),
            shadowElevation = 8.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                // Undo Button (تراجع)
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("undo_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "تراجع",
                        tint = if (canUndo) WhitePure else WhiteMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(GrayBorderSubtle)
                )

                // Redo Button (تقدم / إعادة)
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("redo_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "تقدم / إعادة",
                        tint = if (canRedo) WhitePure else WhiteMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
