package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.PremSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val filteredMatches by viewModel.filteredMatches.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    var showWhyMatchFor by remember { mutableStateOf<MatchProfile?>(null) }
    var showSendInterestFor by remember { mutableStateOf<MatchProfile?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("discovery_screen")
    ) {
        // Search & Filter Header
        Surface(
            color = SurfacePure,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = filterState.searchQuery,
                        onValueChange = { q -> viewModel.updateFilters { copy(searchQuery = q) } },
                        placeholder = { Text("Search by name, profession, city...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeutralLight)
                        },
                        trailingIcon = {
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateFilters { copy(searchQuery = "") } }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("discovery_search_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("open_filters_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter Options",
                            tint = BurgundyPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Quick Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.showOnlyVerifiedIdentity,
                            onClick = {
                                viewModel.updateFilters { copy(showOnlyVerifiedIdentity = !showOnlyVerifiedIdentity) }
                            },
                            label = { Text("Verified Identity", fontSize = 11.sp) },
                            leadingIcon = {
                                if (filterState.showOnlyVerifiedIdentity) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.showOnlyVerifiedPhoto,
                            onClick = {
                                viewModel.updateFilters { copy(showOnlyVerifiedPhoto = !showOnlyVerifiedPhoto) }
                            },
                            label = { Text("Verified Photo", fontSize = 11.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.showOnlyFamilyApproved,
                            onClick = {
                                viewModel.updateFilters { copy(showOnlyFamilyApproved = !showOnlyFamilyApproved) }
                            },
                            label = { Text("Family-Approved", fontSize = 11.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.showOnlyMutual,
                            onClick = {
                                viewModel.updateFilters { copy(showOnlyMutual = !showOnlyMutual) }
                            },
                            label = { Text("Mutual Matches", fontSize = 11.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState.showOnlyHumanRecommended,
                            onClick = {
                                viewModel.updateFilters { copy(showOnlyHumanRecommended = !showOnlyHumanRecommended) }
                            },
                            label = { Text("Matchmaker Curated", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Matches List
        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = NeutralLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Matches Found with Selected Filters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing filters or switching exploration mode to Explore.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.updateFilters {
                                FilterState()
                            }
                        }
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("matches_list"),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredMatches.size} Verified Profiles",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Text(
                            text = "Transparent Compatibility Enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = TrustTeal
                        )
                    }
                }

                items(filteredMatches, key = { it.id }) { match ->
                    DiscoveryProfileCard(
                        match = match,
                        onCardClick = { viewModel.openMatchDetail(match) },
                        onWhyMatchClick = { showWhyMatchFor = match },
                        onSendInterest = { showSendInterestFor = match },
                        onToggleShortlist = { viewModel.toggleShortlist(match.id) },
                        onChatClick = { viewModel.openChatWith(match) }
                    )
                }
            }
        }
    }

    // Filter Sheet Modal
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Match Quality & Search Filters",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BurgundyDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Preferred City",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                val cities = listOf("All Cities", "Kolkata", "Bengaluru", "Mumbai", "Delhi NCR", "Hyderabad", "Pune")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cities) { city ->
                        val isSelected = filterState.selectedCity == city
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateFilters { copy(selectedCity = city) } },
                            label = { Text(city, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verified Quality Parameters",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Verified Identity (Govt ID)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = filterState.showOnlyVerifiedIdentity,
                        onCheckedChange = { chk -> viewModel.updateFilters { copy(showOnlyVerifiedIdentity = chk) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Verified Portrait Photo", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = filterState.showOnlyVerifiedPhoto,
                        onCheckedChange = { chk -> viewModel.updateFilters { copy(showOnlyVerifiedPhoto = chk) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Family-Approved Profiles", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = filterState.showOnlyFamilyApproved,
                        onCheckedChange = { chk -> viewModel.updateFilters { copy(showOnlyFamilyApproved = chk) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Curated by Prem Setu Matchmaker", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = filterState.showOnlyHumanRecommended,
                        onCheckedChange = { chk -> viewModel.updateFilters { copy(showOnlyHumanRecommended = chk) } }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters (${filteredMatches.size} Results)")
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
            onSend = { note ->
                viewModel.sendInterest(match.id, note)
                showSendInterestFor = null
            }
        )
    }
}

@Composable
fun DiscoveryProfileCard(
    match: MatchProfile,
    onCardClick: () -> Unit,
    onWhyMatchClick: () -> Unit,
    onSendInterest: () -> Unit,
    onToggleShortlist: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("discovery_card_${match.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category label + Shortlist button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldLight
                ) {
                    Text(
                        text = match.category.label,
                        color = BurgundyDeep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onToggleShortlist,
                    modifier = Modifier.size(32.dp).testTag("shortlist_button_${match.id}")
                ) {
                    Icon(
                        imageVector = if (match.status == MatchStatus.SHORTLISTED) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Shortlist",
                        tint = if (match.status == MatchStatus.SHORTLISTED) BurgundyPrimary else NeutralLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Details Row: Large Avatar + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(match.photoAccentColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = match.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
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
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Text(
                        text = "${match.city}, ${match.state} • ${match.height}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${match.profession} • ${match.company}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = BurgundyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${match.education} • ${match.motherTongue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compatibility Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSubtle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compatibility: ${match.compatibility.overallScore}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                    }

                    TextButton(
                        onClick = onWhyMatchClick,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp).testTag("why_this_match_btn_${match.id}")
                    ) {
                        Text(
                            text = "Why this match? →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verification badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (match.isIdentityVerified) {
                    VerificationChip("ID Verified", isValid = true)
                }
                if (match.isPhotoVerified) {
                    VerificationChip("Photo Verified", isValid = true)
                }
                if (match.isFamilyApproved) {
                    VerificationChip("Family Approved", isValid = true)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCardClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Full Profile", fontSize = 12.sp)
                }

                when (match.status) {
                    MatchStatus.INTEREST_SENT -> {
                        FilledTonalButton(
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(42.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldLight)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyDeep)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Interest Sent", fontSize = 12.sp, color = BurgundyDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                    MatchStatus.ACCEPTED -> {
                        Button(
                            onClick = onChatClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrustGreen)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Safe Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onSendInterest,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Interest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
