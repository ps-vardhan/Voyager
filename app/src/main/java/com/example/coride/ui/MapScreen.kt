package com.example.coride.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coride.network.PlacePoi
import com.example.coride.network.RouteResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    from: String,
    to: String,
    route: RouteResult,
    places: List<PlacePoi>,
    onBack: () -> Unit
) {
    var selectedPlace by remember { mutableStateOf<PlacePoi?>(null) }
    
    // GPS Animation progression along the route path
    val infiniteTransition = rememberInfiniteTransition(label = "GPS Dot")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GPS Location Progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F13))
    ) {
        // Mock Map Canvas with dynamic route polyline & live GPS dot
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF13131A))
        ) {
            val width = size.width
            val height = size.height
            
            // Draw premium grid system background
            val gridSpacing = 80f
            for (x in 0 until (width / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFF1E1E24).copy(alpha = 0.4f),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0 until (height / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFF1E1E24).copy(alpha = 0.4f),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            // Draw Route Polyline
            val path = Path()
            if (route.polylinePoints.isNotEmpty()) {
                // Map coordinates to canvas space
                val mappedPoints = route.polylinePoints.map { pt ->
                    // Map lat/long delta onto canvas size
                    val normX = (pt.second - (-122.5)) / ((-118.0) - (-122.5))
                    val normY = (pt.first - 37.8) / (34.0 - 37.8) // reversed Y coordinate
                    
                    Offset(
                        x = (normX * width * 0.8f + width * 0.1f).toFloat(),
                        y = (normY * height * 0.6f + height * 0.2f).toFloat()
                    )
                }

                path.moveTo(mappedPoints[0].x, mappedPoints[0].y)
                for (i in 1 until mappedPoints.size) {
                    path.lineTo(mappedPoints[i].x, mappedPoints[i].y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF6C5CE7),
                    style = Stroke(width = 8f, miter = 1f)
                )
                
                // Draw live moving GPS Dot
                val index = ((mappedPoints.size - 1) * progress).toInt().coerceIn(0, mappedPoints.size - 1)
                val gpsPos = mappedPoints[index]
                drawCircle(
                    color = Color(0xFF00D2D3),
                    radius = 14f,
                    center = gpsPos
                )
                drawCircle(
                    color = Color(0xFF00D2D3).copy(alpha = 0.3f),
                    radius = 28f + (progress * 10f),
                    center = gpsPos
                )
            }
        }

        // Top Navigation overlay
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "$from ➔ $to",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${route.distanceText} • ${route.durationText}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E1E24).copy(alpha = 0.85f)
            )
        )

        // Selected stop highlight sheet overlay (appears if stop is clicked)
        selectedPlace?.let { place ->
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E24)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = place.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "✕",
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.clickable { selectedPlace = null }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = place.category, color = Color(0xFF6C5CE7), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = place.address, color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedPlace = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Route stops")
                    }
                }
            }
        }

        // Bottom overlay container with sliding places list
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0F0F13).copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            PlacesPanel(
                places = places,
                onPlaceSelected = { selectedPlace = it }
            )
        }
    }
}
