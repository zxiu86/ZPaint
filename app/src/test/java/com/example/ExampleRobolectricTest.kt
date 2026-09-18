package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectSerializer
import com.example.engine.UltraSmoothStrokeEngine
import com.example.model.AnimationFrame
import com.example.model.BrushType
import com.example.model.DrawingLayer
import com.example.model.Project
import com.example.model.TouchPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZPaint", appName)
  }

  @Test
  fun `verify brush types count`() {
    assertTrue("Should have extensive brushes library", BrushType.values().size >= 10)
  }

  @Test
  fun `verify ultra smooth stroke engine path generation`() {
    val rawPoints = listOf(
      TouchPoint(0f, 0f, 1f),
      TouchPoint(50f, 60f, 1f),
      TouchPoint(120f, 100f, 1f),
      TouchPoint(200f, 220f, 1f)
    )
    val path = UltraSmoothStrokeEngine.createUltraSmoothPath(rawPoints)
    assertNotNull(path)

    val densePoints = UltraSmoothStrokeEngine.interpolateDenseStrokePoints(rawPoints, stepDistance = 5f)
    assertTrue("Dense interpolated points should exceed raw points", densePoints.size > rawPoints.size)

    val stabilizer = UltraSmoothStrokeEngine.LiveTouchStabilizer()
    val baseTime = 1000L
    val first = stabilizer.onDown(TouchPoint(10f, 10f, 1f), timestamp = baseTime)
    assertNotNull(first)
    val next = stabilizer.onMove(TouchPoint(30f, 30f, 1f), timestamp = baseTime + 16L)
    assertNotNull(next)
  }

  @Test
  fun `verify project serialization and deserialization`() {
    val project = Project(
      title = "Test Artwork",
      width = 1080,
      height = 1080,
      fps = 24,
      frames = listOf(
        AnimationFrame(
          layers = listOf(DrawingLayer(name = "Layer 1"))
        )
      )
    )
    val json = ProjectSerializer.serialize(project)
    assertNotNull(json)
    val restored = ProjectSerializer.deserialize(json)
    assertEquals("Test Artwork", restored.title)
    assertEquals(1080, restored.width)
    assertEquals(24, restored.fps)
    assertEquals(1, restored.frames.size)
  }
}
