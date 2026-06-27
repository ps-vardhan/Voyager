package com.example.voyagers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.voyagers.ui.screens.HomeScreen
import com.example.voyagers.ui.screens.MapScreen
import com.example.voyagers.ui.screens.SettingsScreen
import com.example.voyagers.ui.theme.VoyagersTheme
import com.example.voyagers.ui.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main application activity host, annotated with AndroidEntryPoint for Hilt DI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VoyagersTheme {
                val activeTrip by viewModel.activeTrip.collectAsState()
                var currentScreen by remember { mutableStateOf("home") }

                if (activeTrip != null) {
                    // Force display Map Screen during an active tracking trip
                    MapScreen(viewModel = viewModel)
                } else {
                    when (currentScreen) {
                        "settings" -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                        else -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { currentScreen = "settings" }
                            )
                        }
                    }
                }
            }
        }
    }
}