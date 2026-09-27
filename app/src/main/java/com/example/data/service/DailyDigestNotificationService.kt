package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.MatchCategory
import com.example.data.model.MatchProfile
import com.example.data.model.MatchStatus
import com.example.data.model.UserProfile
import com.example.data.repository.EmailRepository
import com.example.data.repository.PremSetuRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Result model representing the outcome of a Daily Digest email dispatch.
 */
data class DailyDigestResult(
    val success: Boolean,
    val recipientEmail: String,
    val recipientName: String,
    val matchRequestsCount: Int,
    val topCompatibilityScore: Int,
    val digestDate: String,
    val deliveryMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Settings configuration for the Daily Digest notification service.
 */
data class DailyDigestSettings(
    val isEnabled: Boolean = true,
    val preferredTime: String = "09:00 AM",
    val minimumCompatibilityScore: Int = 75,
    val onlyVerifiedCandidates: Boolean = false,
    val sendPushNotificationAlert: Boolean = true
)

/**
 * Notification service responsible for assembling, formatting, and dispatching
 * daily digest emails summarizing new match requests to users using [EmailRepository].
 *
 * Features:
 * 1. Collects and filters pending match requests & high-compatibility recommendations.
 * 2. Formats and sends a rich matrimonial HTML digest via [EmailRepository] (powered by Zoho CPaaS / ZeptoMail).
 * 3. Dispatches local Android notifications to alert the member on their device.
 * 4. Tracks delivery telemetry, history, and duplicate dispatch prevention for the calendar day.
 * 5. Provides scheduled daily alarm hooks and background Service/Receiver execution.
 */
class DailyDigestNotificationService(
    val emailRepository: EmailRepository = EmailRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    companion object {
        const val TAG = "DailyDigestService"
        const val NOTIFICATION_CHANNEL_ID = "prem_setu_daily_digest_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Prem Setu Daily Match Digest"
        const val NOTIFICATION_ID = 2001

        const val ACTION_SEND_DAILY_DIGEST = "com.example.action.SEND_DAILY_DIGEST"
        const val PREFS_NAME = "prem_setu_daily_digest_prefs"
        const val KEY_LAST_SENT_DATE = "last_digest_sent_date"
        const val KEY_DIGEST_ENABLED = "digest_enabled"
        const val KEY_DIGEST_TIME = "digest_time"

        @Volatile
        private var instance: DailyDigestNotificationService? = null

        fun getInstance(emailRepository: EmailRepository = EmailRepository()): DailyDigestNotificationService {
            return instance ?: synchronized(this) {
                instance ?: DailyDigestNotificationService(emailRepository).also { instance = it }
            }
        }

        private fun logD(msg: String) {
            try {
                Log.d(TAG, msg)
            } catch (_: Throwable) {
                // JVM unit tests
            }
        }

        private fun logW(msg: String) {
            try {
                Log.w(TAG, msg)
            } catch (_: Throwable) {
                // JVM unit tests
            }
        }
    }

    private val _settings = MutableStateFlow(DailyDigestSettings())
    val settings: StateFlow<DailyDigestSettings> = _settings.asStateFlow()

    private val _lastDigestResult = MutableStateFlow<DailyDigestResult?>(null)
    val lastDigestResult: StateFlow<DailyDigestResult?> = _lastDigestResult.asStateFlow()

    private val _digestHistory = MutableStateFlow<List<DailyDigestResult>>(emptyList())
    val digestHistory: StateFlow<List<DailyDigestResult>> = _digestHistory.asStateFlow()

    private val _isDispatching = MutableStateFlow(false)
    val isDispatching: StateFlow<Boolean> = _isDispatching.asStateFlow()

    fun updateSettings(newSettings: DailyDigestSettings) {
        _settings.value = newSettings
        logD("Daily digest settings updated: $newSettings")
    }

    fun setDigestEnabled(enabled: Boolean) {
        _settings.update { it.copy(isEnabled = enabled) }
    }

    fun setPreferredTime(time: String) {
        _settings.update { it.copy(preferredTime = time) }
    }

    fun setMinimumCompatibility(score: Int) {
        _settings.update { it.copy(minimumCompatibilityScore = score.coerceIn(50, 100)) }
    }

    /**
     * Filters and resolves the list of new match requests to summarize in the daily digest.
     */
    fun resolveMatchRequestsForDigest(
        allMatches: List<MatchProfile>,
        settings: DailyDigestSettings = _settings.value
    ): List<MatchProfile> {
        return allMatches.filter { match ->
            // Exclude blocked or declined matches
            if (match.status == MatchStatus.BLOCKED || match.status == MatchStatus.DECLINED) {
                return@filter false
            }

            // Must meet minimum compatibility threshold
            if (match.compatibility.overallScore < settings.minimumCompatibilityScore) {
                return@filter false
            }

            // Check verified filter if requested
            if (settings.onlyVerifiedCandidates && !match.isIdentityVerified) {
                return@filter false
            }

            // Include match requests, mutual matches, or daily top recommendations
            match.status == MatchStatus.INTEREST_SENT ||
                    match.status == MatchStatus.AVAILABLE ||
                    match.category == MatchCategory.DAILY_5 ||
                    match.category == MatchCategory.MUTUAL ||
                    match.category == MatchCategory.HUMAN_RECOMMENDED
        }.sortedByDescending { it.compatibility.overallScore }
    }

    /**
     * Sends the Daily Digest email to the user summarizing their new match requests using [EmailRepository].
     *
     * @param recipientEmail Destination email address for the digest.
     * @param recipientName Name of the user receiving the digest.
     * @param matchRequests List of [MatchProfile] instances to summarize.
     * @param context Optional Android Context for system notification and SharedPreferences storage.
     * @param digestDate Formatted date string (defaults to current date).
     * @param forceSend If false, skips sending if a digest was already dispatched on this calendar day.
     */
    suspend fun sendDailyDigest(
        recipientEmail: String,
        recipientName: String,
        matchRequests: List<MatchProfile>,
        context: Context? = null,
        digestDate: String = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()),
        forceSend: Boolean = false
    ): Result<DailyDigestResult> = withContext(Dispatchers.IO) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (!forceSend && context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastSent = prefs.getString(KEY_LAST_SENT_DATE, null)
            if (lastSent == todayKey) {
                logD("Daily digest for $recipientEmail was already sent today ($todayKey). Skipping duplicate.")
                val existing = _lastDigestResult.value
                if (existing != null) {
                    return@withContext Result.success(existing)
                }
            }
        }

        _isDispatching.value = true
        logD("Preparing daily digest email for $recipientEmail with ${matchRequests.size} match requests.")

        val topScore = matchRequests.maxOfOrNull { it.compatibility.overallScore } ?: 0

        // Dispatch via EmailRepository
        val emailResult = emailRepository.sendDailyDigestEmail(
            recipientEmail = recipientEmail,
            recipientName = recipientName,
            matchRequests = matchRequests,
            digestDate = digestDate
        )

        _isDispatching.value = false

        return@withContext emailResult.fold(
            onSuccess = { delivery ->
                val digestResult = DailyDigestResult(
                    success = delivery.success,
                    recipientEmail = recipientEmail,
                    recipientName = recipientName,
                    matchRequestsCount = matchRequests.size,
                    topCompatibilityScore = topScore,
                    digestDate = digestDate,
                    deliveryMessage = delivery.message
                )

                _lastDigestResult.value = digestResult
                _digestHistory.update { listOf(digestResult) + it.take(29) }

                // Record date in SharedPreferences
                context?.let { ctx ->
                    try {
                        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_LAST_SENT_DATE, todayKey)
                            .apply()
                    } catch (t: Throwable) {
                        logW("Could not save last sent date: ${t.message}")
                    }

                    // Post system notification if enabled
                    if (_settings.value.sendPushNotificationAlert) {
                        postLocalDigestNotification(ctx, digestResult)
                    }
                }

                logD("Daily digest email successfully sent to $recipientEmail: ${delivery.message}")
                Result.success(digestResult)
            },
            onFailure = { error ->
                logW("Failed to send daily digest email to $recipientEmail: ${error.message}")
                val failureResult = DailyDigestResult(
                    success = false,
                    recipientEmail = recipientEmail,
                    recipientName = recipientName,
                    matchRequestsCount = matchRequests.size,
                    topCompatibilityScore = topScore,
                    digestDate = digestDate,
                    deliveryMessage = error.message ?: "Dispatch failed"
                )
                _lastDigestResult.value = failureResult
                _digestHistory.update { listOf(failureResult) + it.take(29) }
                Result.failure(error)
            }
        )
    }

    /**
     * Checks whether the user has received today's digest and sends it if eligible.
     */
    suspend fun checkAndSendScheduledDigest(
        userProfile: UserProfile,
        repository: PremSetuRepository,
        context: Context? = null,
        forceSend: Boolean = false
    ): Result<DailyDigestResult> {
        val targetEmail = userProfile.email.ifBlank { "member@technope.co.in" }
        val targetName = userProfile.name.ifBlank { "Prem Setu Member" }
        val allMatches = repository.matches.value
        val eligibleRequests = resolveMatchRequestsForDigest(allMatches)

        return sendDailyDigest(
            recipientEmail = targetEmail,
            recipientName = targetName,
            matchRequests = eligibleRequests,
            context = context,
            forceSend = forceSend
        )
    }

    /**
     * Posts a local Android push notification informing the member that their Daily Digest is available.
     */
    fun postLocalDigestNotification(context: Context, digestResult: DailyDigestResult) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Create notification channel on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily matrimonial match requests and compatibility digests"
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val count = digestResult.matchRequestsCount
            val title = "💌 Prem Setu Daily Match Digest Sent"
            val text = if (count > 0) {
                "Summarized $count new match requests to ${digestResult.recipientEmail}. Top match: ${digestResult.topCompatibilityScore}%."
            } else {
                "Your daily matrimonial summary has been delivered to ${digestResult.recipientEmail}."
            }

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
            logD("Local push notification posted for daily digest: $title")
        } catch (t: Throwable) {
            logW("Could not post local notification: ${t.message}")
        }
    }
}

/**
 * Android Service to execute daily digest operations in the background.
 */
class DailyDigestService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val email = intent?.getStringExtra("recipient_email") ?: "member@technope.co.in"
        val name = intent?.getStringExtra("recipient_name") ?: "Prem Setu Member"

        serviceScope.launch {
            try {
                val digestService = DailyDigestNotificationService.getInstance()
                val repository = PremSetuRepository()
                val matches = repository.matches.value
                val matchRequests = digestService.resolveMatchRequestsForDigest(matches)

                digestService.sendDailyDigest(
                    recipientEmail = email,
                    recipientName = name,
                    matchRequests = matchRequests,
                    context = applicationContext,
                    forceSend = true
                )
            } catch (t: Throwable) {
                Log.w("DailyDigestService", "Background daily digest dispatch error: ${t.message}")
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}

/**
 * BroadcastReceiver triggered by AlarmManager or system events to dispatch the daily digest email.
 */
class DailyDigestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val serviceIntent = Intent(context, DailyDigestService::class.java).apply {
            intent?.extras?.let { putExtras(it) }
        }
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.w("DailyDigestReceiver", "Failed to start DailyDigestService: ${e.message}")
        }
    }
}
