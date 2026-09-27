package com.example.data.model

enum class ProfileCreatedFor(val label: String) {
    MYSELF("Myself"),
    SON("My Son"),
    DAUGHTER("My Daughter"),
    BROTHER("My Brother"),
    SISTER("My Sister"),
    RELATIVE("My Relative")
}

enum class FamilyParticipationMode(val title: String, val subtitle: String) {
    PRIVATE("Private", "Only me. Complete personal autonomy."),
    FAMILY_ASSISTED("Family Assisted", "Family can review selected matches and suggest favorites."),
    FAMILY_MANAGED("Family Managed", "Family can assist with sending and receiving interest requests.")
}

enum class MatchExplorationMode(val title: String, val description: String) {
    STRICT("Strict", "Only strong preference matches."),
    BALANCED("Balanced", "Strong matches + reasonable alternatives."),
    EXPLORE("Explore", "Broader recommendations outside narrow criteria.")
}

enum class ProfileVisibility(val label: String, val description: String) {
    PUBLIC("Public", "Visible to all verified members"),
    LIMITED("Limited", "Visible to shortlisted or matched members"),
    PRIVATE("Private", "Only visible after mutual interaction"),
    INVISIBLE("Invisible", "Search hidden but active for current conversations")
}

enum class MatchCategory(val label: String) {
    DAILY_5("Your Daily 5"),
    BEST_PREFERENCE("Best Preference"),
    MUTUAL("Mutual Matches"),
    REVERSE("Reverse Matches"),
    NEARBY("Nearby Matches"),
    NEWLY_VERIFIED("Newly Verified"),
    RECENTLY_ACTIVE("Recently Active"),
    HUMAN_RECOMMENDED("Curated by Matchmaker")
}

enum class MatchStatus {
    AVAILABLE,
    SHORTLISTED,
    INTEREST_SENT,
    ACCEPTED,
    DECLINED,
    MAYBE_LATER,
    FAMILY_INTRO_REQUESTED,
    MEETING_PLANNED,
    BLOCKED
}

data class VerificationDetail(
    val title: String,
    val isVerified: Boolean,
    val statusText: String,
    val validityPeriod: String = "Valid 12 months",
    val verifiedDate: String = "18 Sep 2026"
)

data class CompatibilityDetails(
    val overallScore: Int,
    val lifestyleScore: Int,
    val familyExpectationsScore: Int,
    val locationScore: Int,
    val careerScore: Int,
    val communicationScore: Int,
    val futureGoalsScore: Int,
    val whyRecommendedPoints: List<String>,
    val commonGround: List<String>,
    val topicsToDiscuss: List<String>
)

data class MatchProfile(
    val id: String,
    val name: String,
    val age: Int,
    val city: String,
    val state: String,
    val profession: String,
    val education: String,
    val company: String,
    val height: String,
    val religion: String,
    val community: String,
    val motherTongue: String,
    val bio: String,
    val photoAccentColor: Long,
    val trustScore: Int,
    val isIdentityVerified: Boolean,
    val isPhotoVerified: Boolean,
    val isFamilyApproved: Boolean,
    val isHumanRecommended: Boolean,
    val isMutualMatch: Boolean,
    val category: MatchCategory,
    val compatibility: CompatibilityDetails,
    val familyBackground: String,
    val livingArrangement: String,
    val careerExpectation: String,
    val childrenTimeline: String,
    var status: MatchStatus = MatchStatus.AVAILABLE,
    var customIntroNote: String = ""
)

data class FamilyMember(
    val id: String,
    val name: String,
    val relation: String, // Father, Mother, Brother, Sister, Guardian, Relative
    val phone: String,
    val email: String,
    val canReviewMatches: Boolean = true,
    val canRecommend: Boolean = true,
    val notesCount: Int = 0,
    val dateInvited: String = "Today"
)

data class JourneyStage(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val completedDate: String? = null
)

data class ChatMessage(
    val id: String = "",
    val matchId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: String = "",
    val isMine: Boolean = false,
    val isSafetyAlert: Boolean = false,
    val alertWarning: String = "",
    val senderId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MeetingPlan(
    val id: String,
    val matchId: String,
    val matchName: String,
    val partnerCity: String,
    val date: String,
    val time: String,
    val location: String,
    val meetingType: String, // "Video Meeting", "Public Place", "Family Meeting", "Matchmaker Assisted"
    val participants: String,
    val trustedContact: String,
    val isSharedWithContact: Boolean,
    var isCheckedIn: Boolean = false,
    var isCheckedOut: Boolean = false,
    var feedbackGiven: Boolean = false,
    var feedbackOutcome: String? = null // "Continue", "Need more time", "Not interested", "Request matchmaker assistance"
)

data class MatchmakerAdvisor(
    val name: String,
    val title: String,
    val experienceYears: Int,
    val successfulUnions: Int,
    val phone: String,
    val email: String,
    val specializations: List<String>
)

data class SuccessStory(
    val id: String,
    val coupleNames: String,
    val marriageDate: String,
    val location: String,
    val quote: String,
    val journeyStory: String
)

data class WeeklyReport(
    val newMatchesCount: Int,
    val viewedCount: Int,
    val interestsReceivedCount: Int,
    val interestsSentCount: Int,
    val acceptedCount: Int,
    val conversationsCount: Int,
    val profileHealthScore: Int,
    val recommendedAction: String
)
