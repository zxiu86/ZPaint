package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.BrushConfig
import com.example.model.BrushType
import com.example.ui.components.ToolPalette
import com.example.ui.theme.ZPaintTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      ZPaintTheme {
        ToolPalette(
          brushConfig = BrushConfig(
            type = BrushType.PEN,
            size = 12f,
            opacity = 1f,
            color = Color.White
          ),
          onOpenBrushStudio = {},
          onToggleEraser = {},
          onToggleSmudge = {},
          onOpenColorPicker = {},
          onClearLayer = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
