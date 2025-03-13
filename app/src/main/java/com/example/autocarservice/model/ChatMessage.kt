package com.example.autocarservice.model

import com.google.firebase.firestore.DocumentId

/**
 * Chat message model representing messages exchanged between clients and employees
 */
data class ChatMessage(
    @DocumentId val id: String = "",
    val serviceRequestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: UserRole = UserRole.CLIENT,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
