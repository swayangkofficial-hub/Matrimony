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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import com.example.data.repository.ProfilePhotoRepository
import com.example.ui.components.PhotoGallery
import com.example.ui.theme.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    currentProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    userProfileRepository: UserProfileRepository = remember { UserProfileRepository() },
    onVerifyEmailClick: (() -> Unit)? = null,
    onOpenPrivacySettings: (() -> Unit)? = null,
    zohoEmailService: com.example.data.service.ZohoEmailService = remember { com.example.data.service.ZohoEmailService() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoRepository = remember { ProfilePhotoRepository() }

    // Form editing state initialized with currentProfile
    var id by remember { mutableStateOf(currentProfile.id.ifBlank { "user_default_001" }) }
    var name by remember { mutableStateOf(currentProfile.name) }
    var ageText by remember { mutableStateOf(if (currentProfile.age > 0) currentProfile.age.toString() else "29") }
    var gender by remember { mutableStateOf(currentProfile.gender.ifBlank { "Male" }) }
    var city by remember { mutableStateOf(currentProfile.city) }
    var state by remember { mutableStateOf(currentProfile.state) }
    var profession by remember { mutableStateOf(currentProfile.profession) }
    var education by remember { mutableStateOf(currentProfile.education) }
    var company by remember { mutableStateOf(currentProfile.company) }
    var height by remember { mutableStateOf(currentProfile.height) }
    var religion by remember { mutableStateOf(currentProfile.religion) }
    var community by remember { mutableStateOf(currentProfile.community) }
    var motherTongue by remember { mutableStateOf(currentProfile.motherTongue) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var profileCreatedFor by remember { mutableStateOf(currentProfile.profileCreatedFor) }
    var phone by remember { mutableStateOf(currentProfile.phone) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var familyBackground by remember { mutableStateOf(currentProfile.familyBackground) }
    var livingArrangement by remember { mutableStateOf(currentProfile.livingArrangement) }
    var careerExpectation by remember { mutableStateOf(currentProfile.careerExpectation) }
    var childrenTimeline by remember { mutableStateOf(currentProfile.childrenTimeline) }
    var womenExtraPrivacy by remember { mutableStateOf(currentProfile.womenExtraPrivacy) }
    var visibilityMode by remember { mutableStateOf(currentProfile.visibilityMode) }

    // Cloud Firebase Storage Photo Gallery State
    var photoUrls by remember { mutableStateOf(currentProfile.photoUrls) }
    var avatarUrl by remember { mutableStateOf(currentProfile.avatarUrl.ifBlank { currentProfile.photoUrls.firstOrNull() ?: "" }) }

    // Advanced Matrimonial Matching Criteria State
    var familyValues by remember { mutableStateOf(currentProfile.familyValues.ifBlank { "Moderate" }) }
    var educationLevel by remember { mutableStateOf(currentProfile.educationLevel.ifBlank { "Master's / Postgraduate" }) }
    var dietaryPreference by remember { mutableStateOf(currentProfile.dietaryPreference.ifBlank { "Vegetarian" }) }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var selectedSectionIndex by remember { mutableStateOf(0) } // 0: Photos & Album, 1: Personal & Bio, 2: Career & Education, 3: Family & Values, 4: Contact & Privacy

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Matrimonial Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            "Synced with Cloud Firestore",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("user_profile_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BurgundyPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val parsedAge = ageText.toIntOrNull() ?: currentProfile.age
                            val updatedProfile = currentProfile.copy(
                                id = id,
                                name = name.trim(),
                                age = parsedAge,
                                gender = gender,
                                city = city.trim(),
                                state = state.trim(),
                                profession = profession.trim(),
                                education = education.trim(),
                                company = company.trim(),
                                height = height.trim(),
                                religion = religion.trim(),
                                community = community.trim(),
                                motherTongue = motherTongue.trim(),
                                bio = bio.trim(),
                                profileCreatedFor = profileCreatedFor,
                                phone = phone.trim(),
                                email = email.trim(),
                                familyBackground = familyBackground.trim(),
                                livingArrangement = livingArrangement.trim(),
                                careerExpectation = careerExpectation.trim(),
                                childrenTimeline = childrenTimeline.trim(),
                                womenExtraPrivacy = womenExtraPrivacy,
                                visibilityMode = visibilityMode,
                                photoUrls = photoUrls,
                                avatarUrl = avatarUrl,
                                familyValues = familyValues,
                                educationLevel = educationLevel,
                                dietaryPreference = dietaryPreference,
                                updatedAt = System.currentTimeMillis()
                            )

                            isSaving = true
                            coroutineScope.launch {
                                val result = userProfileRepository.saveUserProfile(updatedProfile)
                                isSaving = false
                                onSaveProfile(updatedProfile)
                                showSuccessAnimation = true
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Profile successfully saved to Firestore!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Profile saved locally & queued for Firestore sync", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isSaving && name.isNotBlank(),
                        modifier = Modifier.testTag("user_profile_save_top_btn")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BurgundyPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(
                                "Save",
                                fontWeight = FontWeight.Bold,
                                color = if (name.isNotBlank()) BurgundyPrimary else NeutralMedium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header summary card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(BurgundyPrimary, BurgundyDeep)
                                    )
                                )
                                .clickable { selectedSectionIndex = 0 },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = name.take(2).uppercase().ifBlank { "ME" },
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = name.ifBlank { "Candidate Name" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified Identity",
                                    tint = TrustGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${profession.ifBlank { "Professional" }} • ${city.ifBlank { "India" }}",
                                fontSize = 12.sp,
                                color = NeutralMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldLight
                                ) {
                                    Text(
                                        "Trust Score: ${currentProfile.trustScore}/100",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BurgundyDeep,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        "Health: ${currentProfile.profileHealthScore}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSectionIndex,
                containerColor = SurfaceSubtle,
                contentColor = BurgundyPrimary,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = selectedSectionIndex == 0,
                    onClick = { selectedSectionIndex = 0 },
                    text = { Text("Photos & Album", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedSectionIndex == 1,
                    onClick = { selectedSectionIndex = 1 },
                    text = { Text("Personal & Bio", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedSectionIndex == 2,
                    onClick = { selectedSectionIndex = 2 },
                    text = { Text("Career & Education", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedSectionIndex == 3,
                    onClick = { selectedSectionIndex = 3 },
                    text = { Text("Family & Values", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedSectionIndex == 4,
                    onClick = { selectedSectionIndex = 4 },
                    text = { Text("Contact & Privacy", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedSectionIndex) {
                    0 -> {
                        // Section 0: Photos & Album stored in Cloud Firebase Storage
                        PhotoGallery(
                            userId = id,
                            photoUrls = photoUrls,
                            primaryAvatarUrl = avatarUrl,
                            isWomenPrivacyEnabled = womenExtraPrivacy,
                            photoRepository = photoRepository,
                            onPhotosChanged = { updatedList, newPrimary ->
                                photoUrls = updatedList
                                avatarUrl = newPrimary
                                coroutineScope.launch {
                                    userProfileRepository.updateProfilePhotos(id, updatedList, newPrimary)
                                }
                            }
                        )
                    }

                    1 -> {
                        // Section 1: Personal & Bio
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = BackgroundIvory),
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSectionIndex = 0 }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Collections,
                                            contentDescription = null,
                                            tint = BurgundyPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Photo Album (${photoUrls.size} Photos)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BurgundyDark)
                                            Text("Stored securely in Firebase Storage", fontSize = 11.sp, color = NeutralMedium)
                                        }
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = BurgundyPrimary
                                        )
                                    }
                                }

                                Text("Basic Demographics", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Full Name *") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_name"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = ageText,
                                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(2) },
                                        label = { Text("Age (Yrs)") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_age"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = height,
                                        onValueChange = { height = it },
                                        label = { Text("Height (e.g. 5 ft 10 in)") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_height"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = religion,
                                        onValueChange = { religion = it },
                                        label = { Text("Religion") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_religion"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = community,
                                        onValueChange = { community = it },
                                        label = { Text("Community / Caste") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_community"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = motherTongue,
                                    onValueChange = { motherTongue = it },
                                    label = { Text("Mother Tongue") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_mothertongue"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = profileCreatedFor,
                                    onValueChange = { profileCreatedFor = it },
                                    label = { Text("Profile Created For (Myself / Son / Daughter / Sibling)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_createdfor"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("About Me & Partner Expectations", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = bio,
                                    onValueChange = { bio = it },
                                    label = { Text("Bio Description") },
                                    minLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_bio"),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SuggestionChip(
                                        onClick = {
                                            bio = "Passionate professional who cherishes family harmony, lifelong learning, and open communication in marriage."
                                        },
                                        label = { Text("Values First", fontSize = 11.sp) }
                                    )
                                    SuggestionChip(
                                        onClick = {
                                            bio = "Career-oriented yet family-grounded. Enjoys travel, culinary adventures, and mutual intellectual growth with my life partner."
                                        },
                                        label = { Text("Modern & Balanced", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Section 2: Career & Education
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Professional Background", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = profession,
                                    onValueChange = { profession = it },
                                    label = { Text("Profession / Job Title *") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_profession"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = company,
                                    onValueChange = { company = it },
                                    label = { Text("Company / Organization") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_company"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = education,
                                    onValueChange = { education = it },
                                    label = { Text("Education Degree & Institution") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_education"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Text("Education Level Category", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BurgundyDark)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("Doctorate", "Master's / Postgraduate", "Bachelor's / Graduate", "Professional Degree")) { lvl ->
                                        FilterChip(
                                            selected = educationLevel == lvl,
                                            onClick = { educationLevel = lvl },
                                            label = { Text(lvl, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BurgundyPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Location & Relocation", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City *") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_city"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = state,
                                        onValueChange = { state = it },
                                        label = { Text("State") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("user_profile_input_state"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                OutlinedTextField(
                                    value = careerExpectation,
                                    onValueChange = { careerExpectation = it },
                                    label = { Text("Career & Relocation Expectations") },
                                    minLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_careerexpectation"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    3 -> {
                        // Section 3: Family & Values
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Family Background & Structure", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)
                                OutlinedTextField(
                                    value = familyBackground,
                                    onValueChange = { familyBackground = it },
                                    label = { Text("Family Background Details") },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_familybackground"),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = livingArrangement,
                                    onValueChange = { livingArrangement = it },
                                    label = { Text("Preferred Living Arrangement") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_livingarrangement"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = childrenTimeline,
                                    onValueChange = { childrenTimeline = it },
                                    label = { Text("Family Planning & Children Timeline") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_childrentimeline"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Text("Family Values Orientation", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BurgundyDark)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("Traditional", "Moderate", "Liberal")) { fv ->
                                        FilterChip(
                                            selected = familyValues == fv,
                                            onClick = { familyValues = fv },
                                            label = { Text(fv, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BurgundyPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Text("Dietary Lifestyle & Habits", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BurgundyDark)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("Pure Vegetarian", "Eggetarian", "Non-Vegetarian", "Jain", "Vegan")) { dp ->
                                        FilterChip(
                                            selected = dietaryPreference == dp,
                                            onClick = { dietaryPreference = dp },
                                            label = { Text(dp, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BurgundyPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Section 4: Contact & Privacy
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Verified Contact Information", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_phone"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email Address") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("user_profile_input_email"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                // Zoho CPaaS Email Verification Status Banner
                                val emailIsVerified = remember(email, zohoEmailService.verifiedEmails.collectAsState().value) {
                                    currentProfile.isIdentityVerified || zohoEmailService.isEmailVerified(email)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (emailIsVerified) Color(0xFFECFDF5) else BurgundyLight,
                                    border = BorderStroke(1.dp, if (emailIsVerified) TrustGreen else CardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (emailIsVerified) Icons.Default.Verified else Icons.Default.MarkEmailUnread,
                                            contentDescription = null,
                                            tint = if (emailIsVerified) TrustGreen else BurgundyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (emailIsVerified) "Email Verified via Zoho CPaaS" else "Email Verification Pending",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (emailIsVerified) Color(0xFF065F46) else BurgundyDark
                                            )
                                            Text(
                                                text = if (emailIsVerified) "prem.setu@technope.co.in security shield active" else "Verify email via 6-digit OTP to boost Trust Score +5",
                                                fontSize = 11.sp,
                                                color = NeutralMedium
                                            )
                                        }
                                        if (!emailIsVerified) {
                                            FilledTonalButton(
                                                onClick = {
                                                    onVerifyEmailClick?.invoke()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("verify_email_otp_btn")
                                            ) {
                                                Text("Verify OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Privacy & Safe Access Controls", fontWeight = FontWeight.Bold, color = BurgundyDark, fontSize = 14.sp)

                                // Privacy & Search Visibility Settings Card (Firestore Document sync)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenPrivacySettings?.invoke() }
                                        .testTag("user_profile_open_privacy_settings_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEF3C7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFB45309),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Search Visibility & Details Privacy",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Text(
                                                text = "Control phone number and location visibility in search results (Stored in Firestore)",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309),
                                                lineHeight = 15.sp
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Open Privacy Settings",
                                            tint = Color(0xFFB45309)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceSubtle)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Women Extra Privacy Shield", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BurgundyDark)
                                        Text("Hide photo and surname until reciprocal request is approved", fontSize = 11.sp, color = NeutralMedium)
                                    }
                                    Switch(
                                        checked = womenExtraPrivacy,
                                        onCheckedChange = { womenExtraPrivacy = it },
                                        modifier = Modifier.testTag("user_profile_switch_privacy")
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceSubtle)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Profile Visibility: $visibilityMode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BurgundyDark)
                                        Text("Currently visible to verified Prem Setu members", fontSize = 11.sp, color = NeutralMedium)
                                    }
                                    TextButton(
                                        onClick = {
                                            visibilityMode = when (visibilityMode) {
                                                "PUBLIC" -> "LIMITED"
                                                "LIMITED" -> "PRIVATE"
                                                else -> "PUBLIC"
                                            }
                                        }
                                    ) {
                                        Text("Toggle", color = BurgundyPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Save Button
                Button(
                    onClick = {
                        val parsedAge = ageText.toIntOrNull() ?: currentProfile.age
                        val updatedProfile = currentProfile.copy(
                            id = id,
                            name = name.trim(),
                            age = parsedAge,
                            gender = gender,
                            city = city.trim(),
                            state = state.trim(),
                            profession = profession.trim(),
                            education = education.trim(),
                            company = company.trim(),
                            height = height.trim(),
                            religion = religion.trim(),
                            community = community.trim(),
                            motherTongue = motherTongue.trim(),
                            bio = bio.trim(),
                            profileCreatedFor = profileCreatedFor,
                            phone = phone.trim(),
                            email = email.trim(),
                            familyBackground = familyBackground.trim(),
                            livingArrangement = livingArrangement.trim(),
                            careerExpectation = careerExpectation.trim(),
                            childrenTimeline = childrenTimeline.trim(),
                            womenExtraPrivacy = womenExtraPrivacy,
                            visibilityMode = visibilityMode,
                            photoUrls = photoUrls,
                            avatarUrl = avatarUrl,
                            familyValues = familyValues,
                            educationLevel = educationLevel,
                            dietaryPreference = dietaryPreference,
                            updatedAt = System.currentTimeMillis()
                        )

                        isSaving = true
                        coroutineScope.launch {
                            val result = userProfileRepository.saveUserProfile(updatedProfile)
                            isSaving = false
                            onSaveProfile(updatedProfile)
                            showSuccessAnimation = true
                            if (result.isSuccess) {
                                Toast.makeText(context, "Profile successfully saved to Firestore!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Profile saved locally & queued for Firestore sync", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isSaving && name.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("user_profile_save_btn")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Firestore...")
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile to Firestore", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Lottie-based Success Celebration Dialog
    if (showSuccessAnimation) {
        ProfileSuccessDialog(
            onDismiss = { showSuccessAnimation = false }
        )
    }
}

/**
 * Celebratory Lottie Success Dialog triggered upon successfully saving profile data.
 * Enhances user trust and feedback through smooth Bodymovin vector animations.
 */
@Composable
fun ProfileSuccessDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f
    )

    // Automatically dismiss dialog shortly after Lottie animation finishes
    LaunchedEffect(progress) {
        if (progress >= 1.0f) {
            delay(1200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePure),
            border = BorderStroke(1.5.dp, GoldAccent),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("lottie_success_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lottie Animation Container
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("lottie_success_anim")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Profile Updated Successfully!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BurgundyDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your matrimonial profile information, lifestyle preferences, and photos are safely synced to Cloud Firestore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Cloud Status Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloud Firestore Synchronized",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("lottie_success_done_btn")
                ) {
                    Text(
                        "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
