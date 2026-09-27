package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import com.example.ui.components.ActionSuccessDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Privacy Settings screen allowing users to control the visibility of personal details
 * such as phone number and location in search results, storing these preferences
 * securely in their Firestore document.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    currentUserProfile: UserProfile,
    onBack: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    modifier: Modifier = Modifier,
    userProfileRepository: UserProfileRepository = remember { UserProfileRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Local state initialized from currentUserProfile
    var showPhoneInSearch by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.showPhoneInSearch)
    }
    var showLocationInSearch by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.showLocationInSearch)
    }
    var showEmailInSearch by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.showEmailInSearch)
    }
    var allowSearchIndexing by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.allowSearchIndexing)
    }
    var womenExtraPrivacy by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.womenExtraPrivacy)
    }
    var visibilityMode by remember(currentUserProfile) {
        mutableStateOf(currentUserProfile.visibilityMode)
    }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showSuccessDialog) {
        ActionSuccessDialog(
            title = "Privacy Settings Updated",
            message = "Your personal details visibility preferences have been securely saved to your Firestore document. Search results will immediately reflect these privacy rules.",
            detailsBadgeText = if (!showPhoneInSearch && !showLocationInSearch) {
                "Phone & Location hidden in Search"
            } else if (!showPhoneInSearch) {
                "Phone Number hidden in Search"
            } else if (!showLocationInSearch) {
                "Exact City hidden in Search"
            } else {
                "Search preferences active"
            },
            onDismiss = {
                showSuccessDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Privacy Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            text = "Control Search Results & Contact Visibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("privacy_settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BurgundyPrimary
                        )
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
                .background(BackgroundIvory)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shield & Trust Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_shield_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(BurgundyPrimary, BurgundyDeep)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Shield",
                            tint = GoldAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Matrimonial Privacy Shield",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Preferences are stored in your Firestore document (users/${currentUserProfile.id.ifBlank { "current" }}) and enforce strict access controls on the search engine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section 1: Search Result Visibility (Phone & Location)
            Text(
                text = "SEARCH RESULTS VISIBILITY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = BurgundyPrimary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Phone Number Visibility Control
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_phone_control_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (showPhoneInSearch) TrustGreenLight else SurfaceSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showPhoneInSearch) Icons.Default.Phone else Icons.Default.PhoneDisabled,
                                    contentDescription = null,
                                    tint = if (showPhoneInSearch) TrustGreen else NeutralMedium,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Show Phone Number in Search",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Text(
                                    text = if (showPhoneInSearch) "Phone is visible to verified searchers" else "Phone is masked (Recommended)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (showPhoneInSearch) TrustGreen else BurgundyPrimary
                                )
                            }
                        }

                        Switch(
                            checked = showPhoneInSearch,
                            onCheckedChange = { showPhoneInSearch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BurgundyPrimary,
                                uncheckedThumbColor = NeutralMedium,
                                uncheckedTrackColor = SurfaceSubtle
                            ),
                            modifier = Modifier.testTag("privacy_phone_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When turned OFF, candidates in search results see: '🔒 Phone hidden by privacy preference (Request Mutual Connect)'. Your actual number (${currentUserProfile.phone.ifBlank { "+91 98301 23456" }}) is never disclosed without mutual family consent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Search Preview Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search Preview: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                            Text(
                                text = if (showPhoneInSearch) currentUserProfile.phone.ifBlank { "+91 98301 23456" } else "🔒 Phone Protected (Confidential)",
                                fontSize = 11.sp,
                                color = if (showPhoneInSearch) NeutralDark else BurgundyPrimary,
                                fontWeight = if (showPhoneInSearch) FontWeight.Normal else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Location Visibility Control
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_location_control_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (showLocationInSearch) SurfaceSubtle else Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showLocationInSearch) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = if (showLocationInSearch) BurgundyPrimary else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Show Exact City in Search",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Text(
                                    text = if (showLocationInSearch) "Full City & State visible" else "State only (Exact city hidden)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (showLocationInSearch) NeutralMedium else Color(0xFFB45309)
                                )
                            }
                        }

                        Switch(
                            checked = showLocationInSearch,
                            onCheckedChange = { showLocationInSearch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BurgundyPrimary,
                                uncheckedThumbColor = NeutralMedium,
                                uncheckedTrackColor = SurfaceSubtle
                            ),
                            modifier = Modifier.testTag("privacy_location_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When turned OFF, search cards and lists will display your state (${currentUserProfile.state.ifBlank { "West Bengal" }}) but hide your specific municipality/city (${currentUserProfile.city.ifBlank { "Kolkata" }}) to protect your residential neighborhood privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Search Preview Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Search Preview: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                            Text(
                                text = if (showLocationInSearch) {
                                    "${currentUserProfile.city.ifBlank { "Kolkata" }}, ${currentUserProfile.state.ifBlank { "West Bengal" }}"
                                } else {
                                    "📍 [Confidential City], ${currentUserProfile.state.ifBlank { "West Bengal" }}"
                                },
                                fontSize = 11.sp,
                                color = if (showLocationInSearch) NeutralDark else Color(0xFFB45309),
                                fontWeight = if (showLocationInSearch) FontWeight.Normal else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Email Address Visibility Control
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Email Address in Search",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Keep email private until mutual communication is unlocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }

                    Switch(
                        checked = showEmailInSearch,
                        onCheckedChange = { showEmailInSearch = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BurgundyPrimary
                        ),
                        modifier = Modifier.testTag("privacy_email_toggle_switch")
                    )
                }
            }

            // Section 2: Directory & Discovery Mode
            Text(
                text = "DIRECTORY & COMMUNITY ACCESS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = BurgundyPrimary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Search Indexing
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include in Candidate Search Directory",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Allow compatible members to discover your profile through age, profession, and cultural filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }

                    Switch(
                        checked = allowSearchIndexing,
                        onCheckedChange = { allowSearchIndexing = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BurgundyPrimary
                        ),
                        modifier = Modifier.testTag("privacy_search_indexing_switch")
                    )
                }
            }

            // Women Extra Privacy Shield
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (womenExtraPrivacy) Color(0xFFFFF1F2) else SurfacePure
                ),
                border = BorderStroke(1.dp, if (womenExtraPrivacy) RoseBlush else CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Women's Enhanced Privacy Shield",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Blurs photo gallery for non-connected users and requires human verified ID before viewing complete biodata",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }

                    Switch(
                        checked = womenExtraPrivacy,
                        onCheckedChange = { womenExtraPrivacy = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BurgundyPrimary
                        ),
                        modifier = Modifier.testTag("privacy_women_extra_switch")
                    )
                }
            }

            // Profile Visibility Tier Selection
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overall Visibility Tier",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "PUBLIC" to "Public to All Verified Members",
                        "CONTACTS_ONLY" to "Visible Only to Shortlisted & Mutual Contacts",
                        "BLURRED_PHOTO" to "Protected (Photos Blurred until Interest Accepted)"
                    ).forEach { (mode, label) ->
                        val isSelected = visibilityMode.equals(mode, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SurfaceSubtle else SurfacePure,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) BurgundyPrimary else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { visibilityMode = mode }
                                .testTag("privacy_visibility_option_$mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { visibilityMode = mode },
                                    colors = RadioButtonDefaults.colors(selectedColor = BurgundyPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BurgundyDark else NeutralDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Action Button
            Button(
                onClick = {
                    isSaving = true
                    val updatedProfile = currentUserProfile.copy(
                        showPhoneInSearch = showPhoneInSearch,
                        showLocationInSearch = showLocationInSearch,
                        showEmailInSearch = showEmailInSearch,
                        allowSearchIndexing = allowSearchIndexing,
                        womenExtraPrivacy = womenExtraPrivacy,
                        visibilityMode = visibilityMode,
                        updatedAt = System.currentTimeMillis()
                    )

                    coroutineScope.launch {
                        // Persist to Firestore
                        userProfileRepository.saveUserProfile(updatedProfile)
                        userProfileRepository.updatePrivacySettings(
                            userId = updatedProfile.id.ifBlank { "user_default_001" },
                            showPhoneInSearch = showPhoneInSearch,
                            showLocationInSearch = showLocationInSearch,
                            showEmailInSearch = showEmailInSearch,
                            allowSearchIndexing = allowSearchIndexing,
                            womenExtraPrivacy = womenExtraPrivacy,
                            visibilityMode = visibilityMode
                        )
                        onSaveProfile(updatedProfile)
                        isSaving = false
                        showSuccessDialog = true
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_privacy_settings_btn")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Saving to Firestore...",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Privacy Preferences",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
