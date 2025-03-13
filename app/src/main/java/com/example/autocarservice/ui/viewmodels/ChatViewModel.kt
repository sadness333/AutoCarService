package com.example.autocarservice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autocarservice.model.ChatMessage
import com.example.autocarservice.model.User
import com.example.autocarservice.model.UserRole
import com.example.autocarservice.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for chat operations
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    // Chat messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    // Unread message count
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount
    
    /**
     * Get chat messages for a service request
     */
    fun getMessagesForServiceRequest(serviceRequestId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesForServiceRequest(serviceRequestId)
                .collect { messages ->
                    _messages.value = messages
                }
        }
    }
    
    /**
     * Send a chat message
     */
    fun sendMessage(
        serviceRequestId: String,
        sender: User,
        content: String
    ) {
        if (content.isBlank()) return
        
        val message = ChatMessage(
            serviceRequestId = serviceRequestId,
            senderId = sender.id,
            senderName = sender.name,
            senderRole = sender.role,
            content = content
        )
        
        viewModelScope.launch {
            chatRepository.sendMessage(message)
        }
    }
    
    /**
     * Mark messages as read
     */
    fun markMessagesAsRead(serviceRequestId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(serviceRequestId, userId)
        }
    }
    
    /**
     * Get unread message count for a service request
     */
    fun getUnreadMessageCount(serviceRequestId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.getUnreadMessageCount(serviceRequestId, userId)
                .collect { count ->
                    _unreadCount.value = count
                }
        }
    }
}
