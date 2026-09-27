package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import com.example.data.service.FirestoreDataService
import com.example.data.service.ProfileReport
import com.example.ui.components.ActionSuccessDialog
import com.example.ui.components.CompatibilityQuizBottomSheet
import com.example.ui.components.CompatibilityQuizCard
import com.example.ui.components.ReportProfileDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onSelectProfile: ((UserProfile) -> Unit)? = null,
    modifier: Modifier = Modifier,
    userProfileRepository: UserProfileRepository = remember { UserProfileRepository() },
    firestoreDataService: FirestoreDataService = remember { FirestoreDataService() },
    currentUserProfile: UserProfile? = null,
    currentUserId: String = "user_default_001",
    onUpdateUserProfile: ((UserProfile) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeUser by remember(currentUserProfile) {
        mutableStateOf(
            currentUserProfile ?: UserProfile(
                id = currentUserId,
                name = "Aarav Sen",
                age = 29,
                gender = "Male",
                city = "Kolkata",
                state = "West Bengal",
                profession = "Senior Software Architect",
                education = "B.Tech & M.Tech, IIT Kharagpur",
                religion = "Hindu",
                community = "Bengali Brahmin",
                motherTongue = "Bengali",
                familyMode = "FAMILY_ASSISTED",
                explorationMode = "BALANCED",
                livingArrangement = "Comfortable with independent residence near parents",
                careerExpectation = "Supportive of partner's career aspirations, open to hybrid/remote work flexibility",
                childrenTimeline = "Wants children in 2-3 years post marriage.",
                familyValues = "Moderate",
                educationLevel = "Master's / Postgraduate",
                dietaryPreference = "Vegetarian"
            )
        )
    }

    var showQuizBottomSheet by remember { mutableStateOf(false) }
    var profileToReport by remember { mutableStateOf<UserProfile?>(null) }
    var successDialogData by remember { mutableStateOf<Triple<String, String, String?>?>(null) }

    if (successDialogData != null) {
        ActionSuccessDialog(
            title = successDialogData!!.first,
            message = successDialogData!!.second,
            detailsBadgeText = successDialogData!!.third,
            onDismiss = { successDialogData = null }
        )
    }

    if (profileToReport != null) {
        ReportProfileDialog(
            reportedProfile = profileToReport!!,
            reporterUserId = currentUserId,
            onDismiss = { profileToReport = null },
            onSubmitReport = { report ->
                coroutineScope.launch {
                    firestoreDataService.submitReport(report)
                    val reportedName = profileToReport?.name ?: "Candidate"
                    profileToReport = null
                    successDialogData = Triple(
                        "Profile Reported",
                        "Your confidential report regarding $reportedName has been recorded in Firestore and forwarded to Prem Setu Safety Moderation.",
                        "Safety Ticket: REP-${System.currentTimeMillis().toString().takeLast(6)}"
                    )
                }
            }
        )
    }

    // Search & Filter State
    var selectedCity by remember { mutableStateOf("All") }
    var locationInput by remember { mutableStateOf("") }
    var ageRange by remember { mutableStateOf(23f..36f) }
    var selectedGender by remember { mutableStateOf("All") }
    var verifiedOnly by remember { mutableStateOf(false) }

    // Advanced Search Filters State
    var selectedFamilyValues by remember { mutableStateOf("All") }
    var selectedEducationLevel by remember { mutableStateOf("All") }
    var selectedDietaryPreference by remember { mutableStateOf("All") }
    var showAdvancedFilters by remember { mutableStateOf(true) }

    val familyValuesOptions = listOf("All", "Traditional", "Moderate", "Liberal")
    val educationLevelOptions = listOf("All", "Master's / Postgraduate", "Bachelor's / Graduate", "Doctorate", "Professional Degree")
    val dietaryOptions = listOf("All", "Pure Vegetarian", "Eggetarian", "Non-Vegetarian", "Jain", "Vegan")

    val activeAdvancedFilterCount = remember(selectedFamilyValues, selectedEducationLevel, selectedDietaryPreference) {
        listOf(
            selectedFamilyValues != "All",
            selectedEducationLevel != "All",
            selectedDietaryPreference != "All"
        ).count { it }
    }

    // Results & Status State
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isSeeding by remember { mutableStateOf(false) }
    var hasSearchedOnce by remember { mutableStateOf(false) }
    var activeProfileDetail by remember { mutableStateOf<UserProfile?>(null) }
    var expressedInterestIds by remember { mutableStateOf(setOf<String>()) }

    val quickCities = listOf("All", "Kolkata", "Bengaluru", "Mumbai", "Pune", "Delhi NCR", "Hyderabad", "Chennai")

    BackHandler {
        if (activeProfileDetail != null) {
            activeProfileDetail = null
        } else {
            onBack()
        }
    }

    // Function to execute Firestore match query
    fun performSearch() {
        isSearching = true
        coroutineScope.launch {
            val queryLocation = if (locationInput.isNotBlank()) locationInput.trim() else selectedCity
            val result = userProfileRepository.queryProfiles(
                minAge = ageRange.start.toInt(),
                maxAge = ageRange.endInclusive.toInt(),
                location = queryLocation,
                gender = selectedGender,
                verifiedOnly = verifiedOnly,
                familyValues = selectedFamilyValues,
                educationLevel = selectedEducationLevel,
                dietaryPreference = selectedDietaryPreference
            )

            isSearching = false
            hasSearchedOnce = true
            result.onSuccess { list ->
                searchResults = list
            }.onFailure { err ->
                Toast.makeText(context, "Search notice: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Initial search when screen loads
    LaunchedEffect(Unit) {
        performSearch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Find Matches in Firestore",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            "Real-time database queries by criteria",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("search_screen_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BurgundyPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSeeding = true
                            coroutineScope.launch {
                                val seedResult = userProfileRepository.seedSampleProfilesToFirestore()
                                isSeeding = false
                                seedResult.onSuccess { count ->
                                    Toast.makeText(context, "Synced $count profiles to Cloud Firestore!", Toast.LENGTH_SHORT).show()
                                    performSearch()
                                }.onFailure {
                                    Toast.makeText(context, "Sample data ready for search", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("search_screen_seed_btn")
                    ) {
                        if (isSeeding) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BurgundyPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sync Sample Profiles to Firestore",
                                tint = BurgundyPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showQuizBottomSheet = true },
                        modifier = Modifier.testTag("search_screen_quiz_action_btn")
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Compatibility Quiz",
                            tint = BurgundyPrimary
                        )
                    }

                    IconButton(
                        onClick = { performSearch() },
                        modifier = Modifier.testTag("search_screen_refresh_btn")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Results",
                            tint = BurgundyPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Compatibility Quiz Hero Component
            item(span = { GridItemSpan(maxLineSpan) }) {
                CompatibilityQuizCard(
                    userProfile = activeUser,
                    onProfileUpdated = { updated ->
                        activeUser = updated
                        if (updated.familyValues.isNotBlank()) {
                            selectedFamilyValues = updated.familyValues
                        }
                        if (updated.dietaryPreference.isNotBlank()) {
                            selectedDietaryPreference = updated.dietaryPreference
                        }
                        coroutineScope.launch {
                            userProfileRepository.saveUserProfile(updated)
                        }
                        onUpdateUserProfile?.invoke(updated)
                        performSearch()
                    }
                )
            }

            // Header: Filter & Search Criteria Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Search Criteria",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${searchResults.size} Matches Found",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BurgundyPrimary
                            )
                        }

                        // Location Input & City Chips
                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = {
                                locationInput = it
                                if (it.isNotBlank()) selectedCity = "All"
                            },
                            placeholder = { Text("Filter by City or State (e.g. Kolkata)") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BurgundyPrimary)
                            },
                            trailingIcon = {
                                if (locationInput.isNotBlank()) {
                                    IconButton(onClick = { locationInput = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = NeutralMedium)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_location_input")
                        )

                        // Quick City selection row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickCities) { city ->
                                FilterChip(
                                    selected = selectedCity == city && locationInput.isBlank(),
                                    onClick = {
                                        selectedCity = city
                                        locationInput = ""
                                        performSearch()
                                    },
                                    label = { Text(city, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BurgundyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("search_city_chip_$city")
                                )
                            }
                        }

                        HorizontalDivider(color = SurfaceSubtle, thickness = 1.dp)

                        // Age Range Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Age Range Criteria",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BurgundyDark
                                )
                                Text(
                                    "${ageRange.start.toInt()} yrs - ${ageRange.endInclusive.toInt()} yrs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BurgundyPrimary
                                )
                            }

                            RangeSlider(
                                value = ageRange,
                                onValueChange = { ageRange = it },
                                valueRange = 20f..50f,
                                steps = 29,
                                colors = SliderDefaults.colors(
                                    thumbColor = BurgundyPrimary,
                                    activeTrackColor = BurgundyPrimary,
                                    inactiveTrackColor = SurfaceSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_age_slider")
                            )
                        }

                        // Gender & Verification Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gender:", fontSize = 12.sp, color = NeutralMedium, fontWeight = FontWeight.Medium)
                            listOf("All", "Female", "Male").forEach { g ->
                                FilterChip(
                                    selected = selectedGender == g,
                                    onClick = {
                                        selectedGender = g
                                        performSearch()
                                    },
                                    label = { Text(g, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BurgundyLight,
                                        selectedLabelColor = BurgundyDeep
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = TrustGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Show Verified Profiles Only",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BurgundyDark,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = verifiedOnly,
                                onCheckedChange = {
                                    verifiedOnly = it
                                    performSearch()
                                },
                                modifier = Modifier.testTag("search_verified_switch")
                            )
                        }

                        // Advanced Match Filters Toggle Bar
                        HorizontalDivider(color = SurfaceSubtle, thickness = 1.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAdvancedFilters = !showAdvancedFilters }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Advanced Match Criteria",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BurgundyDark
                                    )
                                    if (activeAdvancedFilterCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = BurgundyPrimary
                                        ) {
                                            Text(
                                                "$activeAdvancedFilterCount Active",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "Family Values • Education Level • Dietary Habits",
                                    fontSize = 11.sp,
                                    color = NeutralMedium
                                )
                            }

                            if (activeAdvancedFilterCount > 0) {
                                TextButton(
                                    onClick = {
                                        selectedFamilyValues = "All"
                                        selectedEducationLevel = "All"
                                        selectedDietaryPreference = "All"
                                        performSearch()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("clear_advanced_filters_btn")
                                ) {
                                    Text("Reset", fontSize = 11.sp, color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = { showAdvancedFilters = !showAdvancedFilters },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("search_advanced_filters_toggle")
                            ) {
                                Icon(
                                    if (showAdvancedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showAdvancedFilters) "Collapse Advanced Filters" else "Expand Advanced Filters",
                                    tint = BurgundyPrimary
                                )
                            }
                        }

                        // Collapsible Advanced Filter Sections
                        AnimatedVisibility(
                            visible = showAdvancedFilters,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BackgroundIvory)
                                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Family Values Filter
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.FamilyRestroom,
                                                contentDescription = null,
                                                tint = BurgundyPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Family Values",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BurgundyDark
                                            )
                                        }
                                        if (selectedFamilyValues != "All") {
                                            Text(
                                                selectedFamilyValues,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BurgundyPrimary
                                            )
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(familyValuesOptions) { option ->
                                            FilterChip(
                                                selected = selectedFamilyValues == option,
                                                onClick = {
                                                    selectedFamilyValues = option
                                                    performSearch()
                                                },
                                                label = { Text(option, fontSize = 11.sp) },
                                                leadingIcon = if (selectedFamilyValues == option) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BurgundyPrimary,
                                                    selectedLabelColor = Color.White,
                                                    selectedLeadingIconColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("filter_family_values_$option")
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                                // 2. Education Level Filter
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = BurgundyPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Education Level",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BurgundyDark
                                            )
                                        }
                                        if (selectedEducationLevel != "All") {
                                            Text(
                                                selectedEducationLevel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BurgundyPrimary
                                            )
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(educationLevelOptions) { option ->
                                            FilterChip(
                                                selected = selectedEducationLevel == option,
                                                onClick = {
                                                    selectedEducationLevel = option
                                                    performSearch()
                                                },
                                                label = { Text(option, fontSize = 11.sp) },
                                                leadingIcon = if (selectedEducationLevel == option) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BurgundyPrimary,
                                                    selectedLabelColor = Color.White,
                                                    selectedLeadingIconColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("filter_education_$option")
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                                // 3. Dietary Preferences Filter
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Restaurant,
                                                contentDescription = null,
                                                tint = BurgundyPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Dietary Preferences",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BurgundyDark
                                            )
                                        }
                                        if (selectedDietaryPreference != "All") {
                                            Text(
                                                selectedDietaryPreference,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BurgundyPrimary
                                            )
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(dietaryOptions) { option ->
                                            FilterChip(
                                                selected = selectedDietaryPreference == option,
                                                onClick = {
                                                    selectedDietaryPreference = option
                                                    performSearch()
                                                },
                                                label = { Text(option, fontSize = 11.sp) },
                                                leadingIcon = if (selectedDietaryPreference == option) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                } else null,
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BurgundyPrimary,
                                                    selectedLabelColor = Color.White,
                                                    selectedLeadingIconColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("filter_dietary_$option")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Search Execute Button
                        Button(
                            onClick = { performSearch() },
                            enabled = !isSearching,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("search_query_firestore_btn")
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Querying Firestore...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Search Firestore Database",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Results count banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Potential Matrimonial Matches",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BurgundyDark
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BurgundyLight
                    ) {
                        Text(
                            "${searchResults.size} Profiles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Loading state
            if (isSearching && searchResults.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = BurgundyPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Scanning Cloud Firestore for matching candidates...",
                                fontSize = 13.sp,
                                color = NeutralMedium
                            )
                        }
                    }
                }
            }

            // Empty state
            if (!isSearching && searchResults.isEmpty() && hasSearchedOnce) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePure),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = NeutralMedium,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No matches found with current criteria",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDark,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Try broadening your age range or selecting 'All Cities' to discover more candidates.",
                                fontSize = 12.sp,
                                color = NeutralMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedCity = "All"
                                    locationInput = ""
                                    ageRange = 21f..45f
                                    selectedGender = "All"
                                    verifiedOnly = false
                                    selectedFamilyValues = "All"
                                    selectedEducationLevel = "All"
                                    selectedDietaryPreference = "All"
                                    performSearch()
                                }
                            ) {
                                Text("Reset All Filters", color = BurgundyPrimary)
                            }
                        }
                    }
                }
            }

            // Grid Items: Potential Matches
            items(searchResults, key = { it.id }) { match ->
                val compScore = remember(match, activeUser) {
                    userProfileRepository.calculateCompatibilityScore(activeUser, match)
                }

                MatchGridCard(
                    profile = match,
                    compatibilityScore = compScore,
                    isInterestSent = expressedInterestIds.contains(match.id),
                    onCardClick = {
                        activeProfileDetail = match
                        onSelectProfile?.invoke(match)
                    },
                    onExpressInterest = {
                        expressedInterestIds = expressedInterestIds + match.id
                        coroutineScope.launch {
                            firestoreDataService.saveInterestSent(
                                userId = currentUserId,
                                matchId = match.id,
                                intro = "Namaste, I reviewed your profile on Prem Setu and would like to connect."
                            )
                        }
                        Toast.makeText(context, "Interest sent to ${match.name}!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Interactive Compatibility Quiz Modal Bottom Sheet
    if (showQuizBottomSheet) {
        CompatibilityQuizBottomSheet(
            userProfile = activeUser,
            onDismiss = { showQuizBottomSheet = false },
            onProfileUpdated = { updated ->
                activeUser = updated
                if (updated.familyValues.isNotBlank()) {
                    selectedFamilyValues = updated.familyValues
                }
                if (updated.dietaryPreference.isNotBlank()) {
                    selectedDietaryPreference = updated.dietaryPreference
                }
                coroutineScope.launch {
                    userProfileRepository.saveUserProfile(updated)
                }
                onUpdateUserProfile?.invoke(updated)
                performSearch()
            }
        )
    }

    // Full Profile Detail Modal / Bottom Sheet
    if (activeProfileDetail != null) {
        val detail = activeProfileDetail!!
        val isSent = expressedInterestIds.contains(detail.id)
        val compBreakdown = remember(detail, activeUser) {
            userProfileRepository.calculateCompatibilityBreakdown(activeUser, detail)
        }

        ModalBottomSheet(
            onDismissRequest = { activeProfileDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfacePure,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = detail.name.take(2).uppercase().ifBlank { "ME" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = detail.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            if (detail.isIdentityVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified Candidate",
                                    tint = TrustGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "${detail.age} yrs • ${detail.height} • ${detail.gender}",
                            fontSize = 12.sp,
                            color = NeutralMedium
                        )

                        val locationText = if (detail.showLocationInSearch) {
                            "${detail.city}, ${detail.state}"
                        } else {
                            "📍 [City Protected by Candidate], ${detail.state}"
                        }
                        Text(
                            text = locationText,
                            fontSize = 12.sp,
                            color = BurgundyPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Trust & Health Pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BurgundyDeep, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trust Score: ${detail.trustScore}/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BurgundyDeep)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Profile Health: ${detail.profileHealthScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }

                // Compatibility Breakdown Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (compBreakdown.overallScore >= 90) Color(0xFFECFDF5) else BurgundyLight
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (compBreakdown.overallScore >= 90) TrustGreen else BurgundyPrimary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Compatibility with ${activeUser.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BurgundyDark,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BurgundyPrimary
                            ) {
                                Text(
                                    text = "${compBreakdown.overallScore}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = compBreakdown.compatibilitySummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BurgundyDeep
                        )

                        HorizontalDivider(color = SurfacePure, thickness = 1.dp)

                        CompatibilityDimensionRow("Age Synergy", "${compBreakdown.ageScore} / 20 pts", compBreakdown.ageScore / 20f)
                        CompatibilityDimensionRow("Geographic Alignment", "${compBreakdown.locationScore} / 20 pts", compBreakdown.locationScore / 20f)
                        CompatibilityDimensionRow("Culture & Mother Tongue", "${compBreakdown.culturalScore} / 25 pts", compBreakdown.culturalScore / 25f)
                        CompatibilityDimensionRow("Family Values & Lifestyle", "${compBreakdown.lifestyleFamilyScore} / 20 pts", compBreakdown.lifestyleFamilyScore / 20f)
                        CompatibilityDimensionRow("Career & Education Parity", "${compBreakdown.careerEducationScore} / 15 pts", compBreakdown.careerEducationScore / 15f)
                    }
                }

                HorizontalDivider(color = SurfaceSubtle)

                // Bio
                Text("About", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)
                Text(
                    text = detail.bio,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF2D2D2D)
                )

                // Professional & Educational Details
                Text("Career & Education", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceSubtle)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailRow(icon = Icons.Default.Work, label = "Profession", value = detail.profession)
                    DetailRow(icon = Icons.Default.Business, label = "Company", value = detail.company.ifBlank { "Leading Organization" })
                    DetailRow(icon = Icons.Default.School, label = "Education", value = detail.education)
                    DetailRow(icon = Icons.Default.CastForEducation, label = "Education Level", value = detail.educationLevel)
                    DetailRow(
                        icon = Icons.Default.LocationCity,
                        label = "Location",
                        value = if (detail.showLocationInSearch) "${detail.city}, ${detail.state}" else "State: ${detail.state} (Exact city hidden for privacy)"
                    )
                    DetailRow(
                        icon = Icons.Default.Phone,
                        label = "Contact Phone",
                        value = if (detail.showPhoneInSearch) detail.phone.ifBlank { "+91 Confidential" } else "🔒 Phone hidden in search (Request Mutual Connect)"
                    )
                }

                // Family & Community
                Text("Family & Community Values", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceSubtle)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DetailRow(icon = Icons.Default.People, label = "Family Values", value = detail.familyValues)
                    DetailRow(icon = Icons.Default.Restaurant, label = "Dietary Preference", value = detail.dietaryPreference)
                    DetailRow(icon = Icons.Default.Favorite, label = "Religion & Community", value = "${detail.religion} • ${detail.community}")
                    DetailRow(icon = Icons.Default.Translate, label = "Mother Tongue", value = detail.motherTongue)
                    DetailRow(icon = Icons.Default.FamilyRestroom, label = "Family Background", value = detail.familyBackground.ifBlank { "Respected family background" })
                    DetailRow(icon = Icons.Default.Home, label = "Living Arrangement", value = detail.livingArrangement.ifBlank { "Flexible / mutual consensus" })
                    DetailRow(icon = Icons.Default.ChildCare, label = "Children Timeline", value = detail.childrenTimeline.ifBlank { "Open to discussion" })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Member Moderation & Safety Actions (Block & Report with ActionSuccessDialog)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { profileToReport = detail },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("search_detail_report_btn")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report Profile", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                userProfileRepository.blockUser(currentUserId, detail.id)
                                searchResults = searchResults.filter { it.id != detail.id }
                                val blockedName = detail.name
                                activeProfileDetail = null
                                successDialogData = Triple(
                                    "User Blocked",
                                    "$blockedName has been successfully blocked. They will no longer appear in your search results or be able to contact you.",
                                    "Blocked in Firestore • Communication Prevented"
                                )
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeutralDark),
                        border = BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("search_detail_block_btn")
                    ) {
                        Icon(Icons.Default.Block, contentDescription = "Block", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Block Candidate", fontSize = 11.sp)
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { activeProfileDetail = null },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = BurgundyPrimary)
                    }

                    Button(
                        onClick = {
                            if (!isSent) {
                                expressedInterestIds = expressedInterestIds + detail.id
                                coroutineScope.launch {
                                    firestoreDataService.saveInterestSent(
                                        userId = currentUserId,
                                        matchId = detail.id,
                                        intro = "Namaste ${detail.name}, I reviewed your profile on Prem Setu and would like to connect."
                                    )
                                }
                                Toast.makeText(context, "Interest sent to ${detail.name}!", Toast.LENGTH_SHORT).show()
                            }
                            activeProfileDetail = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSent) SuccessGreen else BurgundyPrimary
                        ),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(
                            imageVector = if (isSent) Icons.Default.Check else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSent) "Interest Sent" else "Express Interest",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BurgundyPrimary,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeutralMedium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = BurgundyDark,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CompatibilityDimensionRow(label: String, scoreText: String, ratio: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = NeutralMedium, fontWeight = FontWeight.Medium)
            Text(scoreText, fontSize = 11.sp, color = BurgundyDark, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = BurgundyPrimary,
            trackColor = SurfaceSubtle
        )
    }
}

@Composable
fun MatchGridCard(
    profile: UserProfile,
    compatibilityScore: Int = 85,
    isInterestSent: Boolean,
    onCardClick: () -> Unit,
    onExpressInterest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("match_card_${profile.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card Banner / Photo Placeholder with Initials
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(BurgundyPrimary, BurgundyDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Initials Circle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(2).uppercase().ifBlank { "IN" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                // Trust Score Pill in top-left
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "${profile.trustScore}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Compatibility Score Pill in top-right
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        compatibilityScore >= 90 -> Color(0xFF047857)
                        compatibilityScore >= 80 -> BurgundyDark
                        else -> Color(0xFFC2410C)
                    },
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Compatibility Score",
                            tint = GoldAccent,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "$compatibilityScore% Match",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BurgundyDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (profile.isIdentityVerified) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Identity",
                            tint = TrustGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BurgundyPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    val cardLocation = if (profile.showLocationInSearch) {
                        "${profile.age} yrs • ${profile.city}"
                    } else {
                        "${profile.age} yrs • [City Hidden]"
                    }
                    Text(
                        text = cardLocation,
                        fontSize = 11.sp,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Compatibility Score Percentage Banner on the Card
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (compatibilityScore >= 90) Color(0xFFECFDF5) else BurgundyLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (compatibilityScore >= 90) TrustGreen else BurgundyPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$compatibilityScore% Compatibility",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (compatibilityScore >= 90) Color(0xFF065F46) else BurgundyDeep
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (compatibilityScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (compatibilityScore >= 90) TrustGreen else GoldAccent,
                    trackColor = SurfaceSubtle
                )

                Text(
                    text = profile.profession,
                    fontSize = 11.sp,
                    color = BurgundyDeep,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${profile.religion} • ${profile.community}",
                    fontSize = 10.sp,
                    color = NeutralMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Advanced Criteria Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceSubtle
                    ) {
                        Text(
                            text = profile.familyValues,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = BurgundyDark,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceSubtle
                    ) {
                        Text(
                            text = when {
                                profile.educationLevel.contains("Master", ignoreCase = true) -> "PG/Master"
                                profile.educationLevel.contains("Doctorate", ignoreCase = true) -> "Doctorate"
                                profile.educationLevel.contains("Professional", ignoreCase = true) -> "Prof Deg"
                                else -> "Graduate"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = BurgundyDark,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            profile.dietaryPreference.contains("Pure Veg", ignoreCase = true) || profile.dietaryPreference.contains("Jain", ignoreCase = true) -> Color(0xFFECFDF5)
                            profile.dietaryPreference.contains("Non", ignoreCase = true) -> Color(0xFFFEF2F2)
                            else -> SurfaceSubtle
                        }
                    ) {
                        Text(
                            text = profile.dietaryPreference,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                profile.dietaryPreference.contains("Pure Veg", ignoreCase = true) || profile.dietaryPreference.contains("Jain", ignoreCase = true) -> Color(0xFF065F46)
                                profile.dietaryPreference.contains("Non", ignoreCase = true) -> Color(0xFF991B1B)
                                else -> BurgundyDeep
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("match_view_btn_${profile.id}")
                    ) {
                        Text("View", fontSize = 10.sp, color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExpressInterest,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInterestSent) TrustGreen else BurgundyPrimary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(32.dp)
                            .testTag("match_interest_btn_${profile.id}")
                    ) {
                        Icon(
                            imageVector = if (isInterestSent) Icons.Default.Check else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isInterestSent) "Sent" else "Connect",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
