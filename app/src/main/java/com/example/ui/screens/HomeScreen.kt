package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.PremSetuViewModel

@Composable
fun HomeScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userProfileName.collectAsState()
    val trustScore by viewModel.trustScore.collectAsState()
    val profileHealth by viewModel.profileHealthScore.collectAsState()
    val familyMode by viewModel.familyMode.collectAsState()
    val explorationMode by viewModel.explorationMode.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val journeyStages by viewModel.journeyStages.collectAsState()
    val successStories = viewModel.successStories
    val isPaused by viewModel.isMatchmakingPaused.collectAsState()

    var showWhyMatchFor by remember { mutableStateOf<MatchProfile?>(null) }
    var showSendInterestFor by remember { mutableStateOf<MatchProfile?>(null) }

    val currentStage = journeyStages.firstOrNull { it.isCurrent } ?: journeyStages.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Paused Banner if active
        if (isPaused) {
            item {
                Surface(
                    color = WarningAmberLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = WarningAmber
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Matchmaking Paused",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDeep,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Your profile is temporarily hidden from new discovery. Existing connections can still chat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralMedium
                            )
                        }
                        TextButton(
                            onClick = { viewModel.togglePauseMatchmaking() },
                            modifier = Modifier.testTag("resume_matchmaking_button")
                        ) {
                            Text("Resume", color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Personal Profile Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.openUserProfileScreen() }
                    .testTag("home_user_profile_edit_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(2).uppercase().ifBlank { "ME" },
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Namaste, $userName",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDark,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = TrustGreen, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "Trust: $trustScore/100 • Profile Health: $profileHealth% • Firestore Synced",
                            fontSize = 11.sp,
                            color = NeutralMedium
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.openUserProfileScreen() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("home_edit_profile_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp)
                    }
                }
            }
        }

        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BurgundyDark, BurgundyDeep)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = GoldLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "PREM SETU",
                                color = BurgundyDeep,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Family Protected",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Find a Match. Build a Connection. Begin a Life Together.",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verified Profiles • Intelligent Matching • Family-Friendly • Privacy First",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoseBlush)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppDestination.DISCOVERY) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_explore_matches_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Find Match", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("MATCHMAKER_CRM")
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0x2EFFFFFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_talk_matchmaker_button")
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Matchmaker", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Firestore Match Search Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.openSearchScreen() }
                    .testTag("home_firestore_search_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BurgundyLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ManageSearch,
                            contentDescription = null,
                            tint = BurgundyDeep,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Search Firestore Database",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Query candidate grid by location & age criteria",
                            fontSize = 11.sp,
                            color = NeutralMedium
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.openSearchScreen() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("home_open_search_btn")
                    ) {
                        Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // AI & Maps Intelligence Suite
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI & Maps Intelligence Suite",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BurgundyDark
                    )
                    TextButton(onClick = {
                        viewModel.navigateTo(AppDestination.MORE)
                        viewModel.setMoreActiveSection("HUB")
                    }) {
                        Text("View All", fontSize = 12.sp, color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live Voice Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("LIVE_VOICE")
                            }
                            .testTag("home_live_voice_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePure),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Live Voice", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                            Text("gemini-3.8-live", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Veo Media Studio Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("AI_MEDIA")
                            }
                            .testTag("home_ai_media_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePure),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MovieCreation, contentDescription = null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Media Studio", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                            Text("Veo 3 & Photos", fontSize = 9.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Safe Venues Maps Grounded Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("SAFE_VENUES")
                            }
                            .testTag("home_safe_venues_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePure),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Safe Venues", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BurgundyDark)
                            Text("Maps Grounded", fontSize = 9.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Matchmaking Journey (Matchmaking OS) Live Snapshot
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.navigateTo(AppDestination.JOURNEY) }
                    .testTag("matchmaking_journey_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RoseBlush,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = BurgundyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Your Matchmaking Journey",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Text(
                                    text = "Active Stage: Step ${currentStage.stepNumber} of 11",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BurgundyPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Journey",
                            tint = NeutralLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TrustGreen,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentStage.title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Text(
                                    text = currentStage.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next: Family Introduction & Meeting",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralLight
                        )
                        Text(
                            text = "View Pipeline →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyPrimary
                        )
                    }
                }
            }
        }

        // Trust Profile & Health Score Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.navigateTo(AppDestination.MORE)
                        viewModel.setMoreActiveSection("VERIFICATION")
                    }
                    .testTag("home_trust_score_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Prem Setu Trust Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Text(
                                text = "Timestamped & Verified Identity Credentials",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralMedium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = TrustGreenLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TrustGreen)
                        ) {
                            Text(
                                text = "$trustScore/100 Trust",
                                fontWeight = FontWeight.ExtraBold,
                                color = TrustTeal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VerificationChip("Mobile ✓", isValid = true, modifier = Modifier.weight(1f))
                        VerificationChip("Email ✓", isValid = true, modifier = Modifier.weight(1f))
                        VerificationChip("Govt ID ✓", isValid = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VerificationChip("Face Photo ✓", isValid = true, modifier = Modifier.weight(1f))
                        VerificationChip("IIT Degree ✓", isValid = true, modifier = Modifier.weight(1f))
                        VerificationChip("Family Desk ✓", isValid = true, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldLight.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Profile Health: $profileHealth/100 • Action: Complete employment verification",
                                style = MaterialTheme.typography.bodySmall,
                                color = BurgundyDeep
                            )
                        }
                    }
                }
            }
        }

        // Family Participation & Exploration Mode Toggles
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Matchmaking Controls",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Family Mode Selection
                    Text(
                        text = "Family Participation Mode",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BurgundyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FamilyParticipationMode.values().forEach { mode ->
                            val isSelected = familyMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BurgundyPrimary else SurfaceSubtle,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BurgundyPrimary else CardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateFamilyMode(mode) }
                                    .testTag("family_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else NeutralDark,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Match Exploration Mode
                    Text(
                        text = "Match Exploration Depth",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = BurgundyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MatchExplorationMode.values().forEach { mode ->
                            val isSelected = explorationMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) GoldSecondary else SurfaceSubtle,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldSecondary else CardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateExplorationMode(mode) }
                                    .testTag("exploration_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else NeutralDark,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // "Your Daily 5" Digest
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Your Daily 5",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RoseBlush
                            ) {
                                Text(
                                    text = "Handpicked",
                                    color = BurgundyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Based on mutual life values and verified criteria",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }

                    TextButton(
                        onClick = { viewModel.navigateTo(AppDestination.DISCOVERY) },
                        modifier = Modifier.testTag("view_all_matches_button")
                    ) {
                        Text("View All", color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(matches.take(5)) { match ->
                        DailyMatchCard(
                            match = match,
                            onCardClick = { viewModel.openMatchDetail(match) },
                            onWhyMatchClick = { showWhyMatchFor = match },
                            onSendInterest = { showSendInterestFor = match }
                        )
                    }
                }
            }
        }

        // Success Stories Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = BurgundyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prem Setu Success Stories",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(successStories) { story ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = story.coupleNames,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = BurgundyDark
                                    )
                                    Text(
                                        text = story.marriageDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralLight
                                    )
                                }
                                Text(
                                    text = story.location,
                                    fontSize = 11.sp,
                                    color = NeutralMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${story.quote}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = BurgundyPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = story.journeyStory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    showWhyMatchFor?.let { match ->
        WhyThisMatchDialog(match = match, onDismiss = { showWhyMatchFor = null })
    }

    showSendInterestFor?.let { match ->
        SendInterestDialog(
            match = match,
            onDismiss = { showSendInterestFor = null },
            onSend = { customNote ->
                viewModel.sendInterest(match.id, customNote)
                showSendInterestFor = null
            }
        )
    }
}

@Composable
fun DailyMatchCard(
    match: MatchProfile,
    onCardClick: () -> Unit,
    onWhyMatchClick: () -> Unit,
    onSendInterest: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onCardClick() }
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Profile Header with Avatar & Compatibility Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(match.photoAccentColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = match.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoseBlush,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${match.compatibility.overallScore}% Match",
                        color = BurgundyDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${match.name}, ${match.age}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (match.isIdentityVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Identity",
                        tint = VerifiedBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "${match.city}, ${match.state}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralLight
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = match.profession,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = BurgundyPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = match.education,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            // Tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (match.isPhotoVerified) {
                    VerificationChip("Photo ✓", isValid = true)
                }
                if (match.isFamilyApproved) {
                    VerificationChip("Family-Approved", isValid = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onWhyMatchClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Why?", fontSize = 11.sp)
                }

                if (match.status == MatchStatus.INTEREST_SENT) {
                    FilledTonalButton(
                        onClick = {},
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldLight)
                    ) {
                        Text("Sent ✓", fontSize = 11.sp, color = BurgundyDeep, fontWeight = FontWeight.Bold)
                    }
                } else if (match.status == MatchStatus.ACCEPTED) {
                    Button(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrustGreen)
                    ) {
                        Text("Chat", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSendInterest,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary)
                    ) {
                        Text("Interest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
