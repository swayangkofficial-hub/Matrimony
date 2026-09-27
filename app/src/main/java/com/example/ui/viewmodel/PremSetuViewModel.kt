package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.PremSetuRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppDestination(val title: String) {
    HOME("Home"),
    DISCOVERY("Matches"),
    JOURNEY("Journey OS"),
    FAMILY("Family Connect"),
    CHAT("Messages"),
    MORE("Services & Safety")
}

data class FilterState(
    val showOnlyVerifiedIdentity: Boolean = false,
    val showOnlyVerifiedPhoto: Boolean = false,
    val showOnlyFamilyApproved: Boolean = false,
    val showOnlyMutual: Boolean = false,
    val showOnlyHumanRecommended: Boolean = false,
    val selectedCity: String = "All Cities",
    val searchQuery: String = ""
)

class PremSetuViewModel(
    private val repository: PremSetuRepository = PremSetuRepository()
) : ViewModel() {

    val userProfileName = repository.userProfileName
    val userBio = repository.userBio
    val trustScore = repository.trustScore
    val profileHealthScore = repository.profileHealthScore
    val familyMode = repository.familyMode
    val explorationMode = repository.explorationMode
    val visibilityMode = repository.visibilityMode
    val womenExtraPrivacy = repository.womenExtraPrivacy
    val membershipTier = repository.membershipTier
    val isMatchmakingPaused = repository.isMatchmakingPaused
    val matches = repository.matches
    val familyMembers = repository.familyMembers
    val journeyStages = repository.journeyStages
    val meetings = repository.meetings
    val chats = repository.chats
    val sharedContacts = repository.sharedContacts
    val verifications = repository.verifications
    val dedicatedMatchmaker = repository.dedicatedMatchmaker
    val weeklyReport = repository.weeklyReport
    val successStories = repository.successStories

    // Navigation and Modals
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _showLoginScreen = MutableStateFlow(false)
    val showLoginScreen: StateFlow<Boolean> = _showLoginScreen.asStateFlow()

    private val _showUserProfileScreen = MutableStateFlow(false)
    val showUserProfileScreen: StateFlow<Boolean> = _showUserProfileScreen.asStateFlow()

    private val _showSearchScreen = MutableStateFlow(false)
    val showSearchScreen: StateFlow<Boolean> = _showSearchScreen.asStateFlow()

    private val _showEmailCenterScreen = MutableStateFlow(false)
    val showEmailCenterScreen: StateFlow<Boolean> = _showEmailCenterScreen.asStateFlow()

    private val _showPrivacySettingsScreen = MutableStateFlow(false)
    val showPrivacySettingsScreen: StateFlow<Boolean> = _showPrivacySettingsScreen.asStateFlow()

    val emailRepository = com.example.data.repository.EmailRepository()
    val zohoEmailService = emailRepository.emailService
    val emailLogs = emailRepository.emailLogs
    val verifiedEmails = emailRepository.verifiedEmails

    val dailyDigestService = com.example.data.service.DailyDigestNotificationService.getInstance(emailRepository)
    val digestSettings = dailyDigestService.settings
    val lastDigestResult = dailyDigestService.lastDigestResult
    val digestHistory = dailyDigestService.digestHistory
    val isDigestDispatching = dailyDigestService.isDispatching

    private val _isSendingEmail = MutableStateFlow(false)
    val isSendingEmail: StateFlow<Boolean> = _isSendingEmail.asStateFlow()

    val userProfileRepository = com.example.data.repository.UserProfileRepository()
    val chatRepository = com.example.data.repository.ChatRepository()

    private val _currentUserProfile = MutableStateFlow(
        UserProfile(
            id = "user_default_001",
            name = "Aarav Sen",
            age = 29,
            gender = "Male",
            city = "Kolkata",
            state = "West Bengal",
            profession = "Senior Software Architect",
            education = "B.Tech (Computer Science), IIT Kharagpur",
            company = "Tech Innovations Pvt Ltd",
            height = "5 ft 11 in",
            religion = "Hindu",
            community = "Bengali Brahmin",
            motherTongue = "Bengali",
            bio = "Senior Software Architect based in Kolkata. Passionate about technology, classical Indian literature, weekend road trips, and deeply value strong family roots and mutual respect in a marriage.",
            profileCreatedFor = "Myself",
            trustScore = 87,
            profileHealthScore = 86,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            womenExtraPrivacy = false,
            membershipTier = "Assist Plan",
            phone = "+91 98301 23456",
            email = "aarav.sen@example.com",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&auto=format&fit=crop&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=800&auto=format&fit=crop&q=80"
            ),
            familyBackground = "Father is a retired bank manager, mother is a teacher. 1 younger sister married in Pune.",
            livingArrangement = "Open to living with family or independently based on mutual consensus.",
            careerExpectation = "Supportive of partner's career aspirations, open to hybrid/remote work flexibility.",
            childrenTimeline = "Planning for children in 2-3 years post marriage.",
            familyValues = "Moderate",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Vegetarian"
        )
    )
    val currentUserProfile: StateFlow<UserProfile> = _currentUserProfile.asStateFlow()

    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        } catch (e: Throwable) {
            null
        }
    )
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _selectedMatchForDetail = MutableStateFlow<MatchProfile?>(null)
    val selectedMatchForDetail: StateFlow<MatchProfile?> = _selectedMatchForDetail.asStateFlow()

    private val _activeChatMatch = MutableStateFlow<MatchProfile?>(null)
    val activeChatMatch: StateFlow<MatchProfile?> = _activeChatMatch.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Sub-Screen inside MORE tab
    private val _moreActiveSection = MutableStateFlow("HUB") // HUB, VERIFICATION, SAFETY, MATCHMAKER_CRM, MEMBERSHIP, AI_BUILDER
    val moreActiveSection: StateFlow<String> = _moreActiveSection.asStateFlow()

    // Filtered Matches
    val filteredMatches: StateFlow<List<MatchProfile>> = combine(matches, filterState) { matchList, filters ->
        matchList.filter { match ->
            val matchesQuery = filters.searchQuery.isEmpty() ||
                    match.name.contains(filters.searchQuery, ignoreCase = true) ||
                    match.profession.contains(filters.searchQuery, ignoreCase = true) ||
                    match.city.contains(filters.searchQuery, ignoreCase = true)

            val matchesCity = filters.selectedCity == "All Cities" || match.city.equals(filters.selectedCity, ignoreCase = true)
            val matchesIdVer = !filters.showOnlyVerifiedIdentity || match.isIdentityVerified
            val matchesPhotoVer = !filters.showOnlyVerifiedPhoto || match.isPhotoVerified
            val matchesFamilyAppr = !filters.showOnlyFamilyApproved || match.isFamilyApproved
            val matchesMutual = !filters.showOnlyMutual || match.isMutualMatch
            val matchesHuman = !filters.showOnlyHumanRecommended || match.isHumanRecommended

            matchesQuery && matchesCity && matchesIdVer && matchesPhotoVer && matchesFamilyAppr && matchesMutual && matchesHuman
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
        // Reset modals
        if (dest != AppDestination.CHAT) {
            _activeChatMatch.value = null
        }
    }

    fun openMatchDetail(match: MatchProfile) {
        _selectedMatchForDetail.value = match
    }

    fun closeMatchDetail() {
        _selectedMatchForDetail.value = null
    }

    fun openChatWith(match: MatchProfile) {
        _activeChatMatch.value = match
        _currentDestination.value = AppDestination.CHAT
    }

    fun closeChat() {
        _activeChatMatch.value = null
    }

    fun openLoginScreen() {
        _showLoginScreen.value = true
    }

    fun closeLoginScreen() {
        _showLoginScreen.value = false
    }

    fun openUserProfileScreen() {
        _showUserProfileScreen.value = true
    }

    fun closeUserProfileScreen() {
        _showUserProfileScreen.value = false
    }

    fun openSearchScreen() {
        _showSearchScreen.value = true
    }

    fun closeSearchScreen() {
        _showSearchScreen.value = false
    }

    fun openEmailCenterScreen() {
        _showEmailCenterScreen.value = true
    }

    fun closeEmailCenterScreen() {
        _showEmailCenterScreen.value = false
    }

    fun openPrivacySettingsScreen() {
        _showPrivacySettingsScreen.value = true
    }

    fun closePrivacySettingsScreen() {
        _showPrivacySettingsScreen.value = false
    }

    fun sendOtp(
        email: String,
        name: String,
        onResult: (Boolean, String) -> Unit
    ) {
        _isSendingEmail.value = true
        viewModelScope.launch {
            val res = emailRepository.sendOtpVerificationEmail(email, name)
            _isSendingEmail.value = false
            res.onSuccess { otpRes ->
                if (otpRes.success) {
                    onResult(true, "OTP sent to $email via Zoho ZeptoMail")
                } else {
                    onResult(false, "Delivery response: ${otpRes.deliveryResult.message} (${otpRes.deliveryResult.statusCode})")
                }
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to connect to Zoho ZeptoMail server")
            }
        }
    }

    fun verifyOtp(
        email: String,
        otp: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val status = emailRepository.verifyOtpWithStatus(email, otp)
        when (status) {
            com.example.data.service.OtpVerificationStatus.SUCCESS -> {
                // Update profile verified status
                val updated = _currentUserProfile.value.copy(
                    isIdentityVerified = true,
                    trustScore = (_currentUserProfile.value.trustScore + 5).coerceAtMost(100),
                    email = email
                )
                saveUserProfile(updated)
                onResult(true, "Email verified successfully! Trust Score boosted.")
            }
            com.example.data.service.OtpVerificationStatus.INVALID_CODE -> {
                onResult(false, "Invalid verification code. Please check and retry.")
            }
            com.example.data.service.OtpVerificationStatus.EXPIRED_OR_NOT_FOUND -> {
                onResult(false, "Verification code expired or not found. Please request a new OTP.")
            }
            com.example.data.service.OtpVerificationStatus.MAX_ATTEMPTS_EXCEEDED -> {
                onResult(false, "Maximum attempts exceeded. Please request a new OTP.")
            }
        }
    }

    fun sendWelcomeEmail(
        email: String,
        name: String,
        onResult: (Boolean, String) -> Unit
    ) {
        _isSendingEmail.value = true
        viewModelScope.launch {
            val res = emailRepository.sendRegistrationWelcomeEmail(email, name)
            _isSendingEmail.value = false
            res.onSuccess { delivery ->
                onResult(delivery.success, "Welcome email sent! (${delivery.message})")
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to send welcome email")
            }
        }
    }

    fun sendInterestNotification(
        email: String,
        name: String,
        senderName: String,
        senderCity: String = "Kolkata",
        senderProfession: String = "VP - Portfolio Strategy",
        compatibilityScore: Int = 94,
        onResult: (Boolean, String) -> Unit
    ) {
        _isSendingEmail.value = true
        viewModelScope.launch {
            val res = zohoEmailService.sendInterestNotificationEmail(
                toEmail = email,
                toName = name,
                senderName = senderName,
                senderCity = senderCity,
                senderProfession = senderProfession,
                compatibilityScore = compatibilityScore
            )
            _isSendingEmail.value = false
            res.onSuccess { delivery ->
                onResult(delivery.success, "Interest notification sent! (${delivery.message})")
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to send interest notification")
            }
        }
    }

    fun sendMeetingConfirmation(
        email: String,
        name: String,
        partnerName: String,
        meetingDateTime: String,
        venueName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        _isSendingEmail.value = true
        viewModelScope.launch {
            val res = zohoEmailService.sendMeetingConfirmationEmail(
                toEmail = email,
                toName = name,
                partnerName = partnerName,
                meetingDateTime = meetingDateTime,
                venueName = venueName
            )
            _isSendingEmail.value = false
            res.onSuccess { delivery ->
                onResult(delivery.success, "Meeting confirmation alert sent! (${delivery.message})")
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to send meeting confirmation")
            }
        }
    }

    /**
     * Dispatches a Daily Digest email summarizing new match requests using EmailRepository.
     */
    fun sendDailyDigest(
        email: String = _currentUserProfile.value.email.ifBlank { "member@technope.co.in" },
        name: String = _currentUserProfile.value.name.ifBlank { "Prem Setu Member" },
        matchRequests: List<MatchProfile>? = null,
        context: android.content.Context? = null,
        forceSend: Boolean = true,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val requests = matchRequests ?: dailyDigestService.resolveMatchRequestsForDigest(matches.value)
            val res = dailyDigestService.sendDailyDigest(
                recipientEmail = email,
                recipientName = name,
                matchRequests = requests,
                context = context,
                forceSend = forceSend
            )
            res.onSuccess { digestRes ->
                onResult(
                    digestRes.success,
                    "Daily digest dispatched to $email with ${digestRes.matchRequestsCount} match requests summarized!"
                )
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to dispatch daily digest email")
            }
        }
    }

    fun updateDailyDigestSettings(settings: com.example.data.service.DailyDigestSettings) {
        dailyDigestService.updateSettings(settings)
    }

    fun saveUserProfile(profile: UserProfile, onComplete: (Boolean) -> Unit = {}) {
        _currentUserProfile.value = profile
        repository.updateBio(profile.bio)
        repository.updateUserName(profile.name)
        viewModelScope.launch {
            val res = userProfileRepository.saveUserProfile(profile)
            onComplete(res.isSuccess)
        }
    }

    fun onUserSignedIn(user: com.google.firebase.auth.FirebaseUser?) {
        _currentUser.value = user
        _showLoginScreen.value = false
    }

    fun signOut() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (e: Throwable) {
            // Handled gracefully
        }
        _currentUser.value = null
    }

    fun setMoreActiveSection(section: String) {
        _moreActiveSection.value = section
    }

    fun updateFilters(update: FilterState.() -> FilterState) {
        _filterState.update { it.update() }
    }

    fun sendInterest(matchId: String, customIntro: String = "") {
        repository.sendInterest(matchId, customIntro)
        _selectedMatchForDetail.update { if (it?.id == matchId) it?.copy(status = MatchStatus.INTEREST_SENT) else it }
    }

    fun toggleShortlist(matchId: String) {
        repository.toggleShortlist(matchId)
        _selectedMatchForDetail.update {
            if (it?.id == matchId) {
                val newStatus = if (it.status == MatchStatus.SHORTLISTED) MatchStatus.AVAILABLE else MatchStatus.SHORTLISTED
                it?.copy(status = newStatus)
            } else it
        }
    }

    fun respondToInterest(matchId: String, accept: Boolean) {
        repository.respondToInterest(matchId, accept)
    }

    fun getChatMessages(matchId: String): Flow<List<ChatMessage>> {
        val currentUserId = _currentUser.value?.uid ?: _currentUserProfile.value.id
        val conversationId = com.example.data.repository.ChatRepository.getConversationId(currentUserId, matchId)
        val initial = repository.chats.value[matchId] ?: emptyList()
        chatRepository.seedInitialMessagesIfEmpty(conversationId, matchId, "", initial)
        return chatRepository.getMessages(conversationId, currentUserId)
    }

    val blockedUsers: StateFlow<List<String>> = _currentUserProfile
        .map { it.blockedUsers }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun isUserBlocked(targetUserId: String): Boolean {
        val currentUserId = _currentUser.value?.uid ?: _currentUserProfile.value.id
        return _currentUserProfile.value.blockedUsers.contains(targetUserId) || chatRepository.isUserBlocked(currentUserId, targetUserId)
    }

    fun blockUser(
        targetUserId: String,
        reason: String = "Inappropriate communication",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val currentUserId = _currentUser.value?.uid ?: _currentUserProfile.value.id
        viewModelScope.launch {
            val res = chatRepository.blockUser(currentUserId, targetUserId, reason)
            userProfileRepository.blockUser(currentUserId, targetUserId)

            val updatedBlocked = (_currentUserProfile.value.blockedUsers + targetUserId).distinct()
            _currentUserProfile.update { it.copy(blockedUsers = updatedBlocked) }
            repository.updateMatchStatus(targetUserId, MatchStatus.BLOCKED)
            _selectedMatchForDetail.update { if (it?.id == targetUserId) it?.copy(status = MatchStatus.BLOCKED) else it }

            onResult(true, "User blocked. Communication is prevented.")
        }
    }

    fun unblockUser(
        targetUserId: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val currentUserId = _currentUser.value?.uid ?: _currentUserProfile.value.id
        viewModelScope.launch {
            val res = chatRepository.unblockUser(currentUserId, targetUserId)
            userProfileRepository.unblockUser(currentUserId, targetUserId)

            val updatedBlocked = _currentUserProfile.value.blockedUsers.filter { it != targetUserId }
            _currentUserProfile.update { it.copy(blockedUsers = updatedBlocked) }
            repository.updateMatchStatus(targetUserId, MatchStatus.AVAILABLE)
            _selectedMatchForDetail.update { if (it?.id == targetUserId) it?.copy(status = MatchStatus.AVAILABLE) else it }

            onResult(true, "User unblocked successfully.")
        }
    }

    fun sendMessage(matchId: String, text: String) {
        if (text.isNotBlank()) {
            val trimmed = text.trim()
            if (isUserBlocked(matchId)) {
                return
            }
            repository.sendMessage(matchId, trimmed)
            viewModelScope.launch {
                val currentUserId = _currentUser.value?.uid ?: _currentUserProfile.value.id
                val currentUserName = _currentUserProfile.value.name
                val conversationId = com.example.data.repository.ChatRepository.getConversationId(currentUserId, matchId)
                chatRepository.sendMessage(
                    conversationId = conversationId,
                    matchId = matchId,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    text = trimmed
                )
            }
        }
    }

    fun requestContactShare(matchId: String) {
        repository.requestContactShare(matchId)
    }

    fun updateFamilyMode(mode: FamilyParticipationMode) {
        repository.updateFamilyMode(mode)
    }

    fun updateExplorationMode(mode: MatchExplorationMode) {
        repository.updateExplorationMode(mode)
    }

    fun updateVisibility(visibility: ProfileVisibility) {
        repository.updateVisibility(visibility)
    }

    fun toggleWomenExtraPrivacy(enabled: Boolean) {
        repository.toggleWomenExtraPrivacy(enabled)
    }

    fun togglePauseMatchmaking() {
        repository.togglePauseMatchmaking()
    }

    fun addFamilyMember(name: String, relation: String, phone: String, email: String) {
        repository.addFamilyMember(name, relation, phone, email)
    }

    fun scheduleMeeting(meeting: MeetingPlan) {
        repository.scheduleMeeting(meeting)
    }

    fun checkInMeeting(meetingId: String) {
        repository.checkInMeeting(meetingId)
    }

    fun checkOutMeeting(meetingId: String) {
        repository.checkOutMeeting(meetingId)
    }

    fun submitMeetingFeedback(meetingId: String, outcome: String) {
        repository.submitMeetingFeedback(meetingId, outcome)
    }

    fun applyAIBio(style: String) {
        val generatedBio = when (style) {
            "Simple" -> "I am a simple and grounded software professional based in Kolkata. I value sincerity, spend free time reading and traveling, and cherish close family ties."
            "Professional" -> "Accomplished Senior Software Architect with a passion for building scalable systems. Balance ambitious career milestones with cultural appreciation, fitness, and balanced family life."
            "Traditional" -> "Rooted in timeless cultural and family values with a modern outlook. Respectful of elders, fond of Indian heritage and classical music, seeking a life partner to build a harmonious future together."
            "Modern" -> "Independent tech leader with a curious mind. Love progressive discussions, indie films, marathon running, and looking for an equal companion with warmth and emotional intelligence."
            "Family-friendly" -> "Belong to a close-knit, loving family based in Kolkata. We believe in mutual respect, celebrating festivals together, and supporting both partners' personal and professional aspirations."
            else -> "Software architect in Kolkata passionate about innovation, travel, and warm family bonds."
        }
        repository.updateBio(generatedBio)
    }

    fun updateMembership(tier: String) {
        repository.updateMembership(tier)
    }
}
