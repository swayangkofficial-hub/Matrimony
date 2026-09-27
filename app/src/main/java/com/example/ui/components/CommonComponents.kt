package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompatibilityDetails
import com.example.data.model.MatchProfile
import com.example.data.model.MatchStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremSetuTopAppBar(
    title: String,
    subtitle: String? = null,
    trustScore: Int = 87,
    onTrustBadgeClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = BurgundyDark
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = GoldLight,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "PREM SETU",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BurgundyDeep,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralLight
                    )
                }
            }
        },
        actions = {
            // Trust Score Pill
            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onTrustBadgeClick() }
                    .testTag("top_bar_trust_score_badge"),
                shape = RoundedCornerShape(16.dp),
                color = TrustGreenLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrustGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Trust Score",
                        tint = TrustGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trust $trustScore/100",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TrustTeal
                    )
                }
            }
            actions()
        }
    )
}

@Composable
fun VerificationChip(
    label: String,
    isValid: Boolean = true,
    isExpiring: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isValid) VerifiedBlueLight else WarningAmberLight,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isValid) VerifiedBlue.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (isValid) VerifiedBlue else WarningAmber,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isValid) Color(0xFF0369A1) else Color(0xFF92400E)
            )
        }
    }
}

@Composable
fun CompatibilityProgressBar(
    label: String,
    percentage: Int,
    icon: ImageVector,
    color: Color = BurgundyPrimary
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = NeutralDark
                )
            }
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun WhyThisMatchDialog(
    match: MatchProfile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                modifier = Modifier.testTag("dismiss_why_match_button")
            ) {
                Text("Got It")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Why Prem Setu Recommended",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "${match.name} • ${match.compatibility.overallScore}% Overall Match",
                    style = MaterialTheme.typography.labelLarge,
                    color = BurgundyPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoseLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "5 Strong Compatibility Points",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BurgundyDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                match.compatibility.whyRecommendedPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TrustGreen,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Common Ground",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeutralDark
                )
                match.compatibility.commonGround.take(2).forEach { item ->
                    Text(
                        text = "✓ $item",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralLight,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun SendInterestDialog(
    match: MatchProfile,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var customMessage by remember {
        mutableStateOf("Hi ${match.name.split(" ").first()}, I found our profiles compatible on Prem Setu and would be very happy to know more about you.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSend(customMessage) },
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                modifier = Modifier.testTag("confirm_send_interest_button")
            ) {
                Text("Send Interest")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Send Interest to ${match.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TrustGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TrustGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interest Quality Engine: Meaningful first messages receive 3.5x higher responses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Personalized Introduction") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("interest_custom_message_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quick templates:",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralLight
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            customMessage = "Namaskar! Our family reviewed your profile and appreciated your family values and education."
                        },
                        label = { Text("Family-focused", fontSize = 10.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            customMessage = "Hi! I noticed we both share a passion for travel and work in leadership roles. Would love to connect!"
                        },
                        label = { Text("Career & Travel", fontSize = 10.sp) }
                    )
                }
            }
        }
    )
}
