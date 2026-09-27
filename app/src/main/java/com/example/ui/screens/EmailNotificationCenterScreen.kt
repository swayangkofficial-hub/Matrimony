package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.MatchProfile
import com.example.data.service.DailyDigestResult
import com.example.data.service.DailyDigestSettings
import com.example.data.service.EmailLogEntry
import com.example.data.service.ZohoEmailService
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailNotificationCenterScreen(
    viewModel: PremSetuViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isSending by viewModel.isSendingEmail.collectAsState()
    val emailLogs by viewModel.emailLogs.collectAsState()
    val verifiedEmails by viewModel.verifiedEmails.collectAsState()

    val digestSettings by viewModel.digestSettings.collectAsState()
    val lastDigestResult by viewModel.lastDigestResult.collectAsState()
    val isDigestDispatching by viewModel.isDigestDispatching.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val eligibleDigestRequests = remember(matches, digestSettings) {
        viewModel.dailyDigestService.resolveMatchRequestsForDigest(matches, digestSettings)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OTP Verification", "Welcome Email", "Daily Match Digest", "Matrimonial Alerts", "Delivery Logs")

    // State for OTP Tab
    var recipientEmail by remember { mutableStateOf(currentUserProfile.email.ifBlank { "member@technope.co.in" }) }
    var recipientName by remember { mutableStateOf(currentUserProfile.name.ifBlank { "Aarav Sen" }) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpSentNotice by remember { mutableStateOf<String?>(null) }
    var otpCountdown by remember { mutableIntStateOf(0) }
    var isOtpVerified by remember { mutableStateOf(viewModel.zohoEmailService.isEmailVerified(recipientEmail)) }

    // State for Alerts Tab
    var alertType by remember { mutableStateOf("MATCH_INTEREST") }
    var candidateSenderName by remember { mutableStateOf("Priya Mukherjee") }
    var meetingDateTime by remember { mutableStateOf("Sunday, 4:00 PM") }
    var meetingVenue by remember { mutableStateOf("Flurys Heritage Cafe, Park Street, Kolkata") }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Email & OTP Center",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BurgundyLight
                            ) {
                                Text(
                                    "Zoho CPaaS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BurgundyDeep,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "ZeptoMail REST API • prem.setu@technope.co.in",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("email_center_back_btn")
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
                            Toast.makeText(
                                context,
                                "API Host: cpaas.zoho.in\nSender: prem.setu@technope.co.in\nAgent: 30dd78d1e1c9bf86",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "API Info",
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
        ) {
            // Zoho CPaaS Status & Config Card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = BurgundyDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "cpaas.zoho.in / zeptomail.in",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF047857)
                            ) {
                                Text(
                                    "SSL 465 / TLS 587",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            "Agent: 30dd78d1e1c9bf86 • Encrypted Key Active",
                            color = Color(0xFFE5E7EB),
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33000000)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TrustGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Connected",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfacePure,
                contentColor = BurgundyPrimary,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = SurfaceSubtle) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) BurgundyPrimary else NeutralMedium,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("email_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundIvory)
            ) {
                when (selectedTab) {
                    0 -> OtpVerificationTab(
                        recipientEmail = recipientEmail,
                        onEmailChange = { recipientEmail = it },
                        recipientName = recipientName,
                        onNameChange = { recipientName = it },
                        enteredOtp = enteredOtp,
                        onOtpChange = { enteredOtp = it },
                        isSending = isSending,
                        otpSentNotice = otpSentNotice,
                        isVerified = isOtpVerified || verifiedEmails.contains(recipientEmail.trim().lowercase()),
                        onSendOtp = {
                            viewModel.sendOtp(recipientEmail, recipientName) { success, msg ->
                                otpSentNotice = msg
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onVerifyOtp = {
                            viewModel.verifyOtp(recipientEmail, enteredOtp) { success, msg ->
                                if (success) {
                                    isOtpVerified = true
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    1 -> WelcomeEmailTab(
                        recipientEmail = recipientEmail,
                        onEmailChange = { recipientEmail = it },
                        recipientName = recipientName,
                        onNameChange = { recipientName = it },
                        isSending = isSending,
                        onSendWelcome = {
                            viewModel.sendWelcomeEmail(recipientEmail, recipientName) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    2 -> DailyMatchDigestTab(
                        recipientEmail = recipientEmail,
                        onEmailChange = { recipientEmail = it },
                        recipientName = recipientName,
                        onNameChange = { recipientName = it },
                        digestSettings = digestSettings,
                        onUpdateSettings = { viewModel.updateDailyDigestSettings(it) },
                        lastDigestResult = lastDigestResult,
                        isDispatching = isDigestDispatching,
                        eligibleMatchRequests = eligibleDigestRequests,
                        onSendDailyDigest = {
                            viewModel.sendDailyDigest(
                                email = recipientEmail,
                                name = recipientName,
                                matchRequests = eligibleDigestRequests,
                                context = context,
                                forceSend = true
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    3 -> MatrimonialAlertsTab(
                        recipientEmail = recipientEmail,
                        onEmailChange = { recipientEmail = it },
                        recipientName = recipientName,
                        candidateSenderName = candidateSenderName,
                        onCandidateChange = { candidateSenderName = it },
                        meetingDateTime = meetingDateTime,
                        onMeetingDateChange = { meetingDateTime = it },
                        meetingVenue = meetingVenue,
                        onMeetingVenueChange = { meetingVenue = it },
                        isSending = isSending,
                        onSendInterestAlert = {
                            viewModel.sendInterestNotification(
                                email = recipientEmail,
                                name = recipientName,
                                senderName = candidateSenderName
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        onSendMeetingAlert = {
                            viewModel.sendMeetingConfirmation(
                                email = recipientEmail,
                                name = recipientName,
                                partnerName = candidateSenderName,
                                meetingDateTime = meetingDateTime,
                                venueName = meetingVenue
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    4 -> DeliveryLogsTab(emailLogs = emailLogs)
                }
            }
        }
    }
}

@Composable
private fun OtpVerificationTab(
    recipientEmail: String,
    onEmailChange: (String) -> Unit,
    recipientName: String,
    onNameChange: (String) -> Unit,
    enteredOtp: String,
    onOtpChange: (String) -> Unit,
    isSending: Boolean,
    otpSentNotice: String?,
    isVerified: Boolean,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
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
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = BurgundyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Email OTP Verification (Zoho CPaaS)",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isVerified) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = TrustGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "Verified",
                                        color = Color(0xFF065F46),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Safeguards candidate identity and builds trust for matrimonial matching. An authentic 6-digit cryptographic OTP is sent via Zoho ZeptoMail from prem.setu@technope.co.in.",
                        fontSize = 12.sp,
                        color = NeutralMedium,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = onNameChange,
                        label = { Text("Candidate Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BurgundyPrimary) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_candidate_name_input")
                    )

                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = onEmailChange,
                        label = { Text("Candidate Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BurgundyPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_candidate_email_input")
                    )

                    Button(
                        onClick = onSendOtp,
                        enabled = !isSending && recipientEmail.contains("@"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("otp_send_btn")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatched to Zoho CPaaS...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Send 6-Digit OTP via Zoho CPaaS",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (otpSentNotice != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BurgundyLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BurgundyDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = otpSentNotice,
                                    fontSize = 12.sp,
                                    color = BurgundyDeep,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // OTP Code Verification Box
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enter Received 6-Digit OTP",
                        fontWeight = FontWeight.Bold,
                        color = BurgundyDark,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                onOtpChange(it)
                            }
                        },
                        placeholder = { Text("• • • • • •") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 6.sp,
                            color = BurgundyPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input")
                    )

                    Button(
                        onClick = onVerifyOtp,
                        enabled = enteredOtp.length == 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVerified) TrustGreen else BurgundyPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("otp_verify_btn")
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVerified) "Email Verified Successfully" else "Verify OTP Code",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeEmailTab(
    recipientEmail: String,
    onEmailChange: (String) -> Unit,
    recipientName: String,
    onNameChange: (String) -> Unit,
    isSending: Boolean,
    onSendWelcome: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Dispatch Welcome Onboarding Email",
                        fontWeight = FontWeight.Bold,
                        color = BurgundyDark,
                        fontSize = 15.sp
                    )

                    Text(
                        "Sends an Indian matrimonial welcome pack to new candidates. Includes identity verification guidelines, safe meeting recommendations, and family participation access.",
                        fontSize = 12.sp,
                        color = NeutralMedium,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = onNameChange,
                        label = { Text("Recipient Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BurgundyPrimary) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = onEmailChange,
                        label = { Text("Recipient Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BurgundyPrimary) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onSendWelcome,
                        enabled = !isSending && recipientEmail.contains("@"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_welcome_email_btn")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivering via Zoho CPaaS...")
                        } else {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Official Welcome Email", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Template Preview
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep)))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "स्वागतम • Welcome to Prem Setu",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Traditional Family Harmony & Modern Matrimonial Trust",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Namaste $recipientName,",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 14.sp
                        )
                        Text(
                            "We are honored to welcome you to Prem Setu Matrimony. Your profile has been assigned Member ID: PS-849201.",
                            fontSize = 12.sp,
                            color = Color(0xFF374151),
                            lineHeight = 17.sp
                        )
                        HorizontalDivider(color = SurfaceSubtle)
                        Text(
                            "• 100% Identity & Family Verification",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BurgundyPrimary
                        )
                        Text(
                            "• Family Participation Mode Enabled",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BurgundyPrimary
                        )
                        Text(
                            "• Safe Meeting Venues & GPS Verification",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BurgundyPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMatchDigestTab(
    recipientEmail: String,
    onEmailChange: (String) -> Unit,
    recipientName: String,
    onNameChange: (String) -> Unit,
    digestSettings: DailyDigestSettings,
    onUpdateSettings: (DailyDigestSettings) -> Unit,
    lastDigestResult: DailyDigestResult?,
    isDispatching: Boolean,
    eligibleMatchRequests: List<MatchProfile>,
    onSendDailyDigest: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_match_digest_tab")
    ) {
        // Dispatch Digest Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BurgundyLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Daily Match Requests Digest",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDark,
                                fontSize = 15.sp
                            )
                            Text(
                                "Automated Matrimonial Summary via Zoho CPaaS",
                                fontSize = 11.sp,
                                color = NeutralMedium
                            )
                        }
                    }

                    Text(
                        "Sends a daily personalized digest summarizing new match requests, verified partner introductions, and compatibility highlights to the user's registered email.",
                        fontSize = 12.sp,
                        color = NeutralMedium,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = onNameChange,
                        label = { Text("Recipient Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BurgundyPrimary) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("digest_recipient_name_input")
                    )

                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = onEmailChange,
                        label = { Text("Recipient Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BurgundyPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("digest_recipient_email_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceSubtle,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📬 ${eligibleMatchRequests.size} Match Requests eligible for today's digest",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BurgundyDark
                            )
                        }
                    }

                    Button(
                        onClick = onSendDailyDigest,
                        enabled = !isDispatching && recipientEmail.contains("@"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_daily_digest_email_btn")
                    ) {
                        if (isDispatching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatching Digest via Zoho ZeptoMail...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Daily Digest Email Now", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (lastDigestResult != null) {
                        val timeFmt = remember { SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault()) }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (lastDigestResult.success) TrustGreenLight else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (lastDigestResult.success) TrustGreen else Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("last_digest_status_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    if (lastDigestResult.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (lastDigestResult.success) TrustGreen else Color(0xFFB91C1C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (lastDigestResult.success) {
                                            "Digest Sent Successfully! (${lastDigestResult.matchRequestsCount} requests summarized)"
                                        } else {
                                            "Digest Dispatch Failed"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lastDigestResult.success) Color(0xFF065F46) else Color(0xFFB91C1C)
                                    )
                                    Text(
                                        text = "To: ${lastDigestResult.recipientEmail} • ${timeFmt.format(Date(lastDigestResult.timestamp))}",
                                        fontSize = 11.sp,
                                        color = NeutralMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Automated Scheduling & Preferences Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = BurgundyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Automated Daily Delivery Schedule",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDark,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = digestSettings.isEnabled,
                            onCheckedChange = { onUpdateSettings(digestSettings.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BurgundyPrimary
                            ),
                            modifier = Modifier.testTag("toggle_digest_enabled")
                        )
                    }

                    Text(
                        "Preferred delivery time:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeutralDark
                    )

                    val times = listOf("08:00 AM", "09:00 AM", "10:00 AM", "07:00 PM")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        times.forEach { timeStr ->
                            val isSelected = digestSettings.preferredTime == timeStr
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdateSettings(digestSettings.copy(preferredTime = timeStr)) },
                                label = { Text(timeStr, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BurgundyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceSubtle)

                    // Minimum Compatibility Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Min. Compatibility Threshold:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralDark
                        )
                        Text(
                            "${digestSettings.minimumCompatibilityScore}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyPrimary
                        )
                    }

                    Slider(
                        value = digestSettings.minimumCompatibilityScore.toFloat(),
                        onValueChange = { onUpdateSettings(digestSettings.copy(minimumCompatibilityScore = it.toInt())) },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = BurgundyPrimary,
                            activeTrackColor = BurgundyPrimary
                        ),
                        modifier = Modifier.testTag("digest_min_compat_slider")
                    )

                    // On-device notification toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "On-Device Push Notification Alert",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeutralDark
                            )
                            Text(
                                "Shows system notification when daily digest email is dispatched",
                                fontSize = 11.sp,
                                color = NeutralMedium
                            )
                        }
                        Switch(
                            checked = digestSettings.sendPushNotificationAlert,
                            onCheckedChange = { onUpdateSettings(digestSettings.copy(sendPushNotificationAlert = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BurgundyPrimary
                            )
                        )
                    }
                }
            }
        }

        // Preview of Match Requests in Today's Digest
        item {
            Text(
                "Match Requests in Today's Digest (${eligibleMatchRequests.size})",
                fontWeight = FontWeight.Bold,
                color = BurgundyDark,
                fontSize = 14.sp
            )
        }

        items(eligibleMatchRequests.take(5), key = { it.id }) { match ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.name.take(1),
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${match.name}, ${match.age}",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${match.profession} • ${match.city}",
                            fontSize = 11.sp,
                            color = NeutralMedium
                        )
                        Text(
                            text = "${match.education} | ${match.community}",
                            fontSize = 10.sp,
                            color = NeutralLight
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TrustGreenLight
                        ) {
                            Text(
                                text = "${match.compatibility.overallScore}% Match",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "★ ${match.trustScore}/100 Trust",
                            fontSize = 10.sp,
                            color = GoldSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Live HTML Email Template Preview
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(BurgundyPrimary, BurgundyDeep)))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "प्रेम सेतु • PREM SETU",
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Daily Matrimonial Digest • Today",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFFFF1F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🌟 ${eligibleMatchRequests.size} New Match Requests Today",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF881337)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    "DAILY DIGEST",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Namaste $recipientName,",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 13.sp
                        )
                        Text(
                            "Here is your personalized summary of candidates who have expressed interest or whose lifestyle and family values harmonize closely with yours.",
                            fontSize = 11.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 16.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF9FAFB),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "🔒 Prem Setu Safe Connect Protection",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    "Personal phone numbers and sensitive documents remain confidential until both prospective candidates and family members grant mutual consent.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF047857),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrimonialAlertsTab(
    recipientEmail: String,
    onEmailChange: (String) -> Unit,
    recipientName: String,
    candidateSenderName: String,
    onCandidateChange: (String) -> Unit,
    meetingDateTime: String,
    onMeetingDateChange: (String) -> Unit,
    meetingVenue: String,
    onMeetingVenueChange: (String) -> Unit,
    isSending: Boolean,
    onSendInterestAlert: () -> Unit,
    onSendMeetingAlert: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Notification 1: Match Interest Expressed
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = BurgundyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "1. Match Interest Notification",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        "Notifies candidate when someone expresses mutual matrimonial interest on Prem Setu.",
                        fontSize = 12.sp,
                        color = NeutralMedium
                    )

                    OutlinedTextField(
                        value = candidateSenderName,
                        onValueChange = onCandidateChange,
                        label = { Text("Interested Candidate Name") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onSendInterestAlert,
                        enabled = !isSending,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("send_interest_alert_btn")
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Interest Notification Email", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Notification 2: Family Meeting / Safe Date Confirmation
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = BurgundyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "2. Meeting / Date Confirmation",
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        "Sends verified meeting schedule, venue directions, and family alignment checklist.",
                        fontSize = 12.sp,
                        color = NeutralMedium
                    )

                    OutlinedTextField(
                        value = meetingDateTime,
                        onValueChange = onMeetingDateChange,
                        label = { Text("Meeting Date & Time") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = meetingVenue,
                        onValueChange = onMeetingVenueChange,
                        label = { Text("Safe Meeting Venue") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onSendMeetingAlert,
                        enabled = !isSending,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("send_meeting_alert_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Meeting Confirmation Email", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryLogsTab(emailLogs: List<EmailLogEntry>) {
    if (emailLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    tint = NeutralMedium,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No emails dispatched yet in this session",
                    fontWeight = FontWeight.Bold,
                    color = BurgundyDark,
                    fontSize = 15.sp
                )
                Text(
                    "Send an OTP or Welcome Email from the other tabs to see real-time Zoho CPaaS delivery telemetry.",
                    fontSize = 12.sp,
                    color = NeutralMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Live Delivery History (${emailLogs.size})",
                        fontWeight = FontWeight.Bold,
                        color = BurgundyDark,
                        fontSize = 14.sp
                    )
                    Text(
                        "Via Zoho CPaaS",
                        fontSize = 11.sp,
                        color = NeutralMedium
                    )
                }
            }

            items(emailLogs, key = { it.id }) { log ->
                val formatter = remember { SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault()) }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = BorderStroke(1.dp, if (log.isDelivered) CardBorder else Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (log.isDelivered) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.isDelivered) Icons.Default.Check else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (log.isDelivered) TrustGreen else Color(0xFFB91C1C),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.subject,
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDark,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "To: ${log.recipientName} (${log.recipientEmail})",
                                fontSize = 11.sp,
                                color = NeutralMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatter.format(Date(log.timestamp))} • ${log.message}",
                                fontSize = 10.sp,
                                color = if (log.isDelivered) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (log.isDelivered) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                        ) {
                            Text(
                                text = "HTTP ${log.statusCode}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (log.isDelivered) Color(0xFF047857) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
