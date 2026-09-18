package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.GrayBorderSubtle
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure
import com.example.ui.theme.WhiteSoft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert initial color to HSV
    val hsv = remember(initialColor) {
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.argb(
                (initialColor.alpha * 255).toInt(),
                (initialColor.red * 255).toInt(),
                (initialColor.green * 255).toInt(),
                (initialColor.blue * 255).toInt()
            ),
            array
        )
        array
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }

    val currentColor = remember(hue, saturation, value, alpha) {
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
        Color(argb).copy(alpha = alpha)
    }

    val presetSwatches = listOf(
        Color(0xFFFFFFFF), Color(0xFFF0F0F5), Color(0xFFD4D4DC), Color(0xFF8E8E9A),
        Color(0xFF4A4A58), Color(0xFF1E1E24), Color(0xFF000000), Color(0xFFFF3B30),
        Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF34C759), Color(0xFF00C7BE),
        Color(0xFF30B0C7), Color(0xFF007AFF), Color(0xFF5856D6), Color(0xFFAF52DE),
        Color(0xFFFF2D55), Color(0xFFA2845E)
    )

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
                Text(
                    text = "اختيار اللون المخصص",
                    color = WhitePure,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Current Color Preview Box with Hex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(currentColor)
                            .border(2.dp, WhitePure, RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = String.format("#%02X%02X%02X",
                                (currentColor.red * 255).toInt(),
                                (currentColor.green * 255).toInt(),
                                (currentColor.blue * 255).toInt()
                            ),
                            color = WhitePure,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الشفافية: ${(alpha * 100).toInt()}%",
                            color = WhiteMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hue Slider with Rainbow Gradient Track
                Text(text = "درجة اللون (Hue)", color = WhiteComfortable, fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = WhitePure,
                            activeTrackColor = WhitePure,
                            inactiveTrackColor = GrayBorderComfortable
                        ),
                        modifier = Modifier.testTag("hue_slider")
                    )
                }

                // Saturation Slider
                Text(text = "التشبع (Saturation)", color = WhiteComfortable, fontSize = 12.sp)
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = WhitePure,
                        activeTrackColor = WhitePure,
                        inactiveTrackColor = GrayBorderComfortable
                    )
                )

                // Value / Brightness Slider
                Text(text = "السطوع (Brightness)", color = WhiteComfortable, fontSize = 12.sp)
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = WhitePure,
                        activeTrackColor = WhitePure,
                        inactiveTrackColor = GrayBorderComfortable
                    )
                )

                // Preset Swatches Grid
                Text(
                    text = "الألوان الجاهزة",
                    color = WhiteComfortable,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetSwatches.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(preset)
                                .border(1.dp, GrayBorderComfortable, CircleShape)
                                .clickable {
                                    val arr = FloatArray(3)
                                    android.graphics.Color.colorToHSV(
                                        android.graphics.Color.rgb(
                                            (preset.red * 255).toInt(),
                                            (preset.green * 255).toInt(),
                                            (preset.blue * 255).toInt()
                                        ),
                                        arr
                                    )
                                    hue = arr[0]
                                    saturation = arr[1]
                                    value = arr[2]
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Text(text = "إلغاء", color = WhiteComfortable)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhitePure),
                        modifier = Modifier.testTag("apply_color_btn")
                    ) {
                        Text(text = "تطبيق اللون", color = Color(0xFF101014))
                    }
                }
            }
        }
    }
}
