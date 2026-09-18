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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.theme.WhiteSoft
import com.example.util.ExportFormat
import java.io.File

@Composable
fun ExportDialog(
    exportProgress: Float?,
    lastExportedFile: File?,
    onExport: (ExportFormat) -> Unit,
    onShare: (File, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }

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
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "تصدير ومشاركة العمل الفني",
                    color = WhitePure,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Format Options
                val formats = listOf(
                    ExportFormat.PNG to "PNG (مع الشفافية)",
                    ExportFormat.JPG to "JPG (عالي الجودة)",
                    ExportFormat.WEBP to "WebP (فائق الضغط)",
                    ExportFormat.MP4 to "فيديو MP4 (أنيميشن)"
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    formats.forEach { (fmt, label) ->
                        val isSelected = selectedFormat == fmt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) DarkSurfaceHighlight else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) WhitePure else GrayBorderSubtle,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedFormat = fmt }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("export_option_${fmt.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (fmt == ExportFormat.MP4) Icons.Default.Movie else Icons.Default.Photo,
                                    contentDescription = fmt.name,
                                    tint = if (isSelected) WhitePure else WhiteMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = label,
                                        color = if (isSelected) WhitePure else WhiteComfortable,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = fmt.mimeType,
                                        color = WhiteMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = WhitePure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Indicator when exporting
                if (exportProgress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "جارٍ التصدير والمعالجة... ${(exportProgress * 100).toInt()}%",
                            color = WhiteComfortable,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            color = WhitePure,
                            trackColor = GrayBorderComfortable,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // File ready for sharing
                if (lastExportedFile != null && exportProgress == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تم الحفظ بنجاح!",
                                color = Color(0xFF81C784),
                                fontSize = 13.sp
                            )
                            Text(
                                text = lastExportedFile.name,
                                color = WhiteMuted,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { onShare(lastExportedFile, selectedFormat.mimeType) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhitePure),
                            modifier = Modifier.testTag("share_file_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF101014),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "مشاركة", color = Color(0xFF101014), fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Text(text = "إغلاق", color = WhiteComfortable)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onExport(selectedFormat) },
                        enabled = exportProgress == null,
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure),
                        modifier = Modifier.testTag("start_export_btn")
                    ) {
                        Text(text = "تصدير الآن", color = Color(0xFF101014))
                    }
                }
            }
        }
    }
}
