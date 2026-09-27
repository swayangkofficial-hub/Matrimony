package com.example

import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRepositoryTest {

    @Test
    fun testGetConversationId_deterministic() {
        val id1 = ChatRepository.getConversationId("user_101", "user_202")
        val id2 = ChatRepository.getConversationId("user_202", "user_101")
        assertEquals("user_101_user_202", id1)
        assertEquals(id1, id2)
    }

    @Test
    fun testSendMessage_success() = runBlocking {
        val repo = ChatRepository { null } // Test with fallback in-memory flow
        val conversationId = "test_conv_001"
        val matchId = "match_priya"
        val senderId = "user_me"
        val senderName = "Aarav"

        val result = repo.sendMessage(
            conversationId = conversationId,
            matchId = matchId,
            senderId = senderId,
            senderName = senderName,
            text = "Namaste! Great connecting with your family."
        )

        assertTrue(result.isSuccess)
        val msg = result.getOrThrow()
        assertEquals("Namaste! Great connecting with your family.", msg.text)
        assertEquals(senderName, msg.senderName)
        assertEquals(senderId, msg.senderId)
        assertFalse(msg.isSafetyAlert)

        val messages = repo.getMessages(conversationId, senderId).first()
        assertEquals(1, messages.size)
        assertEquals(msg.id, messages[0].id)
        assertTrue(messages[0].isMine)
    }

    @Test
    fun testSendMessage_safetyTrigger() = runBlocking {
        val repo = ChatRepository { null }
        val conversationId = "test_conv_safety"
        val matchId = "match_test"

        val result = repo.sendMessage(
            conversationId = conversationId,
            matchId = matchId,
            senderId = "user_stranger",
            senderName = "Stranger",
            text = "Please send money to my UPI right away."
        )

        assertTrue(result.isSuccess)
        val msg = result.getOrThrow()
        assertTrue(msg.isSafetyAlert)
        assertTrue(msg.alertWarning.isNotBlank())
    }

    @Test
    fun testBlockUser_restrictsCommunication() = runBlocking {
        val repo = ChatRepository { null }
        val currentUserId = "user_me"
        val targetUserId = "user_inappropriate"
        val conversationId = ChatRepository.getConversationId(currentUserId, targetUserId)

        assertFalse(repo.isUserBlocked(currentUserId, targetUserId))

        // Block user
        val blockResult = repo.blockUser(currentUserId, targetUserId, "Inappropriate messages")
        assertTrue(blockResult.isSuccess)
        assertTrue(repo.isUserBlocked(currentUserId, targetUserId))

        // Sending a message should now fail
        val sendResult = repo.sendMessage(
            conversationId = conversationId,
            matchId = targetUserId,
            senderId = currentUserId,
            senderName = "Aarav",
            text = "Hello"
        )
        assertTrue(sendResult.isFailure)
        assertTrue(sendResult.exceptionOrNull()?.message?.contains("blocked", ignoreCase = true) == true)

        // Blocked list flow should include target user
        val blockedList = repo.getBlockedUsers(currentUserId).first()
        assertTrue(blockedList.contains(targetUserId))
    }

    @Test
    fun testUnblockUser_restoresCommunication() = runBlocking {
        val repo = ChatRepository { null }
        val currentUserId = "user_me"
        val targetUserId = "user_spammer"
        val conversationId = ChatRepository.getConversationId(currentUserId, targetUserId)

        repo.blockUser(currentUserId, targetUserId)
        assertTrue(repo.isUserBlocked(currentUserId, targetUserId))

        // Unblock user
        val unblockResult = repo.unblockUser(currentUserId, targetUserId)
        assertTrue(unblockResult.isSuccess)
        assertFalse(repo.isUserBlocked(currentUserId, targetUserId))

        // Sending message should now succeed
        val sendResult = repo.sendMessage(
            conversationId = conversationId,
            matchId = targetUserId,
            senderId = currentUserId,
            senderName = "Aarav",
            text = "Namaste, issue resolved."
        )
        assertTrue(sendResult.isSuccess)

        val blockedList = repo.getBlockedUsers(currentUserId).first()
        assertFalse(blockedList.contains(targetUserId))
    }
}
