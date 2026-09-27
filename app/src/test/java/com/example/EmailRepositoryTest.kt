package com.example

import com.example.data.repository.EmailRepository
import com.example.data.service.OtpVerificationStatus
import com.example.data.service.ZohoEmailService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailRepositoryTest {

    @Test
    fun testEmailRepository_defaultConfig() {
        val repo = EmailRepository()
        assertNotNull(EmailRepository.API_KEY)
        assertTrue(EmailRepository.API_KEY.isNotBlank())
        assertEquals("prem.setu@technope.co.in", EmailRepository.SENDER_EMAIL)
        assertEquals("cpaas.zoho.in", EmailRepository.CPAAS_HOST)
        assertEquals("30dd78d1e1c9bf86", EmailRepository.AGENT_ALIAS)
    }

    @Test
    fun testEmailRepository_otpVerification_notRequested() {
        val repo = EmailRepository()
        val email = "unknown_user@example.com"
        assertFalse(repo.isEmailVerified(email))
        val status = repo.verifyOtpWithStatus(email, "123456")
        assertEquals(OtpVerificationStatus.EXPIRED_OR_NOT_FOUND, status)
        assertFalse(repo.verifyOtp(email, "123456"))
    }

    @Test
    fun testEmailRepository_sendDailyDigestEmail_dispatchesViaService() = kotlinx.coroutines.runBlocking {
        val repo = EmailRepository()
        val result = repo.sendDailyDigestEmail(
            recipientEmail = "test@example.com",
            recipientName = "Test Member",
            matchRequests = emptyList(),
            digestDate = "26 Sep 2026"
        )
        assertNotNull(result)
        val logs = repo.emailLogs.value
        assertTrue("Email logs should track DAILY_DIGEST dispatch", logs.any { it.emailType == "DAILY_DIGEST" })
    }
}
