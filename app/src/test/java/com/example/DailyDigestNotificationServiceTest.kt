package com.example

import com.example.data.model.*
import com.example.data.repository.EmailRepository
import com.example.data.service.DailyDigestNotificationService
import com.example.data.service.DailyDigestSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DailyDigestNotificationServiceTest {

    private fun createSampleMatch(
        id: String,
        name: String,
        score: Int,
        status: MatchStatus = MatchStatus.AVAILABLE,
        isVerified: Boolean = true
    ): MatchProfile {
        return MatchProfile(
            id = id,
            name = name,
            age = 27,
            city = "Kolkata",
            state = "West Bengal",
            profession = "Architect",
            education = "B.Arch",
            company = "Studio Design",
            height = "5' 5\"",
            religion = "Hindu",
            community = "Bengali",
            motherTongue = "Bengali",
            bio = "Sample bio for $name",
            photoAccentColor = 0xFF9F1239,
            trustScore = 92,
            isIdentityVerified = isVerified,
            isPhotoVerified = isVerified,
            isFamilyApproved = true,
            isHumanRecommended = true,
            isMutualMatch = true,
            category = MatchCategory.DAILY_5,
            compatibility = CompatibilityDetails(
                overallScore = score,
                lifestyleScore = score,
                familyExpectationsScore = score,
                locationScore = 95,
                careerScore = 85,
                communicationScore = 90,
                futureGoalsScore = 88,
                whyRecommendedPoints = listOf("Shared city", "High lifestyle harmony"),
                commonGround = listOf("Arts & culture"),
                topicsToDiscuss = listOf("Family goals")
            ),
            familyBackground = "Well educated family",
            livingArrangement = "Comfortable with independent residence",
            careerExpectation = "Supportive of partner growth",
            childrenTimeline = "2-3 years",
            status = status
        )
    }

    @Test
    fun testResolveMatchRequestsForDigest_filtersAndSortsCorrectly() {
        val emailRepo = EmailRepository()
        val service = DailyDigestNotificationService(emailRepo)

        val matches = listOf(
            createSampleMatch("m1", "Candidate Low Score", score = 65), // Below default 75
            createSampleMatch("m2", "Candidate Top", score = 96),
            createSampleMatch("m3", "Candidate Blocked", score = 92, status = MatchStatus.BLOCKED),
            createSampleMatch("m4", "Candidate Declined", score = 90, status = MatchStatus.DECLINED),
            createSampleMatch("m5", "Candidate Good", score = 88),
            createSampleMatch("m6", "Candidate Unverified", score = 89, isVerified = false)
        )

        // Default settings (min score 75, onlyVerified = false)
        val resolved = service.resolveMatchRequestsForDigest(matches)
        assertEquals(3, resolved.size)
        assertEquals("m2", resolved[0].id) // 96%
        assertEquals("m6", resolved[1].id) // 89%
        assertEquals("m5", resolved[2].id) // 88%

        // With onlyVerifiedCandidates = true
        val verifiedOnly = service.resolveMatchRequestsForDigest(
            matches,
            DailyDigestSettings(onlyVerifiedCandidates = true)
        )
        assertEquals(2, verifiedOnly.size)
        assertEquals("m2", verifiedOnly[0].id)
        assertEquals("m5", verifiedOnly[1].id)
    }

    @Test
    fun testSettingsUpdate_persistsState() {
        val service = DailyDigestNotificationService()
        assertEquals(true, service.settings.value.isEnabled)
        assertEquals("09:00 AM", service.settings.value.preferredTime)

        service.setDigestEnabled(false)
        assertEquals(false, service.settings.value.isEnabled)

        service.setPreferredTime("07:00 PM")
        assertEquals("07:00 PM", service.settings.value.preferredTime)

        service.setMinimumCompatibility(85)
        assertEquals(85, service.settings.value.minimumCompatibilityScore)
    }

    @Test
    fun testSendDailyDigest_invokesEmailRepositoryAndUpdatesHistory() = runBlocking {
        val emailRepo = EmailRepository()
        val service = DailyDigestNotificationService(emailRepo)

        val requests = listOf(
            createSampleMatch("m1", "Priya Mukherjee", score = 94),
            createSampleMatch("m2", "Sneha Banerjee", score = 91)
        )

        val result = service.sendDailyDigest(
            recipientEmail = "aarav.sen@example.com",
            recipientName = "Aarav Sen",
            matchRequests = requests,
            digestDate = "26 September 2026",
            forceSend = true
        )

        // EmailRepository was called and delivery result produced
        assertNotNull(result)
        val digestResult = service.lastDigestResult.value
        assertNotNull(digestResult)
        assertEquals("aarav.sen@example.com", digestResult?.recipientEmail)
        assertEquals("Aarav Sen", digestResult?.recipientName)
        assertEquals(2, digestResult?.matchRequestsCount)
        assertEquals(94, digestResult?.topCompatibilityScore)
        assertEquals("26 September 2026", digestResult?.digestDate)

        // Verify digest history tracked
        assertTrue(service.digestHistory.value.isNotEmpty())
        assertEquals(digestResult, service.digestHistory.value.first())

        // Verify that EmailRepository recorded the log entry
        val logs = emailRepo.emailLogs.value
        assertTrue("Email logs should contain dispatched digest", logs.any { it.emailType == "DAILY_DIGEST" })
        val digestLog = logs.first { it.emailType == "DAILY_DIGEST" }
        assertEquals("aarav.sen@example.com", digestLog.recipientEmail)
        assertTrue(digestLog.subject.contains("2 new match requests"))
    }
}
