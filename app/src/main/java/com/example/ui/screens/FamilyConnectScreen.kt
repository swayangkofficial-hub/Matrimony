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
import com.example.data.model.FamilyMember
import com.example.data.model.FamilyParticipationMode
import com.example.ui.components.PremSetuTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.PremSetuViewModel

@Composable
fun FamilyConnectScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val familyMode by viewModel.familyMode.collectAsState()

    var showInviteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("family_connect_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BurgundyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GoldLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "FAMILY-FIRST ECOSYSTEM",
                                color = BurgundyDeep,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Family Connect & Controlled Participation",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Involve parents and siblings with strict privacy boundaries: family can view profiles & compatibility, while your private chats remain strictly confidential.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoseLight)
                    )
                }
            }
        }

        // Mode Selector Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Family Participation Model",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FamilyParticipationMode.values().forEach { mode ->
                        val isSelected = familyMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RoseLight else SurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BurgundyPrimary else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.updateFamilyMode(mode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateFamilyMode(mode) },
                                    colors = RadioButtonDefaults.colors(selectedColor = BurgundyPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) BurgundyDark else NeutralDark
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Granular Privacy Table (What Family Can vs Cannot See - Step 20 in PDF)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = TrustTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Strict Privacy Matrix for Family View",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Can See
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(TrustGreenLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Family CAN See:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            listOf(
                                "Basic profile info",
                                "Education & Career",
                                "Family background",
                                "Selected photos",
                                "Verification status",
                                "Compatibility scores"
                            ).forEach { item ->
                                Text("✓ $item", fontSize = 11.sp, color = Color(0xFF064E3B), modifier = Modifier.padding(vertical = 1.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Cannot See
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(RoseLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Family CANNOT See:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BurgundyDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            listOf(
                                "Private 1-on-1 chats",
                                "Phone & contact details",
                                "Private documents",
                                "Rejected profiles",
                                "Private meeting notes"
                            ).forEach { item ->
                                Text("✕ $item", fontSize = 11.sp, color = BurgundyDeep, modifier = Modifier.padding(vertical = 1.dp))
                            }
                        }
                    }
                }
            }
        }

        // Family Members List
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
                                text = "Authorized Family Members (${familyMembers.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Text(
                                text = "Family Information Verified by Prem Setu",
                                style = MaterialTheme.typography.bodySmall,
                                color = TrustGreen
                            )
                        }

                        Button(
                            onClick = { showInviteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("invite_family_member_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Invite", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    familyMembers.forEach { member ->
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
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BurgundyPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.relation.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = NeutralDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = RoseBlush
                                        ) {
                                            Text(
                                                text = member.relation,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BurgundyDark,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${member.phone} • ${member.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralMedium
                                    )
                                    Text(
                                        text = "Reviewed ${member.notesCount} matches • Access: Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TrustTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invite Family Member Dialog
    if (showInviteDialog) {
        var name by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("Father") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addFamilyMember(
                                name = name.trim(),
                                relation = relation,
                                phone = if (phone.isBlank()) "+91 94330 00000" else phone.trim(),
                                email = if (email.isBlank()) "${name.lowercase().replace(" ", "")}@family.com" else email.trim()
                            )
                            showInviteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier.testTag("confirm_invite_family_btn")
                ) {
                    Text("Send Family Passcode")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Invite Family Member", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "They will receive an invitation link with controlled family viewing privileges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("family_invite_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Relation:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Father", "Mother", "Brother", "Sister", "Guardian").forEach { rel ->
                            FilterChip(
                                selected = relation == rel,
                                onClick = { relation = rel },
                                label = { Text(rel, fontSize = 10.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number (for OTP)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
