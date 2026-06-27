package com.example.voyagers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.voyagers.ui.viewmodel.TripViewModel

@Composable
fun MapScreen(
    viewModel: TripViewModel,
    modifier: Modifier = Modifier
) {
    val activeTrip by viewModel.activeTrip.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF130F26), Color(0xFF0F0B1E))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Pulse Alert Badge
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF00FFD1),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ACTIVE TRACKING SHIELD",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "Emergency Guardians Guard Active",
                fontSize = 13.sp,
                color = Color(0xFF00FFD1)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Traveler Info Card
            activeTrip?.let { trip ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Traveler", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(trip.travelerName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00FFD1), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("START", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(trip.startLocationName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp))
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DESTINATION", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(trip.destinationName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp))
                            }
                        }
                    }
                }
            }

            // Real Google Map View Card
            activeTrip?.let { trip ->
                val startLatLng = LatLng(trip.startLatitude, trip.startLongitude)
                val currentLatLng = LatLng(trip.currentLatitude, trip.currentLongitude)
                val destLatLng = LatLng(trip.destinationLatitude, trip.destinationLongitude)

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(currentLatLng, 8f)
                }

                // Automatically center map on current position when it updates
                LaunchedEffect(trip.currentLatitude, trip.currentLongitude) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 8f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.05f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(position = startLatLng),
                                title = "Start Point",
                                snippet = trip.startLocationName
                            )
                            Marker(
                                state = MarkerState(position = destLatLng),
                                title = "Destination",
                                snippet = trip.destinationName
                            )
                            Marker(
                                state = MarkerState(position = currentLatLng),
                                title = "Live Tracking Shield Position",
                                snippet = "Latitude: ${trip.currentLatitude}, Longitude: ${trip.currentLongitude}"
                            )
                            Polyline(
                                points = listOf(startLatLng, destLatLng),
                                color = Color(0xFF00FFD1),
                                width = 8f
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT LATITUDE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%.6f", trip.currentLatitude),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CURRENT LONGITUDE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format("%.6f", trip.currentLongitude),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Simulation Controller Card
            var speedVal by remember { mutableStateOf(1f) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simulation Speed Controller",
                        color = Color(0xFF00FFD1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Control tracking frequency speed in the background service",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = speedVal,
                            onValueChange = { 
                                speedVal = it
                                val speedMultiplier = when {
                                    it < 0.2f -> 1
                                    it < 0.4f -> 5
                                    it < 0.6f -> 20
                                    it < 0.8f -> 50
                                    else -> 100
                                }
                                viewModel.updateSimulationSpeed(speedMultiplier)
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00FFD1),
                                activeTrackColor = Color(0xFF00FFD1),
                                inactiveTrackColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        val speedText = when {
                            speedVal < 0.2f -> "1x (Normal)"
                            speedVal < 0.4f -> "5x (Fast)"
                            speedVal < 0.6f -> "20x (Super)"
                            speedVal < 0.8f -> "50x (Hyper)"
                            else -> "100x (Max)"
                        }
                        Text(
                            text = speedText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }

            // Info Alert
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00FFD1).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00FFD1))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Simulation Mode is active. A background Foreground Service is writing live GPS ticks to Room database at set speeds. Use the slider above to speed up the journey.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // End Trip button
            Button(
                onClick = { viewModel.endTrip() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "END TRIP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
