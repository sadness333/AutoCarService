package com.example.autocarservice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.autocarservice.model.ChatMessage
import com.example.autocarservice.model.UserRole
import com.example.autocarservice.utils.formatTime
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Message input
        ChatInput(
            onSendMessage = { messageText ->
                onSendMessage(messageText)
                coroutineScope.launch {
                    // Scroll to bottom after sending a message
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size)
                    }
                }
            }
        )
    }
}

/**
 * A component for displaying a chat message bubble.
 * This is used in the ServiceRequestDetailScreen to display chat messages.
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bubbleShape = if (isFromCurrentUser) {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Show sender name for messages not from current user
        if (!isFromCurrentUser) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        
        // Message bubble
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = bubbleShape,
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Message content
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Timestamp
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text field
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("Введите сообщение...") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                        focusManager.clearFocus()
                    }
                }
            ),
            maxLines = 4
        )
        
        // Send button
        IconButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
