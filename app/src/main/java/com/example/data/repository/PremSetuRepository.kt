package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PremSetuRepository {

    // Current User Profile State
    private val _userProfileName = MutableStateFlow("Aarav Sen")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _userBio = MutableStateFlow(
        "Senior Software Architect based in Kolkata. Passionate about technology, classical Indian literature, weekend road trips, and deeply value strong family roots and mutual respect in a marriage."
    )
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _trustScore = MutableStateFlow(87)
    val trustScore: StateFlow<Int> = _trustScore.asStateFlow()

    private val _profileHealthScore = MutableStateFlow(86)
    val profileHealthScore: StateFlow<Int> = _profileHealthScore.asStateFlow()

    private val _familyMode = MutableStateFlow(FamilyParticipationMode.FAMILY_ASSISTED)
    val familyMode: StateFlow<FamilyParticipationMode> = _familyMode.asStateFlow()

    private val _explorationMode = MutableStateFlow(MatchExplorationMode.BALANCED)
    val explorationMode: StateFlow<MatchExplorationMode> = _explorationMode.asStateFlow()

    private val _visibilityMode = MutableStateFlow(ProfileVisibility.PUBLIC)
    val visibilityMode: StateFlow<ProfileVisibility> = _visibilityMode.asStateFlow()

    private val _womenExtraPrivacy = MutableStateFlow(false)
    val womenExtraPrivacy: StateFlow<Boolean> = _womenExtraPrivacy.asStateFlow()

    private val _membershipTier = MutableStateFlow("Assist Plan")
    val membershipTier: StateFlow<String> = _membershipTier.asStateFlow()

    private val _isMatchmakingPaused = MutableStateFlow(false)
    val isMatchmakingPaused: StateFlow<Boolean> = _isMatchmakingPaused.asStateFlow()

    // Matches List
    private val _matches = MutableStateFlow<List<MatchProfile>>(emptyList())
    val matches: StateFlow<List<MatchProfile>> = _matches.asStateFlow()

    // Family Members
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    // Journey Stages
    private val _journeyStages = MutableStateFlow<List<JourneyStage>>(emptyList())
    val journeyStages: StateFlow<List<JourneyStage>> = _journeyStages.asStateFlow()

    // Meetings
    private val _meetings = MutableStateFlow<List<MeetingPlan>>(emptyList())
    val meetings: StateFlow<List<MeetingPlan>> = _meetings.asStateFlow()

    // Chats mapped by Match ID
    private val _chats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chats: StateFlow<Map<String, List<ChatMessage>>> = _chats.asStateFlow()

    // Contact sharing consents
    private val _sharedContacts = MutableStateFlow<Set<String>>(emptySet())
    val sharedContacts: StateFlow<Set<String>> = _sharedContacts.asStateFlow()

    // Verification Items
    private val _verifications = MutableStateFlow<List<VerificationDetail>>(emptyList())
    val verifications: StateFlow<List<VerificationDetail>> = _verifications.asStateFlow()

    // Assigned Matchmaker
    val dedicatedMatchmaker = MatchmakerAdvisor(
        name = "Ananya Mukherjee",
        title = "Senior Relationship Advisor",
        experienceYears = 9,
        successfulUnions = 480,
        phone = "+91 98300 24890",
        email = "ananya.m@premsetu.com",
        specializations = listOf("Bengali & North-Eastern Matrimony", "Working Professionals", "Family Mediation")
    )

    // Weekly Report
    val weeklyReport = WeeklyReport(
        newMatchesCount = 24,
        viewedCount = 18,
        interestsReceivedCount = 7,
        interestsSentCount = 5,
        acceptedCount = 2,
        conversationsCount = 2,
        profileHealthScore = 86,
        recommendedAction = "Complete employment verification to reach 95% trust score"
    )

    // Success Stories
    val successStories = listOf(
        SuccessStory(
            id = "s1",
            coupleNames = "Debanjan & Swagata",
            marriageDate = "February 2026",
            location = "Kolkata, West Bengal",
            quote = "Prem Setu made our parents feel included without invading our personal space.",
            journeyStory = "We connected through Prem Setu's Family Assisted mode. After reviewing our detailed Compatibility Profile, both our families arranged an initial video meeting facilitated by Ananya from Prem Setu. Six months later, we tied the knot!"
        ),
        SuccessStory(
            id = "s2",
            coupleNames = "Vikram & Tanvi",
            marriageDate = "November 2025",
            location = "Bengaluru, Karnataka",
            quote = "The 'Why This Match' breakdown saved us from awkward mismatched expectations.",
            journeyStory = "Both of us were clear about career continuity and relocation flexibility. The 5-point match explanation highlighted our mutual alignment on finances and living preferences instantly."
        ),
        SuccessStory(
            id = "s3",
            coupleNames = "Ritwik & Priyanka",
            marriageDate = "January 2026",
            location = "Pune, Maharashtra",
            quote = "Safe Connect gave my daughter full confidence to chat safely.",
            journeyStory = "Priyanka's father Sunil had initiated the search through Prem Setu's Family Desk. The platform's verified identity time-stamp and Safe Chat mode provided total peace of mind."
        )
    )

    init {
        initializeInitialData()
    }

    private fun initializeInitialData() {
        // Initial Verifications
        _verifications.value = listOf(
            VerificationDetail("Mobile Number", true, "Verified via OTP", "Permanent", "12 Sep 2026"),
            VerificationDetail("Work & Personal Email", true, "Verified corporate & personal", "Permanent", "12 Sep 2026"),
            VerificationDetail("Government ID (Aadhaar / Passport)", true, "Verified with Digital Locker", "Valid until 18 Sep 2027", "18 Sep 2026"),
            VerificationDetail("Recent Portrait Photo", true, "AI Face Verification Passed", "Valid 6 months", "15 Sep 2026"),
            VerificationDetail("Higher Education Degree", true, "IIT Kharagpur Verified", "Permanent", "16 Sep 2026"),
            VerificationDetail("Employment & Profession", true, "Company ID & Pay verification", "Valid 12 months", "16 Sep 2026"),
            VerificationDetail("Family Background Information", true, "Family Desk Checked", "Permanent", "17 Sep 2026")
        )

        // Initial Family Members
        _familyMembers.value = listOf(
            FamilyMember("fam1", "Sunil Sen", "Father", "+91 94330 11223", "sunil.sen@gmail.com", canReviewMatches = true, canRecommend = true, notesCount = 4),
            FamilyMember("fam2", "Gayatri Sen", "Mother", "+91 94330 11224", "gayatri.sen@gmail.com", canReviewMatches = true, canRecommend = true, notesCount = 2),
            FamilyMember("fam3", "Rohan Sen", "Brother", "+91 98310 99887", "rohan.sen@outlook.com", canReviewMatches = true, canRecommend = false, notesCount = 1)
        )

        // Initial Journey Stages
        _journeyStages.value = listOf(
            JourneyStage(1, "Profile Created", "Basic biodata, education, and career details saved.", isCompleted = true, isCurrent = false, completedDate = "12 Sep"),
            JourneyStage(2, "Profile Verified", "Trust score 87/100 unlocked with Govt ID and photo checks.", isCompleted = true, isCurrent = false, completedDate = "14 Sep"),
            JourneyStage(3, "Preferences Completed", "Life values, marriage priorities, and partner expectations defined.", isCompleted = true, isCurrent = false, completedDate = "15 Sep"),
            JourneyStage(4, "Matches Found", "Algorithm matched 12 verified profiles matching criteria.", isCompleted = true, isCurrent = false, completedDate = "16 Sep"),
            JourneyStage(5, "Interests Sent", "Sent personalized introductions to 4 prospective matches.", isCompleted = true, isCurrent = false, completedDate = "17 Sep"),
            JourneyStage(6, "Interests Accepted", "2 mutual interests received from Priya Mukherjee & Sneha Banerjee.", isCompleted = true, isCurrent = false, completedDate = "19 Sep"),
            JourneyStage(7, "Conversation Started", "Secure chat unlocked in Safe Connect mode with Priya Mukherjee.", isCompleted = true, isCurrent = true, completedDate = "20 Sep"),
            JourneyStage(8, "Family Introduction", "Family members invited to joint video conference call.", isCompleted = false, isCurrent = false),
            JourneyStage(9, "First Meeting Planned", "Public coffee meeting scheduled with Safety Check-In.", isCompleted = false, isCurrent = false),
            JourneyStage(10, "Considering & Relationship", "Exclusive evaluation and discussions on long term goals.", isCompleted = false, isCurrent = false),
            JourneyStage(11, "Success & Marriage", "Formalized engagement and marriage milestone celebration.", isCompleted = false, isCurrent = false)
        )

        // Initial Profiles
        _matches.value = listOf(
            MatchProfile(
                id = "p1",
                name = "Priya Mukherjee",
                age = 28,
                city = "Kolkata",
                state = "West Bengal",
                profession = "VP - Portfolio Strategy",
                education = "MBA (Finance), IIM Calcutta",
                company = "HSBC Global Private Banking",
                height = "5' 5\"",
                religion = "Hindu",
                community = "Brahmin",
                motherTongue = "Bengali",
                bio = "Compassionate, driven, and family-oriented. I love literature, exploring heritage cafe culture, Hindustani classical music, and value clear communication and empathy above all.",
                photoAccentColor = 0xFFBE185D,
                trustScore = 94,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = true,
                isHumanRecommended = true,
                isMutualMatch = true,
                category = MatchCategory.HUMAN_RECOMMENDED,
                compatibility = CompatibilityDetails(
                    overallScore = 92,
                    lifestyleScore = 91,
                    familyExpectationsScore = 88,
                    locationScore = 95,
                    careerScore = 82,
                    communicationScore = 90,
                    futureGoalsScore = 86,
                    whyRecommendedPoints = listOf(
                        "Same preferred city: Both prefer living in Kolkata / Eastern India",
                        "Compatible age range: Ideal 1-year age difference",
                        "Similar education & career goals: High-impact corporate leadership",
                        "Matching family expectations: Nuclear setup with close parental care",
                        "Relocation flexibility: Both open to occasional overseas assignments"
                    ),
                    commonGround = listOf(
                        "Both prefer living in the same city (Kolkata)",
                        "Similar family involvement expectations",
                        "Shared passion for arts, travel, and cultural heritage",
                        "Identical financial planning & retirement outlook"
                    ),
                    topicsToDiscuss = listOf(
                        "Long-term career relocation plans",
                        "Children timeline (1-2 years post wedding)",
                        "Joint vs separate living arrangement preferences"
                    )
                ),
                familyBackground = "Father is Retired Joint Secretary (Govt of West Bengal), Mother is School Principal.",
                livingArrangement = "Comfortable with independent residence near parents",
                careerExpectation = "Passionate about career growth; mutual support essential",
                childrenTimeline = "Wants 1-2 children after 2 years of marriage",
                status = MatchStatus.ACCEPTED
            ),
            MatchProfile(
                id = "p2",
                name = "Sneha Banerjee",
                age = 28,
                city = "Bengaluru",
                state = "Karnataka",
                profession = "Lead Product Designer",
                education = "M.Des, NID Ahmedabad",
                company = "Atlassian",
                height = "5' 4\"",
                religion = "Hindu",
                community = "Kulin Kayastha",
                motherTongue = "Bengali",
                bio = "Design thinker with an eye for detail. Love weekend painting, badminton, and cooking fusion food. Looking for an intelligent, warm partner who respects individuality.",
                photoAccentColor = 0xFF7C3AED,
                trustScore = 91,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = true,
                isHumanRecommended = false,
                isMutualMatch = true,
                category = MatchCategory.BEST_PREFERENCE,
                compatibility = CompatibilityDetails(
                    overallScore = 94,
                    lifestyleScore = 95,
                    familyExpectationsScore = 90,
                    locationScore = 88,
                    careerScore = 92,
                    communicationScore = 93,
                    futureGoalsScore = 91,
                    whyRecommendedPoints = listOf(
                        "Creative & tech symbiosis: Software architect meets product designer",
                        "Active lifestyle alignment: Health, fitness, and weekend sports",
                        "Shared modern outlook with deep cultural respect",
                        "Flexible location: Willing to relocate back to Kolkata in 2 years",
                        "High trust rating: 91/100 verified credentials"
                    ),
                    commonGround = listOf(
                        "Work in tech/design ecosystem",
                        "Love outdoor activities and creative hobbies",
                        "Mutual value on emotional transparency"
                    ),
                    topicsToDiscuss = listOf(
                        "Timeline for returning to Kolkata or staying in Bengaluru",
                        "Work-from-home flexibility"
                    )
                ),
                familyBackground = "Father is Professor of Economics (Jadavpur University), Mother is Home Maker.",
                livingArrangement = "Open to living in Bengaluru or Kolkata",
                careerExpectation = "Committed to creative career leadership",
                childrenTimeline = "Flexible after 2-3 years",
                status = MatchStatus.INTEREST_SENT
            ),
            MatchProfile(
                id = "p3",
                name = "Dr. Ananya Roy",
                age = 27,
                city = "Mumbai",
                state = "Maharashtra",
                profession = "MD Pediatrics Specialist",
                education = "MBBS, MD - KEM Hospital",
                company = "Lilavati Hospital Mumbai",
                height = "5' 6\"",
                religion = "Hindu",
                community = "Brahmin",
                motherTongue = "Bengali",
                bio = "Pediatrician with a big heart for children and community healthcare. Enjoy playing the sitar and reading memoirs. Seeking a companion with emotional depth.",
                photoAccentColor = 0xFF0D9488,
                trustScore = 96,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = false,
                isHumanRecommended = true,
                isMutualMatch = false,
                category = MatchCategory.MUTUAL,
                compatibility = CompatibilityDetails(
                    overallScore = 88,
                    lifestyleScore = 85,
                    familyExpectationsScore = 92,
                    locationScore = 78,
                    careerScore = 89,
                    communicationScore = 91,
                    futureGoalsScore = 87,
                    whyRecommendedPoints = listOf(
                        "Both prioritize meaningful social contribution",
                        "Respectful of demanding professional schedules",
                        "High emotional intelligence scores",
                        "Family-approved by verified parents' circle",
                        "100% verified medical council credentials"
                    ),
                    commonGround = listOf(
                        "High family involvement",
                        "Classical music appreciation",
                        "Community service values"
                    ),
                    topicsToDiscuss = listOf(
                        "Hospital night call schedules & work-life balance",
                        "Inter-city living arrangements"
                    )
                ),
                familyBackground = "Doctors family: Both parents are Senior Surgeons.",
                livingArrangement = "Hospital quarters or nearby apartment",
                careerExpectation = "Lifelong dedication to medical practice",
                childrenTimeline = "Planning for family within 2 years",
                status = MatchStatus.AVAILABLE
            ),
            MatchProfile(
                id = "p4",
                name = "Rhea Sengupta",
                age = 29,
                city = "Delhi NCR",
                state = "New Delhi",
                profession = "Assistant Director (Civil Services)",
                education = "M.A. Political Science, JNU",
                company = "Ministry of External Affairs, GoI",
                height = "5' 5\"",
                religion = "Hindu",
                community = "Kayastha",
                motherTongue = "Bengali",
                bio = "Public servant dedicated to diplomacy and policy. Keen interest in foreign affairs, trekking, and heritage conservation. Looking for an egalitarian partner.",
                photoAccentColor = 0xFF4338CA,
                trustScore = 98,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = true,
                isHumanRecommended = true,
                isMutualMatch = false,
                category = MatchCategory.DAILY_5,
                compatibility = CompatibilityDetails(
                    overallScore = 89,
                    lifestyleScore = 86,
                    familyExpectationsScore = 89,
                    locationScore = 80,
                    careerScore = 94,
                    communicationScore = 88,
                    futureGoalsScore = 90,
                    whyRecommendedPoints = listOf(
                        "Intellectual compatibility and public policy interest",
                        "Stable government career with diplomatic postings",
                        "Equal partnership mindset",
                        "Values deep family traditions during festivals",
                        "Highest identity trust rating (98/100)"
                    ),
                    commonGround = listOf(
                        "Deep passion for reading and history",
                        "High civic responsibility"
                    ),
                    topicsToDiscuss = listOf(
                        "Potential overseas diplomatic postings every 3-4 years",
                        "Partner's remote work feasibility"
                    )
                ),
                familyBackground = "Father is Retired Army Brigadier, Mother is NGO Trustee.",
                livingArrangement = "Govt accommodations or family residence",
                careerExpectation = "Diplomatic and public policy service",
                childrenTimeline = "Open to discussion",
                status = MatchStatus.AVAILABLE
            ),
            MatchProfile(
                id = "p5",
                name = "Pooja Das",
                age = 29,
                city = "Kolkata",
                state = "West Bengal",
                profession = "Conservation Architect",
                education = "B.Arch, Jadavpur University",
                company = "Heritage Restoration Council",
                height = "5' 3\"",
                religion = "Hindu",
                community = "Kulin",
                motherTongue = "Bengali",
                bio = "Restoring historical mansions and courtyards. Passionate about pottery, eco-friendly living, and soulful addas over chai. Looking for an appreciative, grounded life partner.",
                photoAccentColor = 0xFFEA580C,
                trustScore = 89,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = true,
                isHumanRecommended = false,
                isMutualMatch = true,
                category = MatchCategory.RECENTLY_ACTIVE,
                compatibility = CompatibilityDetails(
                    overallScore = 90,
                    lifestyleScore = 92,
                    familyExpectationsScore = 94,
                    locationScore = 98,
                    careerScore = 84,
                    communicationScore = 89,
                    futureGoalsScore = 88,
                    whyRecommendedPoints = listOf(
                        "Exact location match: North/South Kolkata resident",
                        "Deep appreciation for Kolkata culture and addas",
                        "Eco-conscious and grounded lifestyle",
                        "Both families share traditional values",
                        "Recently active within 24 hours"
                    ),
                    commonGround = listOf(
                        "Same hometown lifestyle",
                        "Family-first outlook",
                        "Eco-friendly home living"
                    ),
                    topicsToDiscuss = listOf(
                        "Home design and living preferences",
                        "Parental care arrangements"
                    )
                ),
                familyBackground = "Traditional business family in North Kolkata.",
                livingArrangement = "Family ancestral home or independent flat nearby",
                careerExpectation = "Continues architectural consultancy",
                childrenTimeline = "Ready for family within 1-2 years",
                status = MatchStatus.AVAILABLE
            ),
            MatchProfile(
                id = "p6",
                name = "Meera Iyer",
                age = 28,
                city = "Hyderabad",
                state = "Telangana",
                profession = "Staff AI Research Engineer",
                education = "M.S. Computer Science, IIIT Hyderabad",
                company = "Google DeepMind Research",
                height = "5' 6\"",
                religion = "Hindu",
                community = "Brahmin",
                motherTongue = "Tamil / Bengali bilingual",
                bio = "AI researcher with love for Carnatic violin, marathon running, and indie films. Looking for a partner with curiosity, humor, and grounded values.",
                photoAccentColor = 0xFF059669,
                trustScore = 93,
                isIdentityVerified = true,
                isPhotoVerified = true,
                isFamilyApproved = false,
                isHumanRecommended = true,
                isMutualMatch = true,
                category = MatchCategory.DAILY_5,
                compatibility = CompatibilityDetails(
                    overallScore = 93,
                    lifestyleScore = 94,
                    familyExpectationsScore = 87,
                    locationScore = 89,
                    careerScore = 96,
                    communicationScore = 95,
                    futureGoalsScore = 92,
                    whyRecommendedPoints = listOf(
                        "Shared research & engineering passion",
                        "Marathon running and discipline alignment",
                        "Multilingual and culturally versatile",
                        "Mutual interest in AI ethics and lifelong learning",
                        "High communication compatibility"
                    ),
                    commonGround = listOf(
                        "Technology leadership careers",
                        "Fitness & endurance running",
                        "Appreciation of Indian classical arts"
                    ),
                    topicsToDiscuss = listOf(
                        "Work from home vs office travel",
                        "Long-term settlement city"
                    )
                ),
                familyBackground = "Father is ISRO Scientist (retd), Mother is Carnatic Vocalist.",
                livingArrangement = "Independent city apartment",
                careerExpectation = "Continued AI research and mentorship",
                childrenTimeline = "Planning after 2-3 years",
                status = MatchStatus.SHORTLISTED
            )
        )

        // Seeded conversation with Priya Mukherjee
        _chats.value = mapOf(
            "p1" to listOf(
                ChatMessage(
                    id = "m1",
                    matchId = "p1",
                    senderName = "Prem Setu Safe Connect",
                    text = "🔒 Safe Connect Active: Phone numbers and personal social handles remain protected until both members mutually approve contact sharing.",
                    timestamp = "Sep 20, 10:00 AM",
                    isMine = false,
                    isSafetyAlert = false
                ),
                ChatMessage(
                    id = "m2",
                    matchId = "p1",
                    senderName = "Aarav Sen",
                    text = "Hi Priya! I went through your compatibility profile and was really impressed by your values regarding family and career continuity. Would love to get to know more about you.",
                    timestamp = "Sep 20, 10:15 AM",
                    isMine = true
                ),
                ChatMessage(
                    id = "m3",
                    matchId = "p1",
                    senderName = "Priya Mukherjee",
                    text = "Hello Aarav! Thank you so much for the note. I noticed we both cherish heritage Kolkata spots and have similar expectations on family care. How is your week going?",
                    timestamp = "Sep 20, 11:30 AM",
                    isMine = false
                ),
                ChatMessage(
                    id = "m4",
                    matchId = "p1",
                    senderName = "Aarav Sen",
                    text = "It has been great! By the way, the Prem Setu compatibility prompt suggested we discuss long-term city settlement. I'm very keen on continuing in Kolkata.",
                    timestamp = "Sep 20, 11:45 AM",
                    isMine = true
                ),
                ChatMessage(
                    id = "m5",
                    matchId = "p1",
                    senderName = "Priya Mukherjee",
                    text = "That is wonderful to hear! Kolkata is definitely where my heart and work are anchored. Our family is also very happy with the Prem Setu verification details.",
                    timestamp = "Sep 20, 12:05 PM",
                    isMine = false
                )
            )
        )

        // Seeded First Meeting Plan
        _meetings.value = listOf(
            MeetingPlan(
                id = "meet1",
                matchId = "p1",
                matchName = "Priya Mukherjee",
                partnerCity = "Kolkata",
                date = "Sunday, 28 Sep 2026",
                time = "4:30 PM IST",
                location = "Flurys Heritage Tearoom, Park Street, Kolkata",
                meetingType = "Public Place",
                participants = "Both of us (Private Meeting)",
                trustedContact = "Sunil Sen (Father - +91 94330 11223)",
                isSharedWithContact = true,
                isCheckedIn = false,
                isCheckedOut = false,
                feedbackGiven = false
            )
        )
    }

    fun updateBio(newBio: String) {
        _userBio.value = newBio
    }

    fun updateUserName(name: String) {
        _userProfileName.value = name
    }

    fun updateFamilyMode(mode: FamilyParticipationMode) {
        _familyMode.value = mode
    }

    fun updateExplorationMode(mode: MatchExplorationMode) {
        _explorationMode.value = mode
    }

    fun updateVisibility(visibility: ProfileVisibility) {
        _visibilityMode.value = visibility
    }

    fun toggleWomenExtraPrivacy(enabled: Boolean) {
        _womenExtraPrivacy.value = enabled
    }

    fun togglePauseMatchmaking(days: Int = 30) {
        _isMatchmakingPaused.value = !_isMatchmakingPaused.value
    }

    fun sendInterest(matchId: String, customNote: String = "") {
        _matches.update { list ->
            list.map {
                if (it.id == matchId) {
                    it.copy(status = MatchStatus.INTEREST_SENT, customIntroNote = customNote)
                } else it
            }
        }
    }

    fun toggleShortlist(matchId: String) {
        _matches.update { list ->
            list.map {
                if (it.id == matchId) {
                    val newStatus = if (it.status == MatchStatus.SHORTLISTED) MatchStatus.AVAILABLE else MatchStatus.SHORTLISTED
                    it.copy(status = newStatus)
                } else it
            }
        }
    }

    fun respondToInterest(matchId: String, accept: Boolean) {
        _matches.update { list ->
            list.map {
                if (it.id == matchId) {
                    it.copy(status = if (accept) MatchStatus.ACCEPTED else MatchStatus.DECLINED)
                } else it
            }
        }
    }

    fun updateMatchStatus(matchId: String, newStatus: MatchStatus) {
        _matches.update { list ->
            list.map {
                if (it.id == matchId) {
                    it.copy(status = newStatus)
                } else it
            }
        }
    }

    fun sendMessage(matchId: String, text: String) {
        val targetMatch = _matches.value.firstOrNull { it.id == matchId }
        if (targetMatch?.status == MatchStatus.BLOCKED) {
            return
        }

        val containsFinancialRisk = listOf("money", "transfer", "wire", "crypto", "paytm", "gpay", "rupees", "bank account", "urgent funds", "loan")
            .any { text.lowercase().contains(it) }

        val currentList = _chats.value[matchId] ?: emptyList()
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            matchId = matchId,
            senderName = _userProfileName.value,
            text = text,
            timestamp = "Just now",
            isMine = true,
            isSafetyAlert = false
        )

        val updatedList = currentList.toMutableList().apply { add(userMsg) }

        // If financial language detected, inject real-time Prem Setu Safety Reminder
        if (containsFinancialRisk) {
            updatedList.add(
                ChatMessage(
                    id = "alert_${System.currentTimeMillis()}",
                    matchId = matchId,
                    senderName = "Prem Setu Safety Guard",
                    text = "⚠️ Safety Reminder: Never send money or financial credentials to someone you have met through Prem Setu. Report suspicious requests to safety@premsetu.com immediately.",
                    timestamp = "System Alert",
                    isMine = false,
                    isSafetyAlert = true,
                    alertWarning = "Financial Keyword Detected"
                )
            )
        }

        _chats.update {
            it.toMutableMap().apply { put(matchId, updatedList) }
        }
    }

    fun requestContactShare(matchId: String) {
        _sharedContacts.update { it + matchId }
        val currentList = _chats.value[matchId] ?: emptyList()
        val shareNotice = ChatMessage(
            id = "share_${System.currentTimeMillis()}",
            matchId = matchId,
            senderName = "Prem Setu Consent Desk",
            text = "🤝 Contact Sharing Request Approved! Phone numbers and personal email addresses are now unlocked for both members with mutual consent.",
            timestamp = "System Notice",
            isMine = false,
            isSafetyAlert = false
        )
        _chats.update {
            it.toMutableMap().apply { put(matchId, currentList + shareNotice) }
        }
    }

    fun addFamilyMember(name: String, relation: String, phone: String, email: String) {
        val newMember = FamilyMember(
            id = "fam_${System.currentTimeMillis()}",
            name = name,
            relation = relation,
            phone = phone,
            email = email,
            canReviewMatches = true,
            canRecommend = true,
            notesCount = 0
        )
        _familyMembers.update { it + newMember }
    }

    fun scheduleMeeting(meeting: MeetingPlan) {
        _meetings.update { it + meeting }
    }

    fun checkInMeeting(meetingId: String) {
        _meetings.update { list ->
            list.map { if (it.id == meetingId) it.copy(isCheckedIn = true) else it }
        }
    }

    fun checkOutMeeting(meetingId: String) {
        _meetings.update { list ->
            list.map { if (it.id == meetingId) it.copy(isCheckedOut = true) else it }
        }
    }

    fun submitMeetingFeedback(meetingId: String, outcome: String) {
        _meetings.update { list ->
            list.map { if (it.id == meetingId) it.copy(feedbackGiven = true, feedbackOutcome = outcome) else it }
        }
    }

    fun updateMembership(tier: String) {
        _membershipTier.value = tier
    }

    private val userProfileRepo = UserProfileRepository()

    /**
     * Calculates compatibility percentage between current user profile attributes and another profile.
     */
    fun calculateCompatibilityScore(user: UserProfile, target: UserProfile): Int {
        return userProfileRepo.calculateCompatibilityScore(user, target)
    }

    /**
     * Calculates compatibility breakdown between current user profile attributes and another profile.
     */
    fun calculateCompatibilityBreakdown(user: UserProfile, target: UserProfile): CompatibilityBreakdown {
        return userProfileRepo.calculateCompatibilityBreakdown(user, target)
    }
}
