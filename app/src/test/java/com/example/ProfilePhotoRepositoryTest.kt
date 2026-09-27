package com.example

import com.example.data.repository.ProfilePhotoRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePhotoRepositoryTest {

    @Test
    fun testRepositoryInitialization() {
        val repo = ProfilePhotoRepository { null }
        assertNotNull(repo)
    }

    @Test
    fun testDeletePhoto_graceful() = runBlocking {
        val repo = ProfilePhotoRepository { null }
        val result = repo.deleteProfilePhoto("https://firebasestorage.googleapis.com/v0/b/app/test.jpg")
        assertTrue(result.isSuccess)
    }
}
