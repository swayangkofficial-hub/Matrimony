package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

/**
 * Option item for a compatibility lifestyle quiz question.
 */
data class QuizOption(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val applyToProfile: (UserProfile) -> UserProfile
)

/**
 * Lifestyle Question definition for the compatibility matching algorithm.
 */
data class LifestyleQuestion(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val options: List<QuizOption>,
    val getCurrentSelectedId: (UserProfile) -> String
)

/**
 * List of lifestyle questions used to dynamically update user profile preferences
 * and calibrate the matrimonial compatibility matching engine.
 */
val COMPATIBILITY_QUIZ_QUESTIONS: List<LifestyleQuestion> = listOf(
    LifestyleQuestion(
        id = "family_values",
        category = "Values & Cultural Outlook",
        title = "What are your core family values & traditions?",
        subtitle = "Calibrates matches based on cultural alignment, generational harmony, and lifestyle outlook.",
        options = listOf(
            QuizOption(
                id = "Traditional",
                label = "Traditional",
                description = "Cherishes age-old family customs, joint celebrations, and strong elder guidance.",
                icon = Icons.Default.FamilyRestroom,
                applyToProfile = { it.copy(familyValues = "Traditional", familyMode = "FAMILY_DRIVEN") }
            ),
            QuizOption(
                id = "Moderate",
                label = "Moderate",
                description = "Harmonious blend of modern outlook with deep respect for Indian family traditions.",
                icon = Icons.Default.Balance,
                applyToProfile = { it.copy(familyValues = "Moderate", familyMode = "FAMILY_ASSISTED") }
            ),
            QuizOption(
                id = "Liberal",
                label = "Liberal",
                description = "Progressive, egalitarian worldview prioritizing personal growth and mutual autonomy.",
                icon = Icons.Default.Diversity3,
                applyToProfile = { it.copy(familyValues = "Liberal", familyMode = "SELF_MANAGED") }
            )
        ),
        getCurrentSelectedId = { it.familyValues.ifBlank { "Moderate" } }
    ),

    LifestyleQuestion(
        id = "dietary_preference",
        category = "Culinary & Daily Lifestyle",
        title = "What is your primary dietary preference?",
        subtitle = "Ensures domestic culinary synergy, kitchen comfort, and food habit compatibility.",
        options = listOf(
            QuizOption(
                id = "Pure Vegetarian",
                label = "Pure Vegetarian",
                description = "Strictly vegetarian meals; prefer an exclusively vegetarian kitchen at home.",
                icon = Icons.Default.Grass,
                applyToProfile = { it.copy(dietaryPreference = "Pure Vegetarian") }
            ),
            QuizOption(
                id = "Eggetarian",
                label = "Eggetarian",
                description = "Vegetarian diet including eggs; flexible when dining out or traveling.",
                icon = Icons.Default.Egg,
                applyToProfile = { it.copy(dietaryPreference = "Eggetarian") }
            ),
            QuizOption(
                id = "Non-Vegetarian",
                label = "Non-Vegetarian",
                description = "Regularly enjoy chicken, seafood, or meat both at home and outside.",
                icon = Icons.Default.Restaurant,
                applyToProfile = { it.copy(dietaryPreference = "Non-Vegetarian") }
            ),
            QuizOption(
                id = "Jain",
                label = "Jain Vegetarian",
                description = "Strict Jain principles excluding root vegetables, onion, and garlic.",
                icon = Icons.Default.Spa,
                applyToProfile = { it.copy(dietaryPreference = "Jain") }
            ),
            QuizOption(
                id = "Vegan",
                label = "Vegan",
                description = "100% plant-based lifestyle without dairy, honey, or animal derivatives.",
                icon = Icons.Default.Eco,
                applyToProfile = { it.copy(dietaryPreference = "Vegan") }
            )
        ),
        getCurrentSelectedId = { it.dietaryPreference.ifBlank { "Vegetarian" } }
    ),

    LifestyleQuestion(
        id = "living_arrangement",
        category = "Household & Living Space",
        title = "What is your post-marriage living preference?",
        subtitle = "Clarifies expectations on independent couple residence vs living with parents.",
        options = listOf(
            QuizOption(
                id = "independent",
                label = "Independent Residence",
                description = "Prefer living independently as a couple while residing reasonably close to parents.",
                icon = Icons.Default.Home,
                applyToProfile = { it.copy(livingArrangement = "Comfortable with independent residence near parents") }
            ),
            QuizOption(
                id = "joint",
                label = "Joint Family Home",
                description = "Prefer living together in a warm, connected extended/joint family household.",
                icon = Icons.Default.HolidayVillage,
                applyToProfile = { it.copy(livingArrangement = "Prefers joint family living with elder parents") }
            ),
            QuizOption(
                id = "flexible",
                label = "Flexible / Location-Based",
                description = "Open to both joint or independent living depending on career locations & mutual consensus.",
                icon = Icons.Default.AltRoute,
                applyToProfile = { it.copy(livingArrangement = "Flexible living arrangement based on mutual consensus") }
            )
        ),
        getCurrentSelectedId = {
            when {
                it.livingArrangement.contains("independent", ignoreCase = true) -> "independent"
                it.livingArrangement.contains("joint", ignoreCase = true) -> "joint"
                else -> "flexible"
            }
        }
    ),

    LifestyleQuestion(
        id = "career_expectation",
        category = "Career & Work-Life Harmony",
        title = "What are your career expectations & balance vision?",
        subtitle = "Aligns mutual support for partner ambitions, work hours, and relocation flexibility.",
        options = listOf(
            QuizOption(
                id = "ambitious",
                label = "Ambitious Dual-Career",
                description = "Both partners actively pursue demanding career growth with reciprocal encouragement.",
                icon = Icons.Default.Work,
                applyToProfile = { it.copy(careerExpectation = "Ambitious career goals with mutual support for partner's growth") }
            ),
            QuizOption(
                id = "balanced",
                label = "Balanced Work-Life",
                description = "Values a stable career with high priority on family evenings, weekends, and peace of mind.",
                icon = Icons.Default.SelfImprovement,
                applyToProfile = { it.copy(careerExpectation = "Balanced work-life rhythm, prioritizing quality family time") }
            ),
            QuizOption(
                id = "flexible_remote",
                label = "Flexible & Remote-Friendly",
                description = "Supportive of hybrid/remote work, relocations, or entrepreneurial flexibility.",
                icon = Icons.Default.LaptopMac,
                applyToProfile = { it.copy(careerExpectation = "Supportive of partner's career aspirations, open to hybrid/remote work flexibility") }
            )
        ),
        getCurrentSelectedId = {
            when {
                it.careerExpectation.contains("ambitious", ignoreCase = true) -> "ambitious"
                it.careerExpectation.contains("balanced", ignoreCase = true) -> "balanced"
                else -> "flexible_remote"
            }
        }
    ),

    LifestyleQuestion(
        id = "children_timeline",
        category = "Family Planning & Timeline",
        title = "What is your vision for children & family planning?",
        subtitle = "Harmonizes life milestones, readiness for parenthood, and family expectations.",
        options = listOf(
            QuizOption(
                id = "timeline_1_2",
                label = "Within 1-2 Years",
                description = "Ready and enthusiastic to welcome children early in married life.",
                icon = Icons.Default.ChildCare,
                applyToProfile = { it.copy(childrenTimeline = "Eager to build a warm family in 1-2 years") }
            ),
            QuizOption(
                id = "timeline_2_3",
                label = "In 2-3 Years",
                description = "Plan for children after establishing marital bonding and financial stability.",
                icon = Icons.Default.AccessTime,
                applyToProfile = { it.copy(childrenTimeline = "Wants children in 2-3 years post marriage.") }
            ),
            QuizOption(
                id = "timeline_flexible",
                label = "Open & Mutually Decided",
                description = "Flexible approach, deciding mutually as a couple without rigid timelines.",
                icon = Icons.Default.Handshake,
                applyToProfile = { it.copy(childrenTimeline = "Flexible and mutually decided when comfortable") }
            )
        ),
        getCurrentSelectedId = {
            when {
                it.childrenTimeline.contains("1-2", ignoreCase = true) -> "timeline_1_2"
                it.childrenTimeline.contains("2-3", ignoreCase = true) -> "timeline_2_3"
                else -> "timeline_flexible"
            }
        }
    )
)

