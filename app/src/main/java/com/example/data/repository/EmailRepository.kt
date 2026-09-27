package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.service.EmailDeliveryResult
import com.example.data.service.EmailLogEntry
import com.example.data.service.OtpSendResult
import com.example.data.service.OtpVerificationStatus
import com.example.data.service.ZohoEmailService
import com.example.data.model.MatchProfile
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Repository responsible for managing transactional email communication
 * using the Zoho ZeptoMail / CPaaS REST API and credentials from .env.
 */
class EmailRepository(
    val emailService: ZohoEmailService = ZohoEmailService()
) {
    companion object {
        private const val TAG = "EmailRepository"

        private fun logD(tag: String, msg: String) {
            try {
                Log.d(tag, msg)
            } catch (_: Throwable) {
                // JVM unit tests
            }
        }

        /**
         * Resolves the Zoho ZeptoMail API authorization key from BuildConfig (injected from .env).
         */
        val API_KEY: String
            get() = try {
                val key = BuildConfig::class.java.getField("ZOHO_CPAAS_API_KEY").get(null) as? String
                if (!key.isNullOrBlank() && !key.contains("MY_")) key else ZohoEmailService.DEFAULT_API_KEY
            } catch (_: Throwable) {
                ZohoEmailService.DEFAULT_API_KEY
            }

        /**
         * Resolves the authorized sender domain / email address from BuildConfig (injected from .env).
         */
        val SENDER_EMAIL: String
            get() = try {
                val email = BuildConfig::class.java.getField("ZOHO_SENDER_EMAIL").get(null) as? String
                if (!email.isNullOrBlank() && !email.contains("MY_")) email else ZohoEmailService.DEFAULT_SENDER_EMAIL
            } catch (_: Throwable) {
                ZohoEmailService.DEFAULT_SENDER_EMAIL
            }

        /**
         * Resolves the Zoho CPaaS host from BuildConfig (injected from .env).
         */
        val CPAAS_HOST: String
            get() = try {
                val host = BuildConfig::class.java.getField("ZOHO_CPAAS_HOST").get(null) as? String
                if (!host.isNullOrBlank()) host else "cpaas.zoho.in"
            } catch (_: Throwable) {
                "cpaas.zoho.in"
            }

        /**
         * Resolves the Zoho CPaaS Agent Alias from BuildConfig (injected from .env).
         */
        val AGENT_ALIAS: String
            get() = try {
                val alias = BuildConfig::class.java.getField("ZOHO_AGENT_ALIAS").get(null) as? String
                if (!alias.isNullOrBlank()) alias else ZohoEmailService.DEFAULT_AGENT_ALIAS
            } catch (_: Throwable) {
                ZohoEmailService.DEFAULT_AGENT_ALIAS
            }
    }

    init {
        // Synchronize service with resolved .env credentials
        emailService.updateCredentials(
            newApiKey = API_KEY,
            newSenderEmail = SENDER_EMAIL
        )
    }

    /**
     * Set of currently verified email addresses.
     */
    val verifiedEmails: StateFlow<Set<String>> = emailService.verifiedEmails

    /**
     * Audit log history of sent emails.
     */
    val emailLogs: StateFlow<List<EmailLogEntry>> = emailService.emailLogs

    /**
     * Sends an OTP verification email to the user using Zoho ZeptoMail API.
     * Generates a 6-digit cryptographic OTP valid for [expiryMinutes].
     */
    suspend fun sendOtpVerificationEmail(
        recipientEmail: String,
        recipientName: String = "Esteemed Member",
        expiryMinutes: Int = 10
    ): Result<OtpSendResult> {
        logD(TAG, "Dispatching OTP verification email to $recipientEmail via Zoho ZeptoMail ($SENDER_EMAIL)")
        return emailService.sendOtpVerificationEmail(
            toEmail = recipientEmail,
            toName = recipientName,
            expiryMinutes = expiryMinutes
        )
    }

    /**
     * Validates the 6-digit OTP provided by the user.
     * Returns true if verification succeeds.
     */
    fun verifyOtp(email: String, enteredOtp: String): Boolean {
        val status = emailService.verifyOtp(email, enteredOtp)
        logD(TAG, "Verifying OTP for $email: $status")
        return status == OtpVerificationStatus.SUCCESS
    }

    /**
     * Validates the OTP and returns the detailed verification status enum.
     */
    fun verifyOtpWithStatus(email: String, enteredOtp: String): OtpVerificationStatus {
        return emailService.verifyOtp(email, enteredOtp)
    }

    /**
     * Checks if a given email is already verified.
     */
    fun isEmailVerified(email: String): Boolean {
        return emailService.isEmailVerified(email)
    }

    /**
     * Sends a rich registration welcome email to the newly onboarded user.
     */
    suspend fun sendRegistrationWelcomeEmail(
        recipientEmail: String,
        recipientName: String,
        memberId: String = "PS-" + UUID.randomUUID().toString().take(6).uppercase()
    ): Result<EmailDeliveryResult> {
        logD(TAG, "Dispatching welcome email to $recipientEmail ($memberId) via Zoho ZeptoMail")
        return emailService.sendWelcomeEmail(
            toEmail = recipientEmail,
            toName = recipientName,
            profileId = memberId
        )
    }

    /**
     * Sends custom transactional emails with HTML content via Zoho ZeptoMail.
     */
    suspend fun sendTransactionalEmail(
        recipientEmail: String,
        recipientName: String,
        subject: String,
        htmlBody: String
    ): Result<EmailDeliveryResult> {
        return emailService.sendEmail(
            toEmail = recipientEmail,
            toName = recipientName,
            subject = subject,
            htmlBody = htmlBody,
            emailType = "TRANSACTIONAL"
        )
    }

    /**
     * Sends a daily digest email summarizing new match requests and recommendations
     * using the Zoho ZeptoMail API.
     */
    suspend fun sendDailyDigestEmail(
        recipientEmail: String,
        recipientName: String,
        matchRequests: List<MatchProfile>,
        digestDate: String = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    ): Result<EmailDeliveryResult> {
        logD(TAG, "Dispatching daily digest email to $recipientEmail (${matchRequests.size} match requests) via Zoho ZeptoMail ($SENDER_EMAIL)")
        return emailService.sendDailyDigestEmail(
            toEmail = recipientEmail,
            toName = recipientName,
            matchRequests = matchRequests,
            digestDate = digestDate
        )
    }
}
