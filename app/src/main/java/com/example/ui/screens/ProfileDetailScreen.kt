package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    match: MatchProfile,
    viewModel: PremSetuViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSendInterestDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<Triple<String, String, String?>?>(null) }
    var blockReason by remember { mutableStateOf("Inappropriate communication") }

    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val isBlocked = blockedUsers.contains(match.id) || viewModel.isUserBlocked(match.id)

    if (showSuccessDialog != null) {
        ActionSuccessDialog(
            title = showSuccessDialog!!.first,
            message = showSuccessDialog!!.second,
            detailsBadgeText = showSuccessDialog!!.third,
            onDismiss = { showSuccessDialog = null }
        )
    }

    if (showReportDialog) {
        val userProfile = UserProfile(
            id = match.id,
            name = match.name,
            age = match.age,
            gender = "",
            city = match.city,
            state = match.state,
            profession = match.profession
        )
        ReportProfileDialog(
            reportedProfile = userProfile,
            reporterUserId = viewModel.currentUser.value?.uid ?: viewModel.currentUserProfile.value.id,
            onDismiss = { showReportDialog = false },
            onSubmitReport = { report ->
                showReportDialog = false
                showSuccessDialog = Triple(
                    "Profile Reported",
                    "Your confidential safety report regarding ${match.name} has been submitted to Prem Setu Moderation.",
                    "Safety Ticket: REP-${System.currentTimeMillis().toString().takeLast(6)}"
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(match.name, fontWeight = FontWeight.Bold, color = BurgundyDark) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleShortlist(match.id) }) {
                        Icon(
                            imageVector = if (match.status == MatchStatus.SHORTLISTED) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Shortlist",
                            tint = if (match.status == MatchStatus.SHORTLISTED) BurgundyPrimary else NeutralMedium
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("detail_more_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = BurgundyDark
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isBlocked) {
                                DropdownMenuItem(
                                    text = { Text("Unblock Profile") },
                                    onClick = {
                                        showMenu = false
                                        showUnblockDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = TrustTeal)
                                    },
                                    modifier = Modifier.testTag("detail_menu_unblock_btn")
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Report Profile", color = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMenu = false
                                        showReportDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFDC2626))
                                    },
                                    modifier = Modifier.testTag("detail_menu_report_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text("Block Profile", color = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMenu = false
                                        showBlockDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626))
                                    },
                                    modifier = Modifier.testTag("detail_menu_block_btn")
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfacePure)
            )
        },
        bottomBar = {
            Surface(
                color = SurfacePure,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleShortlist(match.id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_shortlist_action_btn")
                    ) {
                        Icon(
                            imageVector = if (match.status == MatchStatus.SHORTLISTED) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (match.status == MatchStatus.SHORTLISTED) "Saved" else "Shortlist")
                    }

                    if (isBlocked) {
                        Button(
                            onClick = { showUnblockDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("detail_unblock_action_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unblock Profile", fontWeight = FontWeight.Bold)
                        }
                    } else if (match.status == MatchStatus.ACCEPTED) {
                        Button(
                            onClick = {
                                onBack()
                                viewModel.openChatWith(match)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TrustGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("detail_open_chat_action_btn")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Safe Chat", fontWeight = FontWeight.Bold)
                        }
                    } else if (match.status == MatchStatus.INTEREST_SENT) {
                        FilledTonalButton(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldLight)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = BurgundyDeep)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interest Sent ✓", color = BurgundyDeep, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showSendInterestDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("detail_send_interest_action_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Interest", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("profile_detail_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isBlocked) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_blocked_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "This Profile is Blocked",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Communication is restricted. This profile cannot contact you or send messages.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                            TextButton(onClick = { showUnblockDialog = true }) {
                                Text("Unblock", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            // Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
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
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color(match.photoAccentColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = match.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${match.name}, ${match.age}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = NeutralDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (match.isIdentityVerified) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Identity",
                                            tint = VerifiedBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${match.city}, ${match.state} • ${match.height}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeutralMedium
                                )
                                Text(
                                    text = "${match.religion}, ${match.community} • ${match.motherTongue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Professional Info
                        Text(
                            text = match.profession,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = BurgundyDark
                        )
                        Text(
                            text = "${match.company} • ${match.education}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "About Me",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Text(
                            text = match.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Trust & Time-stamped Verification
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = TrustGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trust & Verification Freshness",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeutralDark
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TrustGreenLight
                            ) {
                                Text(
                                    text = "${match.trustScore}/100 Trust",
                                    fontWeight = FontWeight.Bold,
                                    color = TrustTeal,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Identity Verified — 18 Sep 2026 (Valid for 12 months)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = VerifiedBlue
                        )
                        Text(
                            text = "Photo Verified — 15 Sep 2026 (Valid for 6 months)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                        Text(
                            text = "Education & Degree Verified with University records",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                        if (match.isFamilyApproved) {
                            Text(
                                text = "Family Information Verified by Prem Setu Family Desk",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = TrustGreen
                            )
                        }
                    }
                }
            }

            // Compatibility Overview (Step 9 in PDF)
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
                            Text(
                                text = "Compatibility Overview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RoseBlush
                            ) {
                                Text(
                                    text = "${match.compatibility.overallScore}% Overall",
                                    fontWeight = FontWeight.Bold,
                                    color = BurgundyDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        CompatibilityProgressBar("Lifestyle Compatibility", match.compatibility.lifestyleScore, Icons.Default.Favorite, BurgundyPrimary)
                        CompatibilityProgressBar("Family Expectations", match.compatibility.familyExpectationsScore, Icons.Default.Groups, TrustTeal)
                        CompatibilityProgressBar("Location Settlement", match.compatibility.locationScore, Icons.Default.LocationOn, GoldSecondary)
                        CompatibilityProgressBar("Career & Aspirations", match.compatibility.careerScore, Icons.Default.Work, BurgundyDark)
                        CompatibilityProgressBar("Communication Style", match.compatibility.communicationScore, Icons.Default.Forum, TrustGreen)
                        CompatibilityProgressBar("Future Life Goals", match.compatibility.futureGoalsScore, Icons.Default.Explore, VerifiedBlue)
                    }
                }
            }

            // Why Prem Setu Recommended This Profile (Step 12 in PDF)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Why Prem Setu Recommended This Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "5 Strong Compatibility Points",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDeep,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        match.compatibility.whyRecommendedPoints.forEach { pt ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TrustGreen,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = pt, style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                            }
                        }
                    }
                }
            }

            // Common Ground & Topics to Discuss (Step 9 & 48 in PDF)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Common Ground",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        match.compatibility.commonGround.forEach { cg ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✓", color = TrustGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = cg, style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Topics to Discuss (Conversation Facilitators)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        match.compatibility.topicsToDiscuss.forEach { topic ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = topic, style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                            }
                        }
                    }
                }
            }

            // Family Expectations & Living Arrangement (Step 50 in PDF)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Family & Lifestyle Expectations",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Family Background",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            text = match.familyBackground,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Living Arrangement Preference",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            text = match.livingArrangement,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Career & Relocation Expectations",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            text = match.careerExpectation,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Children & Timeline",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Text(
                            text = match.childrenTimeline,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                }
            }
        }
    }

    if (showSendInterestDialog) {
        SendInterestDialog(
            match = match,
            onDismiss = { showSendInterestDialog = false },
            onSend = { note ->
                viewModel.sendInterest(match.id, note)
                showSendInterestDialog = false
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Block ${match.name}?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to block this user? They will be unable to view details or message you, and their ID will be added to your Firestore block list.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reason for blocking:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val reasons = listOf(
                        "Inappropriate communication",
                        "Harassment or rude messages",
                        "Fake or fraudulent profile",
                        "Financial solicitation",
                        "Not interested"
                    )
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { blockReason = reason }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = blockReason == reason,
                                onClick = { blockReason = reason }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 12.sp, color = NeutralDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.blockUser(match.id, blockReason)
                        showBlockDialog = false
                        showSuccessDialog = Triple(
                            "User Blocked",
                            "${match.name} has been blocked. They can no longer see your profile or send you messages.",
                            "Blocked in Firestore document"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("detail_confirm_block_btn")
                ) {
                    Text("Block Profile")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBlockDialog = false },
                    modifier = Modifier.testTag("detail_cancel_block_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = TrustTeal,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Unblock ${match.name}?", fontWeight = FontWeight.Bold) },
            text = { Text("Unblocking will restore mutual communication permissions and update your Firestore block list.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unblockUser(match.id)
                        showUnblockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.testTag("detail_confirm_unblock_btn")
                ) {
                    Text("Unblock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
