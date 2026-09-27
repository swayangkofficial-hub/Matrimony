package com.example

import com.example.data.model.UserProfile
import com.example.data.repository.UserProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedFiltersTest {

    private val repository = UserProfileRepository()

    @Test
    fun testFilterByFamilyValues() {
        val sampleProfiles = listOf(
            UserProfile(id = "1", name = "Traditional Person", familyValues = "Traditional"),
            UserProfile(id = "2", name = "Moderate Person", familyValues = "Moderate"),
            UserProfile(id = "3", name = "Liberal Person", familyValues = "Liberal")
        )

        val traditionalResults = repository.filterProfiles(
            profiles = sampleProfiles,
            minAge = 0,
            maxAge = 100,
            location = "All",
            gender = "All",
            verifiedOnly = false,
            familyValues = "Traditional"
        )
        assertEquals(1, traditionalResults.size)
        assertEquals("Traditional Person", traditionalResults[0].name)

        val liberalResults = repository.filterProfiles(
            profiles = sampleProfiles,
            minAge = 0,
            maxAge = 100,
            location = "All",
            gender = "All",
            verifiedOnly = false,
            familyValues = "Liberal"
        )
        assertEquals(1, liberalResults.size)
        assertEquals("Liberal Person", liberalResults[0].name)
    }

    @Test
    fun testFilterByDietaryPreferences() {
        val sampleProfiles = listOf(
            UserProfile(id = "1", name = "Pure Veg", dietaryPreference = "Pure Vegetarian"),
            UserProfile(id = "2", name = "Non Veg", dietaryPreference = "Non-Vegetarian"),
            UserProfile(id = "3", name = "Jain", dietaryPreference = "Jain")
        )

        val vegResults = repository.filterProfiles(
            profiles = sampleProfiles,
            minAge = 0,
            maxAge = 100,
            location = "All",
            gender = "All",
            verifiedOnly = false,
            dietaryPreference = "Pure Vegetarian"
        )
        assertEquals(1, vegResults.size)
        assertEquals("Pure Veg", vegResults[0].name)
    }

    @Test
    fun testFilterByEducationLevel() {
        val sampleProfiles = listOf(
            UserProfile(id = "1", name = "MBA Grad", education = "MBA, IIM", educationLevel = "Master's / Postgraduate"),
            UserProfile(id = "2", name = "PhD Scholar", education = "PhD in Physics", educationLevel = "Doctorate"),
            UserProfile(id = "3", name = "Engineer", education = "B.Tech", educationLevel = "Bachelor's / Graduate")
        )

        val docResults = repository.filterProfiles(
            profiles = sampleProfiles,
            minAge = 0,
            maxAge = 100,
            location = "All",
            gender = "All",
            verifiedOnly = false,
            educationLevel = "Doctorate"
        )
        assertEquals(1, docResults.size)
        assertEquals("PhD Scholar", docResults[0].name)
    }

    @Test
    fun testCombinedAdvancedFilters() {
        val seedProfiles = repository.getSampleSeedProfiles()
        assertTrue("Seed profiles should not be empty", seedProfiles.isNotEmpty())

        val filtered = repository.filterProfiles(
            profiles = seedProfiles,
            minAge = 20,
            maxAge = 40,
            location = "All",
            gender = "All",
            verifiedOnly = false,
            familyValues = "Traditional",
            educationLevel = "Master's / Postgraduate",
            dietaryPreference = "Vegetarian"
        )
        assertTrue("Expected matching traditional vegetarian postgraduate candidate", filtered.isNotEmpty())
    }
}
