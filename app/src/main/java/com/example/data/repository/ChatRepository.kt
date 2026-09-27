package com.example.data.repository

import android.util.Log
import com.example.data.model.ChatMessage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Repository responsible for managing real-time chat messages between matched users
 * using Cloud Firestore collections ("chats/{conversationId}/messages").
 */
class ChatRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }
) {
    companion object {
        private const val TAG = "ChatRepository"
        const val COLLECTION_CHATS = "chats"
        const val SUBCOLLECTION_MESSAGES = "messages"
        const val COLLECTION_USERS = "users"

        private fun logD(tag: String, msg: String) {
            try {
                Log.d(tag, msg)
            } catch (_: Throwable) {
                // JVM unit tests
            }
        }

        private fun logW(tag: String, msg: String) {
            try {
                Log.w(tag, msg)
            } catch (_: Throwable) {
                // JVM unit tests
            }
        }

        /**
         * Computes a deterministic conversation ID for two users.
         */
        fun getConversationId(userId1: String, userId2: String): String {
            val u1 = userId1.trim()
            val u2 = userId2.trim()
            return if (u1.isNotBlank() && u2.isNotBlank()) {
                listOf(u1, u2).sorted().joinToString("_")
            } else {
                (u1.ifBlank { u2 }).ifBlank { "default_conversation" }
            }
        }
    }

    // In-memory fallback message storage per conversationId for offline or test environments
    private val fallbackCache = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

    // In-memory blocked users set per userId: userId -> List<blockedUserId>
    private val blockedUsersCache = mutableMapOf<String, MutableStateFlow<List<String>>>()

    private fun getOrCreateFallbackFlow(conversationId: String): MutableStateFlow<List<ChatMessage>> {
        return synchronized(fallbackCache) {
            fallbackCache.getOrPut(conversationId) {
                MutableStateFlow(emptyList())
            }
        }
    }

    private fun getOrCreateBlockedFlow(userId: String): MutableStateFlow<List<String>> {
        return synchronized(blockedUsersCache) {
            blockedUsersCache.getOrPut(userId) {
                MutableStateFlow(emptyList())
            }
        }
    }

    /**
     * Checks if targetUserId is blocked by currentUserId.
     */
    fun isUserBlocked(currentUserId: String, targetUserId: String): Boolean {
        if (currentUserId.isBlank() || targetUserId.isBlank()) return false
        val blockedList = getOrCreateBlockedFlow(currentUserId).value
        return blockedList.contains(targetUserId)
    }

    /**
     * Blocks a user, updating the user's Firestore document in 'users/{currentUserId}'
     * with the blocked target user ID, restricting chat communications.
     */
    suspend fun blockUser(
        currentUserId: String,
        targetUserId: String,
        reason: String = "Inappropriate communication"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(currentUserId.isNotBlank()) { "Current user ID cannot be blank" }
            require(targetUserId.isNotBlank()) { "Target user ID cannot be blank" }

            // 1. Update in-memory blocked state
            val flow = getOrCreateBlockedFlow(currentUserId)
            flow.update { current ->
                if (!current.contains(targetUserId)) current + targetUserId else current
            }

            val conversationId = getConversationId(currentUserId, targetUserId)

            // 2. Add an in-conversation system advisory message
            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val advisoryMsg = ChatMessage(
                id = "alert_block_" + UUID.randomUUID().toString().take(8),
                matchId = targetUserId,
                senderName = "Prem Setu Safety Guard",
                text = "You blocked this user ($reason). All communication and incoming messages are prevented.",
                timestamp = timeFormatter.format(Date()),
                isMine = false,
                isSafetyAlert = true,
                alertWarning = "Blocked Profile",
                senderId = "system_safety",
                createdAt = System.currentTimeMillis()
            )
            getOrCreateFallbackFlow(conversationId).update { it + advisoryMsg }

            // 3. Update Firestore
            val fs = firestoreProvider()
            if (fs != null) {
                try {
                    // Update user's Firestore document block list
                    val userDocRef = fs.collection(COLLECTION_USERS).document(currentUserId)
                    userDocRef.set(
                        mapOf(
                            "blockedUsers" to FieldValue.arrayUnion(targetUserId),
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()

                    // Update chat conversation metadata to blocked status
                    fs.collection(COLLECTION_CHATS).document(conversationId).set(
                        mapOf(
                            "isBlocked" to true,
                            "blockedBy" to currentUserId,
                            "blockedTarget" to targetUserId,
                            "blockReason" to reason,
                            "blockedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()

                    // Also store the advisory message in the messages subcollection
                    val msgData = mapOf(
                        "id" to advisoryMsg.id,
                        "matchId" to targetUserId,
                        "senderId" to "system_safety",
                        "senderName" to "Prem Setu Safety Guard",
                        "text" to advisoryMsg.text,
                        "timestamp" to advisoryMsg.timestamp,
                        "createdAt" to advisoryMsg.createdAt,
                        "isSafetyAlert" to true,
                        "alertWarning" to "Blocked Profile",
                        "isMine" to false
                    )
                    fs.collection(COLLECTION_CHATS)
                        .document(conversationId)
                        .collection(SUBCOLLECTION_MESSAGES)
                        .document(advisoryMsg.id)
                        .set(msgData)
                        .await()

                    logD(TAG, "User $targetUserId successfully blocked in Firestore doc for $currentUserId")
                } catch (e: Throwable) {
                    logW(TAG, "Firestore block update failed, kept in local state: ${e.message}")
                }
            }
        }
    }

    /**
     * Unblocks a previously blocked user, updating the user's Firestore document.
     */
    suspend fun unblockUser(
        currentUserId: String,
        targetUserId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(currentUserId.isNotBlank()) { "Current user ID cannot be blank" }
            require(targetUserId.isNotBlank()) { "Target user ID cannot be blank" }

            // Update in-memory state
            val flow = getOrCreateBlockedFlow(currentUserId)
            flow.update { current ->
                current.filter { it != targetUserId }
            }

            val conversationId = getConversationId(currentUserId, targetUserId)

            // Update Firestore
            val fs = firestoreProvider()
            if (fs != null) {
                try {
                    val userDocRef = fs.collection(COLLECTION_USERS).document(currentUserId)
                    userDocRef.update("blockedUsers", FieldValue.arrayRemove(targetUserId)).await()

                    fs.collection(COLLECTION_CHATS).document(conversationId).set(
                        mapOf(
                            "isBlocked" to false,
                            "unblockedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()

                    logD(TAG, "User $targetUserId successfully unblocked in Firestore for $currentUserId")
                } catch (e: Throwable) {
                    logW(TAG, "Firestore unblock update failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Returns a real-time Flow of blocked user IDs for the specified user from Firestore,
     * falling back to local state when offline.
     */
    fun getBlockedUsers(currentUserId: String): Flow<List<String>> = callbackFlow {
        val fs = firestoreProvider()
        val localFlow = getOrCreateBlockedFlow(currentUserId)

        if (fs == null) {
            val job = launch {
                localFlow.collect { trySend(it) }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        val userDocRef = fs.collection(COLLECTION_USERS).document(currentUserId)
        val registration = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logW(TAG, "Failed to listen to blockedUsers for $currentUserId: ${error.message}")
                trySend(localFlow.value)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val list = (snapshot.get("blockedUsers") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                localFlow.value = list
                trySend(list)
            } else {
                trySend(localFlow.value)
            }
        }

        awaitClose { registration.remove() }
    }

    /**
     * Listens to real-time message stream for a specific conversation from Firestore.
     * Orders messages by createdAt ascending.
     */
    fun getMessages(conversationId: String, currentUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val fs = firestoreProvider()
        if (fs == null) {
            logW(TAG, "Firestore unavailable, using fallback in-memory flow for $conversationId")
            val fallbackFlow = getOrCreateFallbackFlow(conversationId)
            val job = launch {
                fallbackFlow.collect { list ->
                    trySend(list.map { it.copy(isMine = (it.senderId == currentUserId || it.isMine)) })
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        val messagesRef = fs.collection(COLLECTION_CHATS)
            .document(conversationId)
            .collection(SUBCOLLECTION_MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val listenerRegistration = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logW(TAG, "Listen failed for conversation $conversationId: ${error.message}")
                // Fallback to local cache if error occurs
                val fallbackList = getOrCreateFallbackFlow(conversationId).value
                trySend(fallbackList.map { it.copy(isMine = (it.senderId == currentUserId || it.isMine)) })
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val matchId = doc.getString("matchId") ?: ""
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val text = doc.getString("text") ?: ""
                    val timestamp = doc.getString("timestamp") ?: ""
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val isSafetyAlert = doc.getBoolean("isSafetyAlert") ?: false
                    val alertWarning = doc.getString("alertWarning") ?: ""
                    val isMine = (senderId == currentUserId) || (doc.getBoolean("isMine") == true)

                    ChatMessage(
                        id = id,
                        matchId = matchId,
                        senderName = senderName,
                        text = text,
                        timestamp = timestamp,
                        isMine = isMine,
                        isSafetyAlert = isSafetyAlert,
                        alertWarning = alertWarning,
                        senderId = senderId,
                        createdAt = createdAt
                    )
                }

                // Update fallback cache as well
                getOrCreateFallbackFlow(conversationId).value = messages
                trySend(messages)
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Sends a new message to the conversation.
     * Writes to Firestore subcollection "chats/{conversationId}/messages/{msgId}"
     * and updates the parent conversation metadata.
     */
    suspend fun sendMessage(
        conversationId: String,
        matchId: String,
        senderId: String,
        senderName: String,
        text: String
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedText = text.trim()
            if (trimmedText.isBlank()) {
                throw IllegalArgumentException("Cannot send empty message")
            }

            // Check if communication is blocked by either user
            if (isUserBlocked(senderId, matchId) || isUserBlocked(matchId, senderId)) {
                throw IllegalStateException("Communication restricted: This profile is blocked.")
            }

            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = timeFormatter.format(Date())
            val messageId = "msg_" + UUID.randomUUID().toString().take(12)
            val now = System.currentTimeMillis()

            // Automated Matrimonial Safety Filter
            val safetyCheck = analyzeSafety(trimmedText)

            val chatMessage = ChatMessage(
                id = messageId,
                matchId = matchId,
                senderName = senderName,
                text = trimmedText,
                timestamp = formattedTime,
                isMine = true,
                isSafetyAlert = safetyCheck.first,
                alertWarning = safetyCheck.second,
                senderId = senderId,
                createdAt = now
            )

            // 1. Update in-memory fallback cache immediately for responsiveness
            getOrCreateFallbackFlow(conversationId).update { current ->
                current + chatMessage
            }

            // 2. Persist to Firestore
            val fs = firestoreProvider()
            if (fs != null) {
                try {
                    val messageData = mapOf(
                        "id" to chatMessage.id,
                        "matchId" to chatMessage.matchId,
                        "senderId" to chatMessage.senderId,
                        "senderName" to chatMessage.senderName,
                        "text" to chatMessage.text,
                        "timestamp" to chatMessage.timestamp,
                        "createdAt" to chatMessage.createdAt,
                        "isSafetyAlert" to chatMessage.isSafetyAlert,
                        "alertWarning" to chatMessage.alertWarning,
                        "isMine" to false // stored from sender perspective; queried per currentUserId
                    )

                    // Write message document
                    fs.collection(COLLECTION_CHATS)
                        .document(conversationId)
                        .collection(SUBCOLLECTION_MESSAGES)
                        .document(messageId)
                        .set(messageData)
                        .await()

                    // Update parent conversation doc
                    val conversationSummary = mapOf(
                        "conversationId" to conversationId,
                        "matchId" to matchId,
                        "lastMessage" to trimmedText,
                        "lastMessageTime" to now,
                        "lastSenderId" to senderId,
                        "lastSenderName" to senderName,
                        "participants" to listOf(senderId, matchId)
                    )
                    fs.collection(COLLECTION_CHATS)
                        .document(conversationId)
                        .set(conversationSummary, SetOptions.merge())
                        .await()

                    logD(TAG, "Message $messageId sent successfully to Firestore")
                } catch (e: Throwable) {
                    logW(TAG, "Failed to write to Firestore, message kept in fallback cache: ${e.message}")
                }
            }

            chatMessage
        }
    }

    /**
     * Seeds initial conversation history if empty.
     */
    fun seedInitialMessagesIfEmpty(
        conversationId: String,
        matchId: String,
        partnerName: String,
        initialMessages: List<ChatMessage>
    ) {
        val flow = getOrCreateFallbackFlow(conversationId)
        if (flow.value.isEmpty() && initialMessages.isNotEmpty()) {
            flow.value = initialMessages
        }
    }

    /**
     * Analyzes matrimonial safety triggers in messages (bank requests, OTP, financial demands).
     */
    private fun analyzeSafety(text: String): Pair<Boolean, String> {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("send money") || lower.contains("google pay") || lower.contains("phonepe") || lower.contains("upi") || lower.contains("bank account") -> {
                true to "Safety Advisory: Never transfer money, share bank details, or scan UPI QR codes before in-person family meetings."
            }
            lower.contains("otp") || lower.contains("password") || lower.contains("credit card") -> {
                true to "Security Alert: Prem Setu will never request OTPs or financial credentials over private chat."
            }
            lower.contains("investment") || lower.contains("crypto") || lower.contains("crypto wallet") -> {
                true to "Commercial Alert: Suspicious investment solicitations violate Prem Setu matrimonial community guidelines."
            }
            else -> false to ""
        }
    }
}
