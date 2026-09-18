package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.screens.DrawingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.ZPaintTheme
import com.example.ui.viewmodel.DrawingViewModel

enum class AppScreen {
    HOME,
    DRAWING
}

class MainActivity : ComponentActivity() {
    private val drawingViewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZPaintTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                // System back gesture returns to Home if currently on DrawingScreen
                BackHandler(enabled = currentScreen == AppScreen.DRAWING) {
                    currentScreen = AppScreen.HOME
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == AppScreen.DRAWING) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "AppScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                viewModel = drawingViewModel,
                                onOpenProject = { projectId ->
                                    drawingViewModel.loadProject(projectId)
                                    currentScreen = AppScreen.DRAWING
                                },
                                onOpenNewCanvas = { title, width, height ->
                                    drawingViewModel.createNewProject(title, width, height)
                                    currentScreen = AppScreen.DRAWING
                                }
                            )
                        }
                        AppScreen.DRAWING -> {
                            DrawingScreen(
                                viewModel = drawingViewModel,
                                onNavigateToHome = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
