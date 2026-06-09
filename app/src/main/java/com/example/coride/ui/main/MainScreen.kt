package com.example.coride.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.coride.data.DefaultDataRepository
import com.example.coride.network.DirectionsApi
import com.example.coride.network.PlacePoi
import com.example.coride.network.PlacesApi
import com.example.coride.network.RouteResult
import com.example.coride.ui.ContactManagerScreen
import com.example.coride.ui.MapScreen
import com.example.coride.ui.SearchScreen
import kotlinx.coroutines.launch

sealed interface ScreenState {
    object Search : ScreenState
    object Loading : ScreenState
    object Contacts : ScreenState
    data class MapView(
        val from: String,
        val to: String,
        val route: RouteResult,
        val places: List<PlacePoi>
    ) : ScreenState
}

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(DefaultDataRepository()) },
) {
    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.Search) }
    var previousStateForBack by remember { mutableStateOf<ScreenState>(ScreenState.Search) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = screenState) {
            is ScreenState.Search -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    SearchScreen(
                        onSearchClicked = { from, to ->
                            screenState = ScreenState.Loading
                            coroutineScope.launch {
                                try {
                                    val route = DirectionsApi.getDirections(from, to, null)
                                    val places = PlacesApi.getNearbyStops(route.waypoints, null)
                                    screenState = ScreenState.MapView(from, to, route, places)
                                } catch (e: Exception) {
                                    screenState = ScreenState.Search
                                }
                            }
                        }
                    )
                    
                    // Button to configure contacts
                    androidx.compose.material3.TextButton(
                        onClick = {
                            previousStateForBack = ScreenState.Search
                            screenState = ScreenState.Contacts
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Text("Contacts", color = Color(0xFF6C5CE7), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            is ScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6C5CE7))
                }
            }
            is ScreenState.Contacts -> {
                ContactManagerScreen(
                    onBack = { screenState = previousStateForBack }
                )
            }
            is ScreenState.MapView -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapScreen(
                        from = state.from,
                        to = state.to,
                        route = state.route,
                        places = state.places,
                        onBack = { screenState = ScreenState.Search }
                    )

                    // Overlay to switch to Contacts or trigger SOS
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 70.dp, end = 16.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = {
                                // One-Tap SOS Alert trigger
                                coroutineScope.launch {
                                    // Normally sends live coordinates, simulated below
                                    val alertMsg = "SOS EMERGENCY ALERT: Traveler needs immediate assistance at current GPS coordinates."
                                    com.example.coride.network.TwilioSmsSender.sendSms(
                                        toPhone = "Emergency Contacts",
                                        message = alertMsg,
                                        accountSid = null,
                                        authToken = null,
                                        fromPhone = null
                                    )
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B6B)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("🚨 SOS", color = Color.White)
                        }

                        androidx.compose.material3.Button(
                            onClick = {
                                previousStateForBack = state
                                screenState = ScreenState.Contacts
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E1E24)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("Contacts", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
