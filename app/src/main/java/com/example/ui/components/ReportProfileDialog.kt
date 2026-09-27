package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile
import com.example.data.service.ProfileReport
import com.example.ui.theme.*

/**
 * Categorized violation reason for reporting a matrimonial candidate.
 */
data class ReportReasonOption(
    val id: String,
    val title: String,
    val description: String,
    val severity: String,
    val icon: ImageVector
)

val REPORT_REASONS: List<ReportReasonOption> = listOf(
    ReportReasonOption(
        id = "fake_profile",
        title = "Fake / Impersonation Profile",
        description = "Using someone else's identity, stolen photos, or falsified matrimonial credentials.",
        severity = "CRITICAL",
        icon = Icons.Default.PersonOff
    ),
    ReportReasonOption(
        id = "inappropriate_photos",
        title = "Inappropriate / Offensive Photos",
        description = "Explicit, indecent, offensive, or copyright-violating photos uploaded to gallery.",
        severity = "HIGH",
        icon = Icons.Default.HideImage
    ),
    ReportReasonOption(
        id = "abusive_behavior",
        title = "Harassment or Abusive Behavior",
        description = "Disrespectful language, persistent pressure, emotional manipulation, or threats.",
        severity = "CRITICAL",
        icon = Icons.Default.Warning
    ),
    ReportReasonOption(
        id = "commercial_scam",
        title = "Commercial Solicitation / Financial Scam",
        description = "Demanding funds, dowry demands, investment proposals, crypto, or commercial ads.",
        severity = "CRITICAL",
        icon = Icons.Default.MonetizationOn
    ),
    ReportReasonOption(
        id = "misleading_details",
        title = "Falsified / Misleading Marital Details",
        description = "Inaccurate marital status, fabricated degrees, false age, or deceitful family info.",
        severity = "MEDIUM",
        icon = Icons.Default.FactCheck
    ),
    ReportReasonOption(
        id = "underage_ineligible",
        title = "Underage / Ineligible Candidate",
        description = "Candidate is below legal marriage age or ineligible under Indian matrimonial laws.",
        severity = "CRITICAL",
        icon = Icons.Default.Block
    ),
    ReportReasonOption(
        id = "other_violation",
        title = "Other Community Safety Concern",
        description = "Any other violation of Prem Setu verified matrimonial community standards.",
        severity = "MEDIUM",
        icon = Icons.Default.Flag
    )
)

/**
 * Dialog component that allows members to flag and report inappropriate content or behavior.
 * When submitted, this creates a report entry in the Firestore 'reports' collection and
 * sends an immediate alert to the admin operations dashboard.
 */
@Composable
fun ReportProfileDialog(
    reportedProfile: UserProfile,
    reporterUserId: String,
    onDismiss: () -> Unit,
    onSubmitReport: (ProfileReport) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReasonId by remember { mutableStateOf<String?>(null) }
    var descriptionText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfacePure,
            border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
            modifier = modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .testTag("report_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (isSubmittedSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("report_success_view"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TrustGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Report Submitted",
                                tint = TrustGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Report Submitted to Admin",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Thank you for safeguarding the Prem Setu community. Your report for ${reportedProfile.name} has been recorded in Firestore 'reports' and sent to the Admin Operations Dashboard for immediate review.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = BurgundyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Confidentiality Guaranteed: The candidate will never see your report. Our human verification desk reviews flagged accounts within 2–4 hours.",
                                    fontSize = 11.sp,
                                    color = NeutralMedium,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dismiss_report_success_btn")
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Report Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "${reportedProfile.name} • ID: ${reportedProfile.id.take(12)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralMedium
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("cancel_report_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NeutralMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Reason Selection and Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Please select the reason for reporting this profile:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )

                        REPORT_REASONS.forEach { reason ->
                            val isSelected = selectedReasonId == reason.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFFEF2F2) else SurfacePure,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) Color(0xFFDC2626) else CardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReasonId = reason.id }
                                    .testTag("report_reason_item_${reason.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFFDC2626) else SurfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            reason.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else NeutralDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = reason.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) Color(0xFF991B1B) else NeutralDark
                                            )
                                            if (reason.severity == "CRITICAL") {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFEE2E2)
                                                ) {
                                                    Text(
                                                        text = "Critical",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF991B1B),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = reason.description,
                                            fontSize = 11.sp,
                                            color = NeutralMedium,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedReasonId = reason.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFDC2626)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Additional notes / context field
                        Text(
                            text = "Additional context or evidence (optional):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeutralDark
                        )

                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = {
                                if (it.length <= 500) descriptionText = it
                            },
                            placeholder = {
                                Text(
                                    "Describe specific chats, photos, or incidents to aid the admin moderation team...",
                                    fontSize = 12.sp,
                                    color = NeutralLight
                                )
                            },
                            supportingText = {
                                Text(
                                    "${descriptionText.length}/500 characters",
                                    fontSize = 10.sp,
                                    color = NeutralMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_description_field")
                        )

                        // Privacy & trust badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceSubtle,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TrustGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your identity is protected. The candidate is never notified who reported them.",
                                    fontSize = 11.sp,
                                    color = NeutralMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dialog Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("report_cancel_button")
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val selected = REPORT_REASONS.find { it.id == selectedReasonId } ?: return@Button
                                isSubmitting = true
                                val report = ProfileReport(
                                    reporterUserId = reporterUserId,
                                    reportedUserId = reportedProfile.id,
                                    reportedUserName = reportedProfile.name,
                                    reason = selected.title,
                                    description = descriptionText.trim(),
                                    severity = selected.severity,
                                    status = "PENDING_REVIEW",
                                    timestamp = System.currentTimeMillis()
                                )
                                onSubmitReport(report)
                                isSubmitting = false
                                isSubmittedSuccess = true
                            },
                            enabled = selectedReasonId != null && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("submit_report_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Report", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
