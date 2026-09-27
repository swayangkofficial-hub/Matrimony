package com.example.data.repository

import com.example.data.model.UserProfile
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserProfileRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            null
        }

    private val usersCollection: CollectionReference?
        get() = firestore?.collection(COLLECTION_USERS)

    companion object {
        const val COLLECTION_USERS = "users"
    }

    /**
     * Updates the privacy settings in the user's Firestore document.
     */
    suspend fun updatePrivacySettings(
        userId: String,
        showPhoneInSearch: Boolean,
        showLocationInSearch: Boolean,
        showEmailInSearch: Boolean = false,
        allowSearchIndexing: Boolean = true,
        womenExtraPrivacy: Boolean = false,
        visibilityMode: String = "PUBLIC"
    ): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "showPhoneInSearch" to showPhoneInSearch,
                "showLocationInSearch" to showLocationInSearch,
                "showEmailInSearch" to showEmailInSearch,
                "allowSearchIndexing" to allowSearchIndexing,
                "womenExtraPrivacy" to womenExtraPrivacy,
                "visibilityMode" to visibilityMode,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).set(updates, SetOptions.merge()).awaitTask()
        }
    }

    /**
     * Updates the block list in the user's Firestore document.
     */
    suspend fun blockUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            col.document(currentUserId).set(
                mapOf(
                    "blockedUsers" to com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId),
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).awaitTask()
        }
    }

    /**
     * Removes a user from the block list in the user's Firestore document.
     */
    suspend fun unblockUser(currentUserId: String, targetUserId: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            col.document(currentUserId).update(
                "blockedUsers",
                com.google.firebase.firestore.FieldValue.arrayRemove(targetUserId)
            ).awaitTask()
        }
    }

    /**
     * Saves or updates a complete user profile in Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val docRef = if (profile.id.isNotBlank()) {
                col.document(profile.id)
            } else {
                col.document()
            }
            val profileToSave = profile.copy(
                id = docRef.id,
                updatedAt = System.currentTimeMillis()
            )
            docRef.set(profileToSave, SetOptions.merge()).awaitTask()
        }
    }

    /**
     * Reads a user profile from Firestore once by ID.
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return runCatching {
            val col = usersCollection ?: return@runCatching null
            val snapshot = col.document(userId).get().awaitTask()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                null
            }
        }
    }

    /**
     * Observes real-time changes to a user profile in Firestore.
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfile?> = callbackFlow {
        val col = usersCollection
        if (col == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = col.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Updates the bio text of a user profile in Firestore.
     */
    suspend fun updateBio(userId: String, bio: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "bio" to bio,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Updates profile photo gallery and primary avatar in Firestore.
     */
    suspend fun updateProfilePhotos(userId: String, photoUrls: List<String>, avatarUrl: String = ""): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "photoUrls" to photoUrls,
                "avatarUrl" to (avatarUrl.ifBlank { photoUrls.firstOrNull() ?: "" }),
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Updates the trust score and profile health score in Firestore.
     */
    suspend fun updateTrustScores(userId: String, trustScore: Int, healthScore: Int): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "trustScore" to trustScore,
                "profileHealthScore" to healthScore,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Updates family participation mode in Firestore.
     */
    suspend fun updateFamilyMode(userId: String, mode: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "familyMode" to mode,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Updates matchmaking exploration mode in Firestore.
     */
    suspend fun updateExplorationMode(userId: String, mode: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "explorationMode" to mode,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Updates profile visibility in Firestore.
     */
    suspend fun updateVisibility(userId: String, visibility: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "visibilityMode" to visibility,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Toggles pause status for matchmaking in Firestore.
     */
    suspend fun togglePauseMatchmaking(userId: String, isPaused: Boolean): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            val updates = mapOf(
                "isMatchmakingPaused" to isPaused,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(userId).update(updates).awaitTask()
        }
    }

    /**
     * Deletes a user profile document from Firestore.
     */
    suspend fun deleteUserProfile(userId: String): Result<Unit> {
        return runCatching {
            val col = usersCollection ?: return@runCatching
            col.document(userId).delete().awaitTask()
        }
    }

    /**
     * Queries profiles from Firestore matching criteria like age, location, and advanced filters.
     * Gracefully falls back to rich sample profiles if Firestore is offline or empty.
     */
    suspend fun queryProfiles(
        minAge: Int = 21,
        maxAge: Int = 45,
        location: String = "All",
        gender: String = "All",
        verifiedOnly: Boolean = false,
        familyValues: String = "All",
        educationLevel: String = "All",
        dietaryPreference: String = "All",
        blockedUserIds: List<String> = emptyList()
    ): Result<List<UserProfile>> {
        return runCatching {
            val col = usersCollection
            if (col == null) {
                return@runCatching getFallbackProfiles(minAge, maxAge, location, gender, verifiedOnly, familyValues, educationLevel, dietaryPreference, blockedUserIds)
            }

            // Attempt Firestore query by age
            val snapshot = try {
                col.whereGreaterThanOrEqualTo("age", minAge)
                    .whereLessThanOrEqualTo("age", maxAge)
                    .limit(50)
                    .get()
                    .awaitTask()
            } catch (e: Throwable) {
                null
            }

            val firestoreProfiles = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)
            } ?: emptyList()

            val combined = if (firestoreProfiles.isNotEmpty()) {
                firestoreProfiles
            } else {
                getFallbackProfiles(minAge, maxAge, location, gender, verifiedOnly, familyValues, educationLevel, dietaryPreference, blockedUserIds)
            }

            filterProfiles(combined, minAge, maxAge, location, gender, verifiedOnly, familyValues, educationLevel, dietaryPreference, blockedUserIds)
        }.recover {
            getFallbackProfiles(minAge, maxAge, location, gender, verifiedOnly, familyValues, educationLevel, dietaryPreference, blockedUserIds)
        }
    }

    /**
     * Seeds sample profiles into Firestore so real documents exist in the cloud collection.
     */
    suspend fun seedSampleProfilesToFirestore(): Result<Int> {
        return runCatching {
            val col = usersCollection ?: return@runCatching 0
            val samples = getSampleSeedProfiles()
            var count = 0
            for (p in samples) {
                col.document(p.id).set(p, SetOptions.merge()).awaitTask()
                count++
            }
            count
        }
    }

    private fun getFallbackProfiles(
        minAge: Int,
        maxAge: Int,
        location: String,
        gender: String,
        verifiedOnly: Boolean,
        familyValues: String = "All",
        educationLevel: String = "All",
        dietaryPreference: String = "All",
        blockedUserIds: List<String> = emptyList()
    ): List<UserProfile> {
        return filterProfiles(getSampleSeedProfiles(), minAge, maxAge, location, gender, verifiedOnly, familyValues, educationLevel, dietaryPreference, blockedUserIds)
    }

    /**
     * Helper to filter candidate profiles by basic and advanced criteria, excluding blocked profiles.
     */
    fun filterProfiles(
        profiles: List<UserProfile>,
        minAge: Int,
        maxAge: Int,
        location: String,
        gender: String,
        verifiedOnly: Boolean,
        familyValues: String = "All",
        educationLevel: String = "All",
        dietaryPreference: String = "All",
        blockedUserIds: List<String> = emptyList()
    ): List<UserProfile> {
        return profiles.filter { profile ->
            if (blockedUserIds.isNotEmpty() && blockedUserIds.contains(profile.id)) {
                return@filter false
            }

            val matchesLocation = location == "All" || location.isBlank() ||
                    profile.city.contains(location, ignoreCase = true) ||
                    profile.state.contains(location, ignoreCase = true)

            val matchesGender = gender == "All" || gender.isBlank() ||
                    profile.gender.equals(gender, ignoreCase = true)

            val matchesAge = profile.age in minAge..maxAge

            val matchesVerified = !verifiedOnly || profile.isIdentityVerified

            val matchesFamilyValues = familyValues == "All" || familyValues.isBlank() ||
                    profile.familyValues.equals(familyValues, ignoreCase = true) ||
                    (familyValues == "Traditional" && (profile.familyValues.contains("Traditional", ignoreCase = true) || profile.familyMode.contains("FAMILY_DRIVEN", ignoreCase = true))) ||
                    (familyValues == "Moderate" && (profile.familyValues.contains("Moderate", ignoreCase = true) || profile.explorationMode.contains("BALANCED", ignoreCase = true))) ||
                    (familyValues == "Liberal" && (profile.familyValues.contains("Liberal", ignoreCase = true) || profile.familyMode.contains("SELF_MANAGED", ignoreCase = true)))

            val matchesEducationLevel = educationLevel == "All" || educationLevel.isBlank() ||
                    profile.educationLevel.equals(educationLevel, ignoreCase = true) ||
                    profile.educationLevel.contains(educationLevel, ignoreCase = true) ||
                    when (educationLevel) {
                        "Doctorate" -> profile.education.contains("PhD", ignoreCase = true) ||
                                profile.education.contains("Doctorate", ignoreCase = true) ||
                                profile.educationLevel.contains("Doctorate", ignoreCase = true)
                        "Master's / Postgraduate" -> profile.education.contains("MBA", ignoreCase = true) ||
                                profile.education.contains("M.Tech", ignoreCase = true) ||
                                profile.education.contains("MS", ignoreCase = true) ||
                                profile.education.contains("MD", ignoreCase = true) ||
                                profile.education.contains("Master", ignoreCase = true) ||
                                profile.educationLevel.contains("Master", ignoreCase = true)
                        "Bachelor's / Graduate" -> profile.education.contains("B.Tech", ignoreCase = true) ||
                                profile.education.contains("BE", ignoreCase = true) ||
                                profile.education.contains("B.Com", ignoreCase = true) ||
                                profile.education.contains("BSc", ignoreCase = true) ||
                                profile.education.contains("Bachelor", ignoreCase = true) ||
                                profile.educationLevel.contains("Bachelor", ignoreCase = true)
                        "Professional Degree" -> profile.education.contains("CA", ignoreCase = true) ||
                                profile.education.contains("MD", ignoreCase = true) ||
                                profile.education.contains("MBBS", ignoreCase = true) ||
                                profile.education.contains("LL.M", ignoreCase = true) ||
                                profile.education.contains("IIM", ignoreCase = true) ||
                                profile.educationLevel.contains("Professional", ignoreCase = true)
                        else -> profile.education.contains(educationLevel, ignoreCase = true)
                    }

            val matchesDietary = dietaryPreference == "All" || dietaryPreference.isBlank() ||
                    profile.dietaryPreference.equals(dietaryPreference, ignoreCase = true) ||
                    profile.dietaryPreference.contains(dietaryPreference, ignoreCase = true)

            matchesLocation && matchesGender && matchesAge && matchesVerified &&
                    matchesFamilyValues && matchesEducationLevel && matchesDietary
        }
    }

    fun getSampleSeedProfiles(): List<UserProfile> = listOf(
        UserProfile(
            id = "user_p1",
            name = "Priya Mukherjee",
            age = 28,
            gender = "Female",
            city = "Kolkata",
            state = "West Bengal",
            profession = "VP - Portfolio Strategy",
            education = "MBA (Finance), IIM Calcutta",
            company = "HSBC Global Private Banking",
            height = "5 ft 5 in",
            religion = "Hindu",
            community = "Bengali Brahmin",
            motherTongue = "Bengali",
            bio = "Compassionate, driven, and family-oriented. Love classical literature, heritage cafe culture, and value clear communication and empathy above all.",
            profileCreatedFor = "Myself",
            trustScore = 94,
            profileHealthScore = 96,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98300 24891",
            email = "priya.m@example.com",
            familyBackground = "Father is Retired Joint Secretary (Govt of West Bengal), Mother is School Principal.",
            livingArrangement = "Comfortable with independent residence near parents",
            careerExpectation = "Passionate about career growth; mutual support essential",
            childrenTimeline = "Wants children in 2-3 years post marriage.",
            familyValues = "Moderate",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Eggetarian"
        ),
        UserProfile(
            id = "user_p2",
            name = "Sneha Banerjee",
            age = 27,
            gender = "Female",
            city = "Kolkata",
            state = "West Bengal",
            profession = "Principal AI Research Engineer",
            education = "M.Tech in AI, IISc Bengaluru",
            company = "Google DeepMind Research",
            height = "5 ft 4 in",
            religion = "Hindu",
            community = "Bengali Kayastha",
            motherTongue = "Bengali",
            bio = "Passionate technologist, avid reader, and classical sitar player. Striving for intellectual depth and shared curiosity in life and relationship.",
            profileCreatedFor = "Parents",
            trustScore = 96,
            profileHealthScore = 98,
            familyMode = "FAMILY_DRIVEN",
            explorationMode = "CAUTIOUS",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98301 35792",
            email = "sneha.b@example.com",
            familyBackground = "Both parents are esteemed retired professors at Jadavpur University.",
            livingArrangement = "Joint family welcoming or independent living",
            careerExpectation = "Continued research focus with international conferences",
            childrenTimeline = "Open to discussing after 2 years",
            familyValues = "Traditional",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Vegetarian"
        ),
        UserProfile(
            id = "user_p3",
            name = "Dr. Ananya Roy",
            age = 29,
            gender = "Female",
            city = "Bengaluru",
            state = "Karnataka",
            profession = "Consultant Pediatrician",
            education = "MD Pediatrics, CMC Vellore",
            company = "Manipal Hospitals Bengaluru",
            height = "5 ft 6 in",
            religion = "Hindu",
            community = "Bengali Brahmin",
            motherTongue = "Bengali",
            bio = "Dedicated doctor with a heart for community health. Outside the clinic, I enjoy baking, yoga, and exploring scenic trekking trails across the Western Ghats.",
            profileCreatedFor = "Myself",
            trustScore = 92,
            profileHealthScore = 94,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98450 12345",
            email = "dr.ananya.roy@example.com",
            familyBackground = "Father is a retired Civil Engineer, Mother is a homemaker.",
            livingArrangement = "Flexible based on hospital proximity and mutual alignment",
            careerExpectation = "Active medical practice with hospital commitments",
            childrenTimeline = "Planning for children in 2-3 years",
            familyValues = "Liberal",
            educationLevel = "Professional Degree",
            dietaryPreference = "Non-Vegetarian"
        ),
        UserProfile(
            id = "user_p4",
            name = "Rohan Mehra",
            age = 30,
            gender = "Male",
            city = "Delhi NCR",
            state = "Delhi",
            profession = "Corporate Legal Counsel",
            education = "LL.M, National Law School (NLSIU)",
            company = "Khaitan & Co Partners",
            height = "5 ft 10 in",
            religion = "Hindu",
            community = "Khatri",
            motherTongue = "Hindi",
            bio = "Corporate lawyer with a passion for constitutional ethics, squash, and heritage photography. Seeking a grounded partner who values honesty and family warmth.",
            profileCreatedFor = "Myself",
            trustScore = 91,
            profileHealthScore = 90,
            familyMode = "SELF_MANAGED",
            explorationMode = "ACTIVE",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98110 54321",
            email = "rohan.mehra@example.com",
            familyBackground = "Father is Senior Advocate at Delhi High Court, mother runs an education NGO.",
            livingArrangement = "Independent residence in South Delhi",
            careerExpectation = "Partnership track in law firm",
            childrenTimeline = "Looking forward to family life in 2-3 years",
            familyValues = "Moderate",
            educationLevel = "Professional Degree",
            dietaryPreference = "Non-Vegetarian"
        ),
        UserProfile(
            id = "user_p5",
            name = "Tanvi Kulkarni",
            age = 26,
            gender = "Female",
            city = "Pune",
            state = "Maharashtra",
            profession = "Senior UX Architect",
            education = "Master of Design, IDC IIT Bombay",
            company = "Adobe Systems India",
            height = "5 ft 4 in",
            religion = "Hindu",
            community = "Maratha",
            motherTongue = "Marathi",
            bio = "Product designer by day, potter and book club organizer on weekends. Believe that humor and mutual respect are the cornerstones of a joyful marriage.",
            profileCreatedFor = "Myself",
            trustScore = 93,
            profileHealthScore = 95,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98220 98765",
            email = "tanvi.k@example.com",
            familyBackground = "Father is a Mechanical Engineering consultant, mother is a banker.",
            livingArrangement = "Comfortable with urban apartment living in Pune or Mumbai",
            careerExpectation = "Hybrid design leadership roles",
            childrenTimeline = "2-3 years after marriage",
            familyValues = "Liberal",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Vegan"
        ),
        UserProfile(
            id = "user_p6",
            name = "Aditya Verma",
            age = 31,
            gender = "Male",
            city = "Bengaluru",
            state = "Karnataka",
            profession = "Principal Cloud Architect",
            education = "B.Tech Computer Science, BITS Pilani",
            company = "Amazon Web Services",
            height = "6 ft 0 in",
            religion = "Hindu",
            community = "Kayastha",
            motherTongue = "Hindi",
            bio = "Tech builder, marathon runner, and enthusiast of Indie rock and filter coffee. Value emotional intelligence, shared adventures, and close-knit family values.",
            profileCreatedFor = "Myself",
            trustScore = 95,
            profileHealthScore = 94,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98860 11223",
            email = "aditya.v@example.com",
            familyBackground = "Parents reside in Lucknow; well-respected academic family.",
            livingArrangement = "Spacious home in Bengaluru, open to family visits anytime",
            careerExpectation = "Tech executive track",
            childrenTimeline = "Looking forward to starting a family in 2 years",
            familyValues = "Moderate",
            educationLevel = "Bachelor's / Graduate",
            dietaryPreference = "Pure Vegetarian"
        ),
        UserProfile(
            id = "user_p7",
            name = "Pooja Sharma",
            age = 27,
            gender = "Female",
            city = "Mumbai",
            state = "Maharashtra",
            profession = "Chartered Accountant & FinTech Lead",
            education = "FCA, Institute of Chartered Accountants of India",
            company = "Morgan Stanley India",
            height = "5 ft 5 in",
            religion = "Hindu",
            community = "Brahmin",
            motherTongue = "Hindi",
            bio = "Financial analyst with a passion for Hindustani music, theatre, and weekend coastal road trips. Seeking someone kind, communicative, and ambitious.",
            profileCreatedFor = "Parents",
            trustScore = 94,
            profileHealthScore = 93,
            familyMode = "FAMILY_DRIVEN",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98200 44556",
            email = "pooja.sharma@example.com",
            familyBackground = "Father is Senior Accounts Officer, mother is High School Principal.",
            livingArrangement = "Mumbai suburbs or South Mumbai",
            careerExpectation = "Financial leadership in wealth management",
            childrenTimeline = "Planning for children in 2-3 years",
            familyValues = "Traditional",
            educationLevel = "Professional Degree",
            dietaryPreference = "Jain"
        ),
        UserProfile(
            id = "user_p8",
            name = "Vikram Joshi",
            age = 32,
            gender = "Male",
            city = "Hyderabad",
            state = "Telangana",
            profession = "Director of Software Engineering",
            education = "M.S. in Computer Science, Georgia Tech",
            company = "Microsoft IDC Hyderabad",
            height = "5 ft 11 in",
            religion = "Hindu",
            community = "Brahmin",
            motherTongue = "Telugu",
            bio = "Returned from US after 6 years to be close to roots in India. Enjoy badminton, classical carnatic fusion, and cooking for family. Looking for an equal life partner.",
            profileCreatedFor = "Myself",
            trustScore = 97,
            profileHealthScore = 96,
            familyMode = "FAMILY_ASSISTED",
            explorationMode = "BALANCED",
            visibilityMode = "PUBLIC",
            isIdentityVerified = true,
            isPhotoVerified = true,
            isFamilyApproved = true,
            phone = "+91 98490 66778",
            email = "vikram.joshi@example.com",
            familyBackground = "Traditional yet progressive family in Hyderabad with deep cultural roots.",
            livingArrangement = "Villa in Hyderabad with space for extended family gatherings",
            careerExpectation = "Leading engineering teams in Hyderabad",
            childrenTimeline = "Eager to build a warm family in 1-2 years",
            familyValues = "Traditional",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Pure Vegetarian"
        )
    )

    /**
     * Calculates a holistic Matrimonial Compatibility Score (0 - 100%) between two UserProfiles.
     * Evaluates:
     * 1. Age Difference (0-20 pts)
     * 2. Geographic / Location Alignment (0-20 pts)
     * 3. Religion, Community & Mother Tongue (0-25 pts)
     * 4. Family Mode, Living Arrangements & Family Values (0-20 pts)
     * 5. Education & Professional Synergy (0-15 pts)
     */
    fun calculateCompatibilityScore(user: UserProfile, target: UserProfile): Int {
        return calculateCompatibilityBreakdown(user, target).overallScore
    }

    /**
     * Computes the detailed breakdown of the matrimonial compatibility score.
     */
    fun calculateCompatibilityBreakdown(user: UserProfile, target: UserProfile): CompatibilityBreakdown {
        // 1. Age Compatibility (Max 20 pts)
        val ageDiff = kotlin.math.abs(user.age - target.age)
        val ageScore = when {
            ageDiff in 0..2 -> 20
            ageDiff in 3..4 -> 17
            ageDiff in 5..6 -> 14
            ageDiff in 7..9 -> 10
            else -> 6
        }

        // 2. Location Alignment (Max 20 pts)
        val locationScore = when {
            user.city.isNotBlank() && target.city.isNotBlank() &&
                    user.city.equals(target.city, ignoreCase = true) -> 20
            user.state.isNotBlank() && target.state.isNotBlank() &&
                    user.state.equals(target.state, ignoreCase = true) -> 16
            else -> {
                val metros = setOf("kolkata", "bengaluru", "mumbai", "delhi", "delhi ncr", "pune", "hyderabad", "chennai")
                if (user.city.lowercase() in metros && target.city.lowercase() in metros) 12 else 8
            }
        }

        // 3. Cultural & Linguistic Alignment (Max 25 pts)
        var cultScore = 0
        if (user.religion.isNotBlank() && target.religion.isNotBlank() &&
            user.religion.equals(target.religion, ignoreCase = true)
        ) {
            cultScore += 10
        } else {
            cultScore += 4
        }
        if (user.community.isNotBlank() && target.community.isNotBlank() &&
            (user.community.contains(target.community, ignoreCase = true) ||
             target.community.contains(user.community, ignoreCase = true))
        ) {
            cultScore += 8
        } else {
            cultScore += 4
        }
        if (user.motherTongue.isNotBlank() && target.motherTongue.isNotBlank() &&
            user.motherTongue.equals(target.motherTongue, ignoreCase = true)
        ) {
            cultScore += 7
        } else {
            cultScore += 3
        }
        val culturalScore = cultScore.coerceIn(0, 25)

        // 4. Family Mode, Living Arrangements & Values (Max 20 pts)
        var famScore = 0
        if (user.familyMode.isNotBlank() && target.familyMode.isNotBlank() &&
            user.familyMode.equals(target.familyMode, ignoreCase = true)
        ) {
            famScore += 4
        } else {
            famScore += 2
        }

        // Family Values Alignment (Traditional, Moderate, Liberal)
        val fv1 = user.familyValues.trim()
        val fv2 = target.familyValues.trim()
        if (fv1.isNotBlank() && fv2.isNotBlank()) {
            if (fv1.equals(fv2, ignoreCase = true)) {
                famScore += 5
            } else if (fv1.equals("Moderate", ignoreCase = true) || fv2.equals("Moderate", ignoreCase = true)) {
                famScore += 4 // Moderate easily harmonizes with Traditional and Liberal
            } else {
                famScore += 2
            }
        } else {
            famScore += 3
        }

        // Dietary Preference Harmony (Pure Vegetarian, Eggetarian, Non-Vegetarian, Jain, Vegan)
        val diet1 = user.dietaryPreference.trim().lowercase()
        val diet2 = target.dietaryPreference.trim().lowercase()
        if (diet1.isNotBlank() && diet2.isNotBlank()) {
            if (diet1 == diet2 || diet1.contains(diet2) || diet2.contains(diet1)) {
                famScore += 4
            } else if ((diet1.contains("veg") && diet2.contains("eggetarian")) ||
                       (diet1.contains("eggetarian") && diet2.contains("veg")) ||
                       (diet1.contains("jain") && diet2.contains("veg")) ||
                       (diet1.contains("vegan") && diet2.contains("veg"))
            ) {
                famScore += 3
            } else {
                famScore += 1
            }
        } else {
            famScore += 2
        }

        val live1 = user.livingArrangement.lowercase()
        val live2 = target.livingArrangement.lowercase()
        if (live1.isNotBlank() && live2.isNotBlank() &&
            ((live1.contains("independent") && live2.contains("independent")) ||
             (live1.contains("joint") && live2.contains("joint")) ||
              live1.contains("flexible") || live2.contains("flexible") || live1.contains("comfort") || live2.contains("comfort"))
        ) {
            famScore += 4
        } else {
            famScore += 2
        }

        val child1 = user.childrenTimeline.lowercase()
        val child2 = target.childrenTimeline.lowercase()
        if (child1.isNotBlank() && child2.isNotBlank() &&
            ((child1.contains("1-2") && child2.contains("1-2")) ||
             (child1.contains("2-3") && child2.contains("2-3")) ||
             child1.contains("open") || child2.contains("open") ||
             (child1.contains("family") && child2.contains("family")))
        ) {
            famScore += 3
        } else {
            famScore += 1
        }
        val lifestyleFamilyScore = famScore.coerceIn(0, 20)

        // 5. Professional & Educational Synergy (Max 15 pts)
        var profScore = 0
        val higherDegrees = listOf("mba", "m.tech", "ms", "md", "ph.d", "ll.m", "fca", "b.tech")
        val edu1 = user.education.lowercase()
        val edu2 = target.education.lowercase()
        val userHigher = higherDegrees.any { edu1.contains(it) }
        val targetHigher = higherDegrees.any { edu2.contains(it) }
        if (userHigher && targetHigher) {
            profScore += 8
        } else if (edu1.isNotBlank() && edu2.isNotBlank()) {
            profScore += 5
        } else {
            profScore += 3
        }

        if (user.profession.isNotBlank() && target.profession.isNotBlank()) {
            profScore += 7
        } else {
            profScore += 4
        }
        val careerEducationScore = profScore.coerceIn(0, 15)

        val total = (ageScore + locationScore + culturalScore + lifestyleFamilyScore + careerEducationScore).coerceIn(45, 99)

        val summary = when {
            total >= 90 -> "Outstanding Cultural, Geographic & Lifestyle Synergy"
            total >= 80 -> "Strong Alignment in Family Values & Educational Background"
            total >= 70 -> "Good Matrimonial Compatibility with Shared Principles"
            else -> "Moderate Synergy; Explore Mutual Horizons"
        }

        return CompatibilityBreakdown(
            overallScore = total,
            ageScore = ageScore,
            locationScore = locationScore,
            culturalScore = culturalScore,
            lifestyleFamilyScore = lifestyleFamilyScore,
            careerEducationScore = careerEducationScore,
            compatibilitySummary = summary
        )
    }
}

/**
 * Breakdown of attributes evaluated in the compatibility score algorithm.
 */
data class CompatibilityBreakdown(
    val overallScore: Int,
    val ageScore: Int,
    val locationScore: Int,
    val culturalScore: Int,
    val lifestyleFamilyScore: Int,
    val careerEducationScore: Int,
    val compatibilitySummary: String
)

/**
 * Extension function to await Google Play Tasks as a coroutine.
 */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