/**
 * Embedded or Expandable Compatibility Quiz Card in SearchScreen.
 * Prompts users with lifestyle questions and dynamically updates their profile data
 * to refine the matrimonial compatibility matching algorithm.
 */
@Composable
fun CompatibilityQuizCard(
    userProfile: UserProfile,
    onProfileUpdated: (UserProfile) -> Unit,
    modifier: Modifier = Modifier,
    onQuizStarted: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var draftProfile by remember(userProfile) { mutableStateOf(userProfile) }
    var showSuccessFeedback by remember { mutableStateOf(false) }

    val currentQuestion = COMPATIBILITY_QUIZ_QUESTIONS.getOrNull(currentQuestionIndex)
        ?: COMPATIBILITY_QUIZ_QUESTIONS.first()
    val totalQuestions = COMPATIBILITY_QUIZ_QUESTIONS.size
    val currentSelectedOptionId = currentQuestion.getCurrentSelectedId(draftProfile)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePure),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GoldAccent, BurgundyPrimary))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("compatibility_quiz_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lifestyle Compatibility Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BurgundyDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TrustGreenLight
                        ) {
                            Text(
                                text = "Calibrator",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Refines match algorithm & compatibility scores based on your lifestyle",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                }

                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                        if (isExpanded) {
                            draftProfile = userProfile
                            onQuizStarted?.invoke()
                        }
                    },
                    modifier = Modifier.testTag("toggle_quiz_expand_btn")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Quiz" else "Expand Quiz",
                        tint = BurgundyPrimary
                    )
                }
            }

            // Quick Status Chips
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AssistChip(
                        onClick = {
                            isExpanded = true
                            currentQuestionIndex = 0
                        },
                        leadingIcon = {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyPrimary)
                        },
                        label = { Text("Values: ${userProfile.familyValues.ifBlank { "Moderate" }}", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            isExpanded = true
                            currentQuestionIndex = 1
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyPrimary)
                        },
                        label = { Text("Diet: ${userProfile.dietaryPreference.ifBlank { "Vegetarian" }}", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            isExpanded = true
                            currentQuestionIndex = 2
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyPrimary)
                        },
                        label = {
                            val liveText = when {
                                userProfile.livingArrangement.contains("independent", ignoreCase = true) -> "Independent"
                                userProfile.livingArrangement.contains("joint", ignoreCase = true) -> "Joint Family"
                                else -> "Flexible"
                            }
                            Text("Living: $liveText", fontSize = 11.sp)
                        }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            isExpanded = true
                            currentQuestionIndex = 3
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(14.dp), tint = BurgundyPrimary)
                        },
                        label = {
                            val careerText = when {
                                userProfile.careerExpectation.contains("ambitious", ignoreCase = true) -> "Dual Career"
                                userProfile.careerExpectation.contains("balanced", ignoreCase = true) -> "Work-Life"
                                else -> "Flexible"
                            }
                            Text("Career: $careerText", fontSize = 11.sp)
                        }
                    )
                }
            }

            // Success feedback notice
            AnimatedVisibility(visible = showSuccessFeedback) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TrustGreenLight,
                    border = BorderStroke(1.dp, TrustGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("quiz_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TrustGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compatibility Algorithm Calibrated! Matches updated dynamically in real-time.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // Expanded Interactive Quiz Flow
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDark
                        )
                        Text(
                            text = currentQuestion.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BurgundyPrimary,
                        trackColor = SurfaceSubtle
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Prompt
                    Text(
                        text = currentQuestion.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentQuestion.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        currentQuestion.options.forEach { option ->
                            val isSelected = currentSelectedOptionId == option.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RoseLight else SurfacePure,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) BurgundyPrimary else CardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        draftProfile = option.applyToProfile(draftProfile)
                                        // Dynamically update profile in real-time as each option is chosen!
                                        onProfileUpdated(draftProfile)
                                        showSuccessFeedback = true
                                    }
                                    .testTag("quiz_option_${option.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BurgundyPrimary else SurfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = option.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else BurgundyDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = option.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) BurgundyDark else NeutralDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = option.description,
                                            fontSize = 11.sp,
                                            color = NeutralMedium,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            draftProfile = option.applyToProfile(draftProfile)
                                            onProfileUpdated(draftProfile)
                                            showSuccessFeedback = true
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = BurgundyPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Stepper Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = { currentQuestionIndex-- },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("quiz_prev_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (currentQuestionIndex < totalQuestions - 1) {
                            Button(
                                onClick = { currentQuestionIndex++ },
                                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("quiz_next_btn")
                            ) {
                                Text("Next Question", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    onProfileUpdated(draftProfile)
                                    isExpanded = false
                                    showSuccessFeedback = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("quiz_finish_btn")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Apply & Refine Matching", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Call-to-action button when collapsed
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isExpanded = true
                        draftProfile = userProfile
                        onQuizStarted?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("take_quiz_btn")
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp), tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userProfile.familyValues.isNotBlank()) "Retake Lifestyle Compatibility Quiz" else "Take Lifestyle Compatibility Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Full-screen or Modal Bottom Sheet for taking the Compatibility Quiz with deep focus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityQuizBottomSheet(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfacePure,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("compatibility_quiz_bottom_sheet")
        ) {
            CompatibilityQuizCard(
                userProfile = userProfile,
                onProfileUpdated = { updated ->
                    onProfileUpdated(updated)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
