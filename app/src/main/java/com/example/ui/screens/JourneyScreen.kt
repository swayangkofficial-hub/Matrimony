package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.PremSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(
    viewModel: PremSetuViewModel,
    modifier: Modifier = Modifier
) {
    val journeyStages by viewModel.journeyStages.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val weeklyReport = viewModel.weeklyReport

    var showScheduleMeetingModal by remember { mutableStateOf(false) }
    var selectedMeetingForFeedback by remember { mutableStateOf<MeetingPlan?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("journey_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Matchmaking OS Header
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
                                text = "MATCHMAKING OS",
                                color = BurgundyDeep,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "7 of 11 Completed",
                            color = RoseBlush,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Relationship Journey Engine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your matrimonial journey from discovery to lifelong union with family participation & meeting safety.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoseLight)
                    )
                }
            }
        }

        // Active Meetings & Safety Check-In Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
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
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = BurgundyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Meeting Planner & Safety Check-In",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                        }

                        IconButton(
                            onClick = { showScheduleMeetingModal = true },
                            modifier = Modifier.testTag("schedule_meeting_btn")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Meeting", tint = BurgundyPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("SAFE_VENUES")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Safe Venues (Maps)", fontSize = 11.sp, color = BurgundyPrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(AppDestination.MORE)
                                viewModel.setMoreActiveSection("LIVE_VOICE")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Voice Coach", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (meetings.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceSubtle,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                text = "No upcoming meetings scheduled. Click + to schedule a verified video or public meeting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        meetings.forEach { meeting ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Meeting with ${meeting.matchName}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BurgundyDark
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GoldLight
                                        ) {
                                            Text(
                                                text = meeting.meetingType,
                                                color = BurgundyDeep,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📅 ${meeting.date} at ${meeting.time}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = NeutralDark
                                    )
                                    Text(
                                        text = "📍 ${meeting.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralMedium
                                    )
                                    Text(
                                        text = "🛡️ Trusted Safety Contact: ${meeting.trustedContact}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TrustTeal
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Safety Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (!meeting.isCheckedIn) {
                                            Button(
                                                onClick = { viewModel.checkInMeeting(meeting.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = TrustGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp).testTag("check_in_btn")
                                            ) {
                                                Icon(Icons.Default.LocationSearching, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Safety Check-In", fontSize = 11.sp)
                                            }
                                        } else if (!meeting.isCheckedOut) {
                                            Button(
                                                onClick = { viewModel.checkOutMeeting(meeting.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp).testTag("check_out_btn")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Check-Out Safe", fontSize = 11.sp)
                                            }
                                        }

                                        if (!meeting.feedbackGiven) {
                                            OutlinedButton(
                                                onClick = { selectedMeetingForFeedback = meeting },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp).testTag("post_meeting_feedback_btn")
                                            ) {
                                                Text("Private Feedback", fontSize = 11.sp)
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = RoseLight,
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "Outcome: ${meeting.feedbackOutcome ?: "Recorded"}",
                                                        fontSize = 11.sp,
                                                        color = BurgundyDark,
                                                        fontWeight = FontWeight.Bold
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

        // 11 Stages of Matchmaking OS (Step 78)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Matchmaking Pipeline Steps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    journeyStages.forEachIndexed { index, stage ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        stage.isCompleted -> TrustGreen
                                        stage.isCurrent -> BurgundyPrimary
                                        else -> SurfaceSubtle
                                    },
                                    border = if (!stage.isCompleted && !stage.isCurrent) androidx.compose.foundation.BorderStroke(1.dp, CardBorder) else null,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (stage.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${stage.stepNumber}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (stage.isCurrent) Color.White else NeutralLight
                                            )
                                        }
                                    }
                                }

                                if (index < journeyStages.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(38.dp)
                                            .background(if (stage.isCompleted) TrustGreen else CardBorder)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stage.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (stage.isCurrent) BurgundyPrimary else NeutralDark
                                    )
                                    if (stage.completedDate != null) {
                                        Text(
                                            text = stage.completedDate,
                                            fontSize = 10.sp,
                                            color = NeutralLight
                                        )
                                    }
                                }
                                Text(
                                    text = stage.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Weekly Match Report (Step 71 in PDF)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
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
                                text = "Weekly Match Report",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NeutralDark
                            )
                            Text(
                                text = "Updated Every Sunday",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralLight
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldLight
                        ) {
                            Text(
                                text = "Health ${weeklyReport.profileHealthScore}%",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDeep,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportMetric("New Matches", "${weeklyReport.newMatchesCount}", Modifier.weight(1f))
                        ReportMetric("Viewed", "${weeklyReport.viewedCount}", Modifier.weight(1f))
                        ReportMetric("Received", "${weeklyReport.interestsReceivedCount}", Modifier.weight(1f))
                        ReportMetric("Accepted", "${weeklyReport.acceptedCount}", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceSubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recommended Action: ${weeklyReport.recommendedAction}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralDark
                            )
                        }
                    }
                }
            }
        }
    }

    // Schedule Meeting Dialog
    if (showScheduleMeetingModal) {
        var matchName by remember { mutableStateOf("Priya Mukherjee") }
        var date by remember { mutableStateOf("Sunday, 4 Oct 2026") }
        var time by remember { mutableStateOf("5:00 PM IST") }
        var location by remember { mutableStateOf("Cafe Coffee Day, Salt Lake Sector 5, Kolkata") }
        var meetingType by remember { mutableStateOf("Public Place") }
        var trustedContact by remember { mutableStateOf("Sunil Sen (Father - +91 94330 11223)") }

        AlertDialog(
            onDismissRequest = { showScheduleMeetingModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        val newMeeting = MeetingPlan(
                            id = "meet_${System.currentTimeMillis()}",
                            matchId = "p1",
                            matchName = matchName,
                            partnerCity = "Kolkata",
                            date = date,
                            time = time,
                            location = location,
                            meetingType = meetingType,
                            participants = "Candidate & Connection",
                            trustedContact = trustedContact,
                            isSharedWithContact = true
                        )
                        viewModel.scheduleMeeting(newMeeting)
                        showScheduleMeetingModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    modifier = Modifier.testTag("confirm_create_meeting_btn")
                ) {
                    Text("Schedule & Enable Safety Check")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleMeetingModal = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "First Meeting Planner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Public Venue / Video Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Meeting Type:", style = MaterialTheme.typography.labelSmall, color = NeutralDark)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Public Place", "Video Meeting", "Family Meeting", "Matchmaker Assisted").forEach { type ->
                            FilterChip(
                                selected = meetingType == type,
                                onClick = { meetingType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = trustedContact,
                        onValueChange = { trustedContact = it },
                        label = { Text("Trusted Contact (receives itinerary)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // Post Meeting Private Feedback Dialog (Step 29 in PDF)
    selectedMeetingForFeedback?.let { meeting ->
        AlertDialog(
            onDismissRequest = { selectedMeetingForFeedback = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMeetingForFeedback = null }) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "Post-Meeting Private Feedback",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "How did your meeting with ${meeting.matchName} go? (Your feedback remains 100% private)",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        "Continue - We had great chemistry",
                        "Need more time - Want another meeting",
                        "Not interested - Politely move on",
                        "Request matchmaker assistance"
                    ).forEach { outcome ->
                        Button(
                            onClick = {
                                viewModel.submitMeetingFeedback(meeting.id, outcome)
                                selectedMeetingForFeedback = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceSubtle),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = outcome,
                                color = BurgundyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ReportMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceSubtle
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BurgundyPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = NeutralMedium
            )
        }
    }
}
