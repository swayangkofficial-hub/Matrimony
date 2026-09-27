package com.example

import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import com.example.ui.components.COMPATIBILITY_QUIZ_QUESTIONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilityQuizTest {

    private val repository = UserProfileRepository()

    @Test
    fun testQuizQuestions_structureAndCoverage() {
        assertEquals(5, COMPATIBILITY_QUIZ_QUESTIONS.size)

        val categories = COMPATIBILITY_QUIZ_QUESTIONS.map { it.id }.toSet()
        assertTrue(categories.contains("family_values"))
        assertTrue(categories.contains("dietary_preference"))
        assertTrue(categories.contains("living_arrangement"))
        assertTrue(categories.contains("career_expectation"))
        assertTrue(categories.contains("children_timeline"))

        COMPATIBILITY_QUIZ_QUESTIONS.forEach { question ->
            assertTrue(question.options.isNotEmpty())
            question.options.forEach { option ->
                assertTrue(option.id.isNotBlank())
                assertTrue(option.label.isNotBlank())
                assertTrue(option.description.isNotBlank())
            }
        }
    }

    @Test
    fun testQuizDynamicProfileUpdates_updatesAttributes() {
        var profile = UserProfile(
            id = "user_me",
            name = "Aarav",
            familyValues = "Moderate",
            dietaryPreference = "Vegetarian"
        )

        // 1. Answer Question 1: Family Values -> Traditional
        val q1 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "family_values" }
        val optTraditional = q1.options.first { it.id == "Traditional" }
        profile = optTraditional.applyToProfile(profile)
        assertEquals("Traditional", profile.familyValues)
        assertEquals("FAMILY_DRIVEN", profile.familyMode)

        // 2. Answer Question 2: Dietary -> Pure Vegetarian
        val q2 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "dietary_preference" }
        val optPureVeg = q2.options.first { it.id == "Pure Vegetarian" }
        profile = optPureVeg.applyToProfile(profile)
        assertEquals("Pure Vegetarian", profile.dietaryPreference)

        // 3. Answer Question 3: Living Arrangement -> Independent
        val q3 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "living_arrangement" }
        val optIndependent = q3.options.first { it.id == "independent" }
        profile = optIndependent.applyToProfile(profile)
        assertTrue(profile.livingArrangement.contains("independent", ignoreCase = true))

        // 4. Answer Question 4: Career -> Ambitious
        val q4 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "career_expectation" }
        val optAmbitious = q4.options.first { it.id == "ambitious" }
        profile = optAmbitious.applyToProfile(profile)
        assertTrue(profile.careerExpectation.contains("ambitious", ignoreCase = true))

        // 5. Answer Question 5: Children -> 2-3 years
        val q5 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "children_timeline" }
        val opt23 = q5.options.first { it.id == "timeline_2_3" }
        profile = opt23.applyToProfile(profile)
        assertTrue(profile.childrenTimeline.contains("2-3", ignoreCase = true))
    }

    @Test
    fun testQuizRefinesCompatibilityMatchingAlgorithm() {
        // Candidate profile who has Traditional values and Pure Vegetarian diet
        val candidate = UserProfile(
            id = "candidate_1",
            name = "Priya",
            age = 27,
            city = "Kolkata",
            state = "West Bengal",
            religion = "Hindu",
            community = "Bengali Brahmin",
            motherTongue = "Bengali",
            familyValues = "Traditional",
            dietaryPreference = "Pure Vegetarian",
            livingArrangement = "Comfortable with independent residence near parents",
            childrenTimeline = "Wants children in 2-3 years post marriage."
        )

        // User profile BEFORE taking quiz (mismatched/uncalibrated values)
        val initialUser = UserProfile(
            id = "user_me",
            name = "Aarav",
            age = 29,
            city = "Kolkata",
            state = "West Bengal",
            religion = "Hindu",
            community = "Bengali Brahmin",
            motherTongue = "Bengali",
            familyValues = "Liberal",
            dietaryPreference = "Non-Vegetarian",
            livingArrangement = "Prefers joint family living with elder parents",
            childrenTimeline = "Flexible and mutually decided when comfortable"
        )

        val initialScore = repository.calculateCompatibilityScore(initialUser, candidate)
        val initialBreakdown = repository.calculateCompatibilityBreakdown(initialUser, candidate)

        // User answers quiz, aligning with Traditional values and Pure Vegetarian lifestyle
        var calibratedUser = initialUser
        val q1 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "family_values" }
        calibratedUser = q1.options.first { it.id == "Traditional" }.applyToProfile(calibratedUser)

        val q2 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "dietary_preference" }
        calibratedUser = q2.options.first { it.id == "Pure Vegetarian" }.applyToProfile(calibratedUser)

        val q3 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "living_arrangement" }
        calibratedUser = q3.options.first { it.id == "independent" }.applyToProfile(calibratedUser)

        val q5 = COMPATIBILITY_QUIZ_QUESTIONS.first { it.id == "children_timeline" }
        calibratedUser = q5.options.first { it.id == "timeline_2_3" }.applyToProfile(calibratedUser)

        val refinedScore = repository.calculateCompatibilityScore(calibratedUser, candidate)
        val refinedBreakdown = repository.calculateCompatibilityBreakdown(calibratedUser, candidate)

        // Refined score must be strictly higher due to dynamic lifestyle alignment!
        assertTrue(
            "Refined score ($refinedScore) should be higher than initial score ($initialScore)",
            refinedScore > initialScore
        )
        assertTrue(
            "Refined lifestyle score (${refinedBreakdown.lifestyleFamilyScore}) should be higher than initial (${initialBreakdown.lifestyleFamilyScore})",
            refinedBreakdown.lifestyleFamilyScore > initialBreakdown.lifestyleFamilyScore
        )
    }
}
