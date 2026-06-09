package com.example.coride.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coride.network.PlacePoi

@Composable
fun PlacesPanel(
    places: List<PlacePoi>,
    onPlaceSelected: (PlacePoi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Suggested Stops Along Route",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(places) { place ->
                PlaceCard(place = place, onClick = { onPlaceSelected(place) })
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: PlacePoi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E24).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when (place.category) {
                            "Scenic Spot" -> Color(0xFF00D2D3)
                            "Landmark" -> Color(0xFFFF9F43)
                            "Food & Dining" -> Color(0xFFFF6B6B)
                            else -> Color(0xFF10AC84)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = place.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = place.category,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "★ ${place.rating}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF1C40F)
                )
                Text(
                    text = "View Stop",
                    fontSize = 11.sp,
                    color = Color(0xFF6C5CE7),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
