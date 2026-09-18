package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.ProjectEntity
import com.example.data.sync.CloudSyncState
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.theme.WhiteSoft
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsGalleryDialog(
    projects: List<ProjectEntity>,
    currentProjectId: String,
    syncState: CloudSyncState,
    onSelectProject: (String) -> Unit,
    onCreateNewProject: (String, Int, Int) -> Unit,
    onDeleteProject: (String) -> Unit,
    onTriggerSync: () -> Unit,
    onDismiss: () -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }

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
                // Top Header with Cloud Sync Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "معرض الأعمال والمشاريع",
                            color = WhitePure,
                            fontSize = 18.sp
                        )
                        // Cloud sync status text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable { onTriggerSync() }
                        ) {
                            Icon(
                                imageVector = when (syncState) {
                                    CloudSyncState.SYNCED -> Icons.Default.CloudDone
                                    CloudSyncState.SYNCING -> Icons.Default.CloudSync
                                    else -> Icons.Default.Sync
                                },
                                contentDescription = "Sync",
                                tint = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteComfortable,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (syncState) {
                                    CloudSyncState.SYNCED -> "المزامنة السحابية نشطة (محفوظ)"
                                    CloudSyncState.SYNCING -> "جارٍ المزامنة السحابية..."
                                    else -> "محفوظ محلياً (انقر للمزامنة)"
                                },
                                color = if (syncState == CloudSyncState.SYNCED) AccentGreen else WhiteMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // New Project Button
                    Button(
                        onClick = { showNewProjectDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure),
                        modifier = Modifier.testTag("create_new_canvas_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project",
                            tint = Color(0xFF101014),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "لوحة جديدة", color = Color(0xFF101014), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Projects Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    items(projects) { proj ->
                        val isCurrent = proj.id == currentProjectId
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(proj.updatedAt))

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCurrent) DarkSurfaceHighlight else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isCurrent) WhitePure else GrayBorderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onSelectProject(proj.id)
                                    onDismiss()
                                }
                                .padding(10.dp)
                                .testTag("project_card_${proj.id}")
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                val thumbFile = proj.thumbnailPath?.let { File(it) }
                                if (thumbFile != null && thumbFile.exists()) {
                                    val bitmap = BitmapFactory.decodeFile(thumbFile.absolutePath)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = proj.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().height(100.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "لوحة رسم",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = proj.title,
                                        color = WhitePure,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${proj.width}x${proj.height}",
                                        color = WhiteMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = formattedDate,
                                        color = WhiteMuted,
                                        fontSize = 9.sp
                                    )
                                }

                                if (projects.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteProject(proj.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete project",
                                            tint = WhiteMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(text = "إغلاق", color = WhiteComfortable)
                }
            }
        }
    }

    // Modal to create new canvas with preset sizes
    if (showNewProjectDialog) {
        Dialog(onDismissRequest = { showNewProjectDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorderComfortable, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "اختر مقاس اللوحة الجديدة",
                        color = WhitePure,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val presets = listOf(
                        Triple("مربع (Square 1:1)", 1080, 1080),
                        Triple("قصة وتيك توك (Story 9:16)", 1080, 1920),
                        Triple("شاشة عريضة (Landscape 16:9)", 1920, 1080),
                        Triple("دقة عالية (Full HD+ 4:3)", 1440, 1080)
                    )

                    presets.forEach { (name, w, h) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .clickable {
                                    onCreateNewProject(
                                        "عمل فني ${System.currentTimeMillis() % 1000}",
                                        w,
                                        h
                                    )
                                    showNewProjectDialog = false
                                    onDismiss()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, color = WhitePure, fontSize = 13.sp)
                            Text(text = "${w}x${h}", color = WhiteMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
