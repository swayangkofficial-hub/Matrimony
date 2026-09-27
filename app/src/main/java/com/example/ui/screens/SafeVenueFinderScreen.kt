package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.GeminiAiService
import com.example.data.service.GroundedVenue
import com.example.data.service.GroundedVenueResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeVenueFinderScreen(
    viewModel: PremSetuViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { GeminiAiService(context) }

    var selectedCity by remember { mutableStateOf("Bengaluru") }
    var selectedVenueType by remember { mutableStateOf("Quiet Coffee Lounge") }
    var isSearching by remember { mutableStateOf(false) }
    var venueResult by remember { mutableStateOf<GroundedVenueResult?>(null) }

    val cities = listOf("Bengaluru", "Mumbai", "Delhi NCR", "Pune", "Hyderabad", "Chennai", "Kolkata", "Jaipur")
    val venueTypes = listOf(
        "Quiet Coffee Lounge",
        "Heritage Hotel Tea Lounge",
        "Family-Friendly Pure Veg Dining",
        "Open Courtyard Cafe"
    )

    fun executeMapsGroundedSearch() {
        isSearching = true
        coroutineScope.launch {
            val res = aiService.findSafeMeetingVenuesWithMaps(selectedCity, selectedVenueType)
            isSearching = false
            res.onSuccess {
                venueResult = it
            }.onFailure { err ->
                Toast.makeText(context, "Search notice: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        executeMapsGroundedSearch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Safe Meeting Venues",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8EAF6)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF283593), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "Google Maps Grounded",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF283593)
                                    )
                                }
                            }
                        }
                        Text(
                            "gemini-3.5-flash with googleMaps tool",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BurgundyDeep)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Public Safety First: Verified Meeting Venues",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BurgundyDeep
                            )
                            Text(
                                "Prem Setu recommends public places with high footfall, verified Google Maps reviews, well-lit parking, and family-appropriate seating.",
                                fontSize = 11.sp,
                                color = BurgundyDark
                            )
                        }
                    }
                }
            }

            // City Filter Chips
            item {
                Text("Select Meeting City:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cities.take(4).forEach { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick = {
                                selectedCity = city
                                executeMapsGroundedSearch()
                            },
                            label = { Text(city, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cities.drop(4).forEach { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick = {
                                selectedCity = city
                                executeMapsGroundedSearch()
                            },
                            label = { Text(city, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Venue Style Chips
            item {
                Text("Venue Atmosphere:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    venueTypes.forEach { vt ->
                        FilterChip(
                            selected = selectedVenueType == vt,
                            onClick = {
                                selectedVenueType = vt
                                executeMapsGroundedSearch()
                            },
                            label = { Text(vt, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { executeMapsGroundedSearch() },
                    enabled = !isSearching,
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("search_safe_venues_maps_btn")
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grounding with Google Maps...")
                    } else {
                        Icon(Icons.Default.Map, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search $selectedCity with Google Maps")
                    }
                }
            }

            if (venueResult != null) {
                item {
                    Text(
                        venueResult?.summaryText ?: "",
                        fontSize = 11.sp,
                        color = NeutralMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(venueResult!!.venues) { venue ->
                    VenueCard(venue = venue) {
                        Toast.makeText(context, "${venue.name} added to your First Meeting Plan!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun VenueCard(
    venue: GroundedVenue,
    onAddToMeeting: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(venue.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BurgundyDark)
                    Text(venue.address, fontSize = 12.sp, color = NeutralMedium)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldLight
                ) {
                    Text(
                        venue.rating,
                        color = BurgundyDeep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = TrustGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Safety: ${venue.safetyHighlights}", fontSize = 11.sp, color = NeutralDark)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = BurgundyPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Atmosphere: ${venue.suitability}", fontSize = 11.sp, color = NeutralDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode("${venue.name}, ${venue.address}"))
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode("${venue.name} ${venue.address}")))
                            context.startActivity(browserIntent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View on Maps", fontSize = 11.sp)
                }

                Button(
                    onClick = onAddToMeeting,
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Use in Plan", fontSize = 11.sp)
                }
            }
        }
    }
}
