package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayBorderComfortable
import com.example.ui.theme.WhiteComfortable
import com.example.ui.theme.WhiteMuted
import com.example.ui.theme.WhitePure

@Composable
fun QuickSlidersRail(
    brushSize: Float,
    brushOpacity: Float,
    onSizeChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(1.dp, GrayBorderComfortable, RoundedCornerShape(20.dp)),
        color = DarkSurface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Size Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(180.dp)
            ) {
                Text(text = "الحجم", color = WhiteComfortable, fontSize = 11.sp)
                Text(text = "${brushSize.toInt()}px", color = WhitePure, fontSize = 11.sp)
            }
            Slider(
                value = brushSize,
                onValueChange = onSizeChange,
                valueRange = 1f..150f,
                colors = SliderDefaults.colors(
                    thumbColor = WhitePure,
                    activeTrackColor = WhitePure,
                    inactiveTrackColor = GrayBorderComfortable
                ),
                modifier = Modifier.width(180.dp).testTag("quick_size_slider")
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Opacity Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.width(180.dp)
            ) {
                Text(text = "الشفافية", color = WhiteComfortable, fontSize = 11.sp)
                Text(text = "${(brushOpacity * 100).toInt()}%", color = WhitePure, fontSize = 11.sp)
            }
            Slider(
                value = brushOpacity,
                onValueChange = onOpacityChange,
                valueRange = 0.05f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = WhitePure,
                    activeTrackColor = WhitePure,
                    inactiveTrackColor = GrayBorderComfortable
                ),
                modifier = Modifier.width(180.dp).testTag("quick_opacity_slider")
            )
        }
    }
}
