package com.example.autocarservice.repository

import com.example.autocarservice.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat operations
 */
interface ChatRepository {
    /**
     * Send a chat message
     */
    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage>
    
    /**
     * Get chat messages for a service request
     */
    fun getMessagesForServiceRequest(serviceRequestId: String): Flow<List<ChatMessage>>
    
    /**
     * Mark messages as read
     */
    suspend fun markMessagesAsRead(serviceRequestId: String, userId: String): Result<Unit>
    
    /**
     * Get unread message count for a service request
     */
    fun getUnreadMessageCount(serviceRequestId: String, userId: String): Flow<Int>
}
