package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileVisibility
import com.example.ui.components.VerificationChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHubScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val activeSection by viewModel.moreActiveSection.collectAsState()
    val trustScore by viewModel.trustScore.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val verifications by viewModel.verifications.collectAsState()
    val matchmaker = viewModel.dedicatedMatchmaker
    val visibilityMode by viewModel.visibilityMode.collectAsState()
    val isPaused by viewModel.isMatchmakingPaused.collectAsState()
    val womenExtraPrivacy by viewModel.womenExtraPrivacy.collectAsState()
    val membershipTier by viewModel.membershipTier.collectAsState()

    var aiInputText by remember {
        mutableStateOf("I am a simple person working in Kolkata. I like travelling, music and spending time with my family.")
    }
    var selectedTone by remember { mutableStateOf("Professional") }
    var showBioAppliedSnackbar by remember { mutableStateOf(false) }

    if (activeSection == "LIVE_VOICE") {
        VoiceConversationScreen(onBack = { viewModel.setMoreActiveSection("HUB") })
    } else if (activeSection == "AI_MEDIA") {
        AiStudioHubScreen(onBack = { viewModel.setMoreActiveSection("HUB") })
    } else if (activeSection == "SAFE_VENUES") {
        SafeVenueFinderScreen(viewModel = viewModel, onBack = { viewModel.setMoreActiveSection("HUB") })
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .testTag("more_hub_screen")
        ) {
            // Navigation Tabs Bar
            Surface(
                color = SurfacePure,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                ScrollableTabRow(
                    selectedTabIndex = when (activeSection) {
                        "HUB" -> 0
                        "LIVE_VOICE" -> 1
                        "AI_MEDIA" -> 2
                        "SAFE_VENUES" -> 3
                        "VERIFICATION" -> 4
                        "SAFETY" -> 5
                        "AI_BUILDER" -> 6
                        "MATCHMAKER_CRM" -> 7
                        "MEMBERSHIP" -> 8
                        else -> 0
                    },
                    edgePadding = 12.dp,
                    contentColor = BurgundyPrimary
                ) {
                    Tab(
                        selected = activeSection == "HUB",
                        onClick = { viewModel.setMoreActiveSection("HUB") },
                        text = { Text("Services Hub", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSection == "LIVE_VOICE",
                        onClick = { viewModel.setMoreActiveSection("LIVE_VOICE") },
                        text = { Text("Live Voice Coach", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSection == "AI_MEDIA",
                        onClick = { viewModel.setMoreActiveSection("AI_MEDIA") },
                        text = { Text("AI Media Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSection == "SAFE_VENUES",
                        onClick = { viewModel.setMoreActiveSection("SAFE_VENUES") },
                        text = { Text("Safe Venues (Maps)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSection == "VERIFICATION",
                        onClick = { viewModel.setMoreActiveSection("VERIFICATION") },
                        text = { Text("Trust Centre", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSection == "SAFETY",
                        onClick = { viewModel.setMoreActiveSection("SAFETY") },
                        text = { Text("Safety Centre", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSection == "AI_BUILDER",
                        onClick = { viewModel.setMoreActiveSection("AI_BUILDER") },
                        text = { Text("AI Bio Writer", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSection == "MATCHMAKER_CRM",
                        onClick = { viewModel.setMoreActiveSection("MATCHMAKER_CRM") },
                        text = { Text("Matchmaker & CRM", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeSection == "MEMBERSHIP",
                        onClick = { viewModel.setMoreActiveSection("MEMBERSHIP") },
                        text = { Text("Membership", fontSize = 12.sp) }
                    )
                }
            }

        // Section Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeSection) {
                "HUB" -> {
                    // Quick Services Grid
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BurgundyDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Prem Setu Ecosystem Services",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Beyond a profile marketplace: comprehensive verified trust, dedicated matchmaker, safety controls, and personalized services.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoseLight)
                                )
                            }
                        }
                    }

                    item {
                        ServiceItemCard(
                            title = "My Matrimonial Profile & Biodata",
                            subtitle = "Edit personal, career, family values & privacy settings linked to Firestore",
                            icon = Icons.Default.Person,
                            iconTint = BurgundyPrimary,
                            onClick = { viewModel.openUserProfileScreen() }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Email & OTP Notifications (Zoho CPaaS)",
                            subtitle = "Welcome emails, OTP verification & matrimonial notifications via cpaas.zoho.in (prem.setu@technope.co.in)",
                            icon = Icons.Default.MarkEmailRead,
                            iconTint = BurgundyPrimary,
                            onClick = { viewModel.openEmailCenterScreen() }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Search Matches (Firestore)",
                            subtitle = "Real-time candidate grid filtered by location, age & verified status",
                            icon = Icons.Default.ManageSearch,
                            iconTint = BurgundyPrimary,
                            onClick = { viewModel.openSearchScreen() }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Live Voice Coach (Live API)",
                            subtitle = "Real-time speech practice for matrimonial dates & family alignment (gemini-3.8-live)",
                            icon = Icons.Default.RecordVoiceOver,
                            iconTint = Color(0xFF2E7D32),
                            onClick = { viewModel.setMoreActiveSection("LIVE_VOICE") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "AI Media Studio (Veo 3 & Image Preview)",
                            subtitle = "Animate photos into videos, create & edit portraits (veo-3.1-fast-generate-preview & gemini-3.1-flash-image-preview)",
                            icon = Icons.Default.MovieCreation,
                            iconTint = Color(0xFF6A1B9A),
                            onClick = { viewModel.setMoreActiveSection("AI_MEDIA") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Safe Meeting Venues (Maps Grounded)",
                            subtitle = "Find verified public cafes & meeting spots with live Maps Grounding (gemini-3.5-flash)",
                            icon = Icons.Default.Place,
                            iconTint = Color(0xFF1565C0),
                            onClick = { viewModel.setMoreActiveSection("SAFE_VENUES") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Trust & Verification Centre",
                            subtitle = "Timestamped Govt ID, Degree & Photo validation ($trustScore/100)",
                            icon = Icons.Default.VerifiedUser,
                            iconTint = TrustGreen,
                            onClick = { viewModel.setMoreActiveSection("VERIFICATION") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Safety & Fraud Prevention Centre",
                            subtitle = "Scam detection, Meeting Safety check-in, Women's Privacy Mode",
                            icon = Icons.Default.Shield,
                            iconTint = BurgundyPrimary,
                            onClick = { viewModel.setMoreActiveSection("SAFETY") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "AI Profile Assistant",
                            subtitle = "Generate polished introductions in 5 distinct personal tones",
                            icon = Icons.Default.AutoAwesome,
                            iconTint = GoldSecondary,
                            onClick = { viewModel.setMoreActiveSection("AI_BUILDER") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Dedicated Matchmaker & CRM Desk",
                            subtitle = "Consult Ananya Mukherjee (Advisor) & view live funnel analytics",
                            icon = Icons.Default.SupportAgent,
                            iconTint = VerifiedBlue,
                            onClick = { viewModel.setMoreActiveSection("MATCHMAKER_CRM") }
                        )
                    }

                    item {
                        ServiceItemCard(
                            title = "Membership & Pay-For-Service",
                            subtitle = "Transparent tiers + unbundled consultations ($membershipTier)",
                            icon = Icons.Default.WorkspacePremium,
                            iconTint = GoldAccent,
                            onClick = { viewModel.setMoreActiveSection("MEMBERSHIP") }
                        )
                    }

                    // Visibility & Pause Matchmaking
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Profile Privacy & Status",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Pause Matchmaking",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = NeutralDark
                                        )
                                        Text(
                                            text = if (isPaused) "Currently paused from new matches" else "Temporarily hide from search without losing data",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeutralMedium
                                        )
                                    }
                                    Switch(
                                        checked = isPaused,
                                        onCheckedChange = { viewModel.togglePauseMatchmaking() },
                                        modifier = Modifier.testTag("pause_matchmaking_toggle")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = CardBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Profile Visibility Setting",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ProfileVisibility.values().forEach { vis ->
                                        val isSel = visibilityMode == vis
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSel) BurgundyPrimary else SurfaceSubtle,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.updateVisibility(vis) }
                                        ) {
                                            Text(
                                                text = vis.label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else NeutralDark,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = { viewModel.openPrivacySettingsScreen() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BurgundyPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BurgundyPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("more_hub_open_privacy_settings_btn")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Privacy Settings (Search & Contact Visibility)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                "VERIFICATION" -> {
                    // Trust & Verification Detail (Step 5, 33, 34, 35)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Prem Setu Trust Profile",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = NeutralDark
                                        )
                                        Text(
                                            text = "Score: $trustScore/100 Trust Completeness",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TrustTeal
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = TrustGreenLight,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = TrustGreen)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { trustScore / 100f },
                                    color = TrustGreen,
                                    trackColor = TrustGreenLight,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Active Verified Credentials & Freshness Expiry",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                verifications.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceSubtle,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = TrustGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = NeutralDark
                                                )
                                                Text(
                                                    text = "${item.statusText} • Freshness: ${item.validityPeriod}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NeutralMedium
                                                )
                                                Text(
                                                    text = "Verified on: ${item.verifiedDate}",
                                                    fontSize = 10.sp,
                                                    color = NeutralLight
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "SAFETY" -> {
                    // Safety Centre (Step 36, 37, 38, 40)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BurgundyDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Prem Setu Safety Centre",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Zero-tolerance fraud protection, real-time scam alerts, and secure in-person meeting safety protocols.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoseLight)
                                )
                            }
                        }
                    }

                    // Never Ask For Money Policy Alert (Step 37)
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Never Send Money Alert",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Never send money, gifts, wire transfers, or financial credentials to anyone you meet online. Prem Setu's AI Safety Engine automatically flags and reviews financial solicitation.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }

                    // Women's Extra Privacy Mode (Step 40)
                    item {
                        Card(
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
                                            text = "Enhanced Privacy Shield",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NeutralDark
                                        )
                                        Text(
                                            text = "Control who can view photos, send interest, and request calls",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NeutralMedium
                                        )
                                    }
                                    Switch(
                                        checked = womenExtraPrivacy,
                                        onCheckedChange = { viewModel.toggleWomenExtraPrivacy(it) },
                                        modifier = Modifier.testTag("privacy_shield_toggle")
                                    )
                                }
                            }
                        }
                    }

                    // Blocked Profiles Management (Firestore-backed)
                    item {
                        val blockedUserIds by viewModel.blockedUsers.collectAsState()
                        val allMatches by viewModel.matches.collectAsState()
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.testTag("blocked_users_management_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Block,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Blocked Profiles",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NeutralDark
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (blockedUserIds.isEmpty()) SurfaceSubtle else Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "${blockedUserIds.size} Blocked",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (blockedUserIds.isEmpty()) NeutralMedium else Color(0xFF991B1B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Profiles blocked in Prem Setu are synchronized with your Firestore account document to prevent messages, contact sharing, and match visibility.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                if (blockedUserIds.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SurfaceSubtle,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No blocked profiles. You can block any suspicious or inappropriate profile directly from Chat or Profile Details.",
                                            fontSize = 12.sp,
                                            color = NeutralMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        blockedUserIds.forEach { targetId ->
                                            val targetMatch = allMatches.firstOrNull { it.id == targetId }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = BackgroundIvory,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFFEE2E2)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Block,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = targetMatch?.name ?: "Blocked User ($targetId)",
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 13.sp,
                                                            color = NeutralDark
                                                        )
                                                        Text(
                                                            text = targetMatch?.profession?.let { "$it • ${targetMatch.city}" } ?: "Communication restricted in Firestore",
                                                            fontSize = 11.sp,
                                                            color = NeutralMedium
                                                        )
                                                    }
                                                    OutlinedButton(
                                                        onClick = { viewModel.unblockUser(targetId) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.testTag("unblock_btn_$targetId")
                                                    ) {
                                                        Text("Unblock", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Emergency Helplines
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Emergency & Cyber Support Helpline",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🚨 National Emergency: 112", style = MaterialTheme.typography.bodyMedium, color = BurgundyDark)
                                Text("👩 Women National Helpline: 1091", style = MaterialTheme.typography.bodyMedium, color = BurgundyDark)
                                Text("💻 National Cybercrime Portal: 1930 (cybercrime.gov.in)", style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
                            }
                        }
                    }
                }

                "AI_BUILDER" -> {
                    // AI Profile Assistant (Step 7 in PDF)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Profile Bio Assistant",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = BurgundyDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Write your raw thoughts in simple words, and Prem Setu generates an eloquent, culturally respectful bio. You approve before publishing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = aiInputText,
                                    onValueChange = { aiInputText = it },
                                    label = { Text("Your rough points / background") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("ai_bio_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Select Bio Tone:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val tones = listOf("Simple", "Professional", "Traditional", "Modern", "Family-friendly")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    tones.forEach { tone ->
                                        FilterChip(
                                            selected = selectedTone == tone,
                                            onClick = { selectedTone = tone },
                                            label = { Text(tone, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.applyAIBio(selectedTone)
                                        showBioAppliedSnackbar = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("generate_apply_ai_bio_btn")
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate & Apply Polished Bio")
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = CardBorder)
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Current Active Bio on Profile:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SurfaceSubtle,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = userBio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BurgundyDeep,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                "MATCHMAKER_CRM" -> {
                    // Matchmaker Advisor Details & Operations CRM (Step 22, 23, 24, 52, 53, 54, 56, 57)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(BurgundyPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = matchmaker.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NeutralDark
                                        )
                                        Text(
                                            text = matchmaker.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BurgundyPrimary
                                        )
                                        Text(
                                            text = "${matchmaker.experienceYears} Years Exp • ${matchmaker.successfulUnions}+ Unions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TrustGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Specializations: ${matchmaker.specializations.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {},
                                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("Call Advisor", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {},
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("Request Curated List", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Live Admin & Employee CRM Dashboard (Step 52 & 53 in PDF)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePure),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Prem Setu Operations & CRM",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeutralDark
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TrustGreenLight
                                    ) {
                                        Text(
                                            text = "LIVE SYSTEM",
                                            color = TrustTeal,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Matchmaking Funnel Analytics",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                listOf(
                                    "10,000 Profiles" to 1.0f,
                                    "4,500 Eligible" to 0.45f,
                                    "2,100 Strong Matches" to 0.21f,
                                    "800 Interests" to 0.08f,
                                    "320 Accepted" to 0.032f,
                                    "180 Conversations" to 0.018f,
                                    "70 In-Person Meetings" to 0.007f
                                ).forEach { (stageLabel, ratio) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stageLabel, fontSize = 11.sp, color = NeutralMedium)
                                        LinearProgressIndicator(
                                            progress = { ratio },
                                            modifier = Modifier.width(120.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = BurgundyPrimary,
                                            trackColor = CardBorder
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Employee Smart Tasks (Today)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BurgundyDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("🔴 3 overdue follow-ups", style = MaterialTheme.typography.bodySmall, color = Color(0xFFDC2626))
                                Text("🟠 8 profiles awaiting verification review", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEA580C))
                                Text("🟢 12 new mutual matching opportunities", style = MaterialTheme.typography.bodySmall, color = TrustGreen)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("⭐ Customer Satisfaction: 4.9/5 (182 reviews)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = NeutralDark)
                            }
                        }
                    }
                }

                "MEMBERSHIP" -> {
                    // Membership & Pay-For-Service (Step 41 & 42)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BurgundyDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Pay For Service, Not Just Visibility",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Shaadi.com charges to boost profile ranking. Prem Setu charges for genuine human service, verification desk, and personalized coordination.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoseLight)
                                )
                            }
                        }
                    }

                    val tiers = listOf(
                        Triple("Free", "₹0", listOf("Verified profile", "Basic compatibility", "Daily 5 matches", "Safe Chat")),
                        Triple("Plus", "₹2,499", listOf("Advanced life-value filters", "Profile health report", "Unlimited chats")),
                        Triple("Premium", "₹4,999", listOf("Profile boost", "Priority verification", "Emergency safety line", "Direct contact unlock")),
                        Triple("Assist (Active)", "₹8,999", listOf("Dedicated Matchmaker", "Handpicked profiles", "Family introductions", "Meeting facilitation")),
                        Triple("VIP", "₹19,999", listOf("Personal Relationship Manager", "Private meeting coordination", "Background cross-checks", "Concierge desk"))
                    )

                    tiers.forEach { (name, price, benefits) ->
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (membershipTier.contains(name.split(" ").first())) BurgundyPrimary else CardBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BurgundyDark
                                        )
                                        Text(
                                            text = price,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = GoldSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    benefits.forEach { b ->
                                        Text("• $b", style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { viewModel.updateMembership(name) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (membershipTier.contains(name.split(" ").first())) TrustGreen else BurgundyPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text(
                                            if (membershipTier.contains(name.split(" ").first())) "Current Active Plan" else "Select $name",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ServiceItemCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeutralLight)
        }
    }
}
