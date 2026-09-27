package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.ChatMessage
import com.example.data.model.MatchProfile
import com.example.data.model.MatchStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val activeChatMatch by viewModel.activeChatMatch.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val sharedContacts by viewModel.sharedContacts.collectAsState()

    // If no match selected, show active chat list
    val selectedMatch = activeChatMatch ?: matches.firstOrNull { it.status == MatchStatus.ACCEPTED }

    if (selectedMatch == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = NeutralLight
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Conversations Yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Send interest to compatible verified profiles to start private conversations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val firestoreMessages by viewModel.getChatMessages(selectedMatch.id).collectAsState(initial = chats[selectedMatch.id] ?: emptyList())
    val messages = if (firestoreMessages.isNotEmpty()) firestoreMessages else (chats[selectedMatch.id] ?: emptyList())
    val isContactShared = sharedContacts.contains(selectedMatch.id)

    val blockedUsers by viewModel.blockedUsers.collectAsState()
    val isBlocked = blockedUsers.contains(selectedMatch.id) || viewModel.isUserBlocked(selectedMatch.id)

    var inputText by remember { mutableStateOf("") }
    var showShareContactDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var blockReason by remember { mutableStateOf("Inappropriate communication") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        // Chat Top Bar with Safe Connect Badge & Match Info
        Surface(
            color = SurfacePure,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeChatMatch != null) {
                        IconButton(onClick = { viewModel.closeChat() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(selectedMatch.photoAccentColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedMatch.name.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedMatch.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (selectedMatch.isIdentityVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = VerifiedBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "${selectedMatch.profession} • ${selectedMatch.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }

                    // Contact Sharing Action
                    if (!isContactShared) {
                        OutlinedButton(
                            onClick = { showShareContactDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("share_contact_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share No.", fontSize = 11.sp)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TrustGreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = TrustGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shared ✓", fontSize = 11.sp, color = TrustTeal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // More Options (Block / Unblock User)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("chat_more_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Chat Options",
                                tint = BurgundyDark
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isBlocked) {
                                DropdownMenuItem(
                                    text = { Text("Unblock User") },
                                    onClick = {
                                        showMenu = false
                                        showUnblockDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = TrustTeal)
                                    },
                                    modifier = Modifier.testTag("menu_unblock_user")
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Block User", color = Color(0xFFDC2626)) },
                                    onClick = {
                                        showMenu = false
                                        showBlockDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626))
                                    },
                                    modifier = Modifier.testTag("menu_block_user")
                                )
                            }
                        }
                    }
                }

                // Safe Connect Privacy Notice
                Surface(
                    color = TrustGreenLight.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isContactShared)
                                "Safe Connect Mutual Consent: Contact info shared securely."
                            else
                                "Safe Chat Active: Direct phone & personal email hidden until mutual consent.",
                            fontSize = 11.sp,
                            color = TrustTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Blocked Warning Banner
                if (isBlocked) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_blocked_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Profile Blocked",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Communication is restricted. This user cannot send messages.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                            TextButton(
                                onClick = { showUnblockDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("chat_banner_unblock_btn")
                            ) {
                                Text(
                                    text = "Unblock",
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Conversation Starters Bar (Step 11 & 48 in PDF)
        Surface(
            color = SurfaceSubtle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(
                    text = "Prem Setu Meaningful Conversation Starters:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BurgundyDark,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val starters = listOf(
                        "What are your expectations from married life?",
                        "How important is living location to you?",
                        "What are your career plans over the next 5 years?",
                        "What kind of family environment do you prefer?",
                        "How do you like to spend family festivals?"
                    )
                    items(starters) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfacePure,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.clickable {
                                inputText = prompt
                            }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = BurgundyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.isSafetyAlert) {
                    // Safety Engine Real-Time Banner (Step 37 in PDF)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                        modifier = Modifier.fillMaxWidth().testTag("safety_engine_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Safety Guard: ${msg.alertWarning}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isMine) 16.dp else 4.dp,
                                bottomEnd = if (msg.isMine) 4.dp else 16.dp
                            ),
                            color = if (msg.isMine) BurgundyPrimary else SurfacePure,
                            border = if (!msg.isMine) androidx.compose.foundation.BorderStroke(1.dp, CardBorder) else null,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isMine) Color.White else NeutralDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 10.sp,
                                    color = if (msg.isMine) RoseBlush else NeutralLight,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Field
        Surface(
            color = SurfacePure,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp) // accommodate bottom nav
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (isBlocked) "Profile is blocked. Unblock to send messages." else "Write a message or test safety...",
                            fontSize = 13.sp
                        )
                    },
                    enabled = !isBlocked,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (!isBlocked && inputText.isNotBlank()) {
                            viewModel.sendMessage(selectedMatch.id, inputText)
                            inputText = ""
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size)
                                }
                            }
                        }
                    },
                    enabled = !isBlocked && inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isBlocked || inputText.isBlank()) NeutralLight else BurgundyPrimary)
                        .testTag("send_chat_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Contact Sharing Consent Dialog (Step 63 in PDF)
    if (showShareContactDialog) {
        AlertDialog(
            onDismissRequest = { showShareContactDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestContactShare(selectedMatch.id)
                        showShareContactDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier.testTag("confirm_share_contact_btn")
                ) {
                    Text("Yes, Share Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareContactDialog = false }) {
                    Text("Not Yet")
                }
            },
            title = {
                Text("Contact Sharing Approval", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Would you like to share your verified contact number with ${selectedMatch.name}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TrustGreenLight
                    ) {
                        Text(
                            text = "Both members receive verified credentials simultaneously. Prem Setu logs consent for safety tracking.",
                            fontSize = 11.sp,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        )
    }

    // Block User Confirmation Dialog
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
                    text = "Block ${selectedMatch.name}?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Blocking will immediately restrict all communication with ${selectedMatch.name}. They will no longer be able to message you, and their profile will be added to your Firestore block list.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Reason for blocking:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val reasons = listOf(
                        "Inappropriate communication",
                        "Harassment or abusive behavior",
                        "Fake or fraudulent profile",
                        "Financial or gift solicitation",
                        "Not interested"
                    )
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { blockReason = reason }
                                .padding(vertical = 4.dp),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Text(
                            text = "Cloud Safety Sync: Saved to users/${viewModel.currentUser.value?.uid ?: "current_user"}/blockedUsers in Firestore.",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.blockUser(selectedMatch.id, blockReason)
                        showBlockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_block_user_btn")
                ) {
                    Text("Block & Restrict")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBlockDialog = false },
                    modifier = Modifier.testTag("cancel_block_user_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Unblock User Confirmation Dialog
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
            title = {
                Text(
                    text = "Unblock ${selectedMatch.name}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Unblocking will allow ${selectedMatch.name} to message you again and update your Firestore document.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unblockUser(selectedMatch.id)
                        showUnblockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.testTag("confirm_unblock_user_btn")
                ) {
                    Text("Unblock User")
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

/**
 * Simple ChatScreen composable to facilitate real-time messaging between matched users
 * using Firestore collections for message history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleChatScreen(
    chatRepository: com.example.data.repository.ChatRepository,
    matchId: String,
    matchName: String,
    currentUserId: String,
    currentUserName: String,
    modifier: Modifier = Modifier,
    partnerProfession: String = "Verified Match",
    partnerCity: String = "India",
    onBack: (() -> Unit)? = null
) {
    val conversationId = remember(currentUserId, matchId) {
        com.example.data.repository.ChatRepository.getConversationId(currentUserId, matchId)
    }
    val messages by chatRepository.getMessages(conversationId, currentUserId)
        .collectAsState(initial = emptyList())

    val blockedUsers by chatRepository.getBlockedUsers(currentUserId).collectAsState(initial = emptyList())
    var isBlockedLocally by remember { mutableStateOf(chatRepository.isUserBlocked(currentUserId, matchId)) }
    val isBlocked = isBlockedLocally || blockedUsers.contains(matchId)

    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var blockReason by remember { mutableStateOf("Inappropriate communication") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundIvory)
            .testTag("simple_chat_screen")
    ) {
        // Chat Header
        Surface(
            color = SurfacePure,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BurgundyPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BurgundyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = matchName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = matchName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = VerifiedBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "$partnerProfession • $partnerCity",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TrustGreenLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TrustGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Firestore Realtime",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("simple_chat_options_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = BurgundyDark
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isBlocked) {
                            DropdownMenuItem(
                                text = { Text("Unblock User") },
                                onClick = {
                                    showMenu = false
                                    showUnblockDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = TrustTeal)
                                },
                                modifier = Modifier.testTag("simple_menu_unblock_user")
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Block User", color = Color(0xFFDC2626)) },
                                onClick = {
                                    showMenu = false
                                    showBlockDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626))
                                },
                                modifier = Modifier.testTag("simple_menu_block_user")
                            )
                        }
                    }
                }
            }
        }

        // Blocked Banner
        if (isBlocked) {
            Surface(
                color = Color(0xFFFEE2E2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simple_chat_blocked_banner")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profile Blocked",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Communication is restricted. This user cannot send messages.",
                            fontSize = 10.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                    TextButton(
                        onClick = { showUnblockDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("simple_chat_banner_unblock_btn")
                    ) {
                        Text(
                            text = "Unblock",
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = NeutralLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No messages yet",
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark,
                                fontSize = 14.sp
                            )
                            Text(
                                "Say namaste to start real-time chat on Prem Setu.",
                                color = NeutralMedium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isSafetyAlert) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                            modifier = Modifier.fillMaxWidth().testTag("safety_engine_alert_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "AI Safety Guard: ${msg.alertWarning}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isMine) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isMine) 4.dp else 16.dp
                                ),
                                color = if (msg.isMine) BurgundyPrimary else SurfacePure,
                                border = if (!msg.isMine) androidx.compose.foundation.BorderStroke(1.dp, CardBorder) else null,
                                shadowElevation = 1.dp,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (msg.isMine) Color.White else NeutralDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 10.sp,
                                        color = if (msg.isMine) RoseBlush else NeutralLight,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Message Input
        Surface(
            color = SurfacePure,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (isBlocked) "Profile is blocked. Unblock to message." else "Write a message...",
                            fontSize = 13.sp
                        )
                    },
                    enabled = !isBlocked,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val textToSend = inputText.trim()
                        if (!isBlocked && textToSend.isNotBlank()) {
                            inputText = ""
                            coroutineScope.launch {
                                chatRepository.sendMessage(
                                    conversationId = conversationId,
                                    matchId = matchId,
                                    senderId = currentUserId,
                                    senderName = currentUserName,
                                    text = textToSend
                                )
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size)
                                }
                            }
                        }
                    },
                    enabled = !isBlocked && inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isBlocked || inputText.isBlank()) NeutralLight else BurgundyPrimary)
                        .testTag("send_chat_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
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
                    text = "Block $matchName?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to block $matchName? Real-time communication will be stopped immediately and recorded in Firestore.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reason:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val reasons = listOf(
                        "Inappropriate communication",
                        "Harassment or rude messages",
                        "Suspicious or fake profile",
                        "Unwanted contacts"
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
                        coroutineScope.launch {
                            chatRepository.blockUser(currentUserId, matchId, blockReason)
                            isBlockedLocally = true
                        }
                        showBlockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("simple_confirm_block_btn")
                ) {
                    Text("Block Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            title = { Text("Unblock $matchName?", fontWeight = FontWeight.Bold) },
            text = { Text("Unblock this user to allow chat messages and restore Firestore communications.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            chatRepository.unblockUser(currentUserId, matchId)
                            isBlockedLocally = false
                        }
                        showUnblockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.testTag("simple_confirm_unblock_btn")
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
