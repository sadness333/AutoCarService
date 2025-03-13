package com.example.autocarservice.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autocarservice.model.ServiceRequest
import com.example.autocarservice.model.ServiceStatus
import com.example.autocarservice.model.UserRole
import com.example.autocarservice.ui.components.AutoServiceCard
import com.example.autocarservice.ui.components.ChatInput
import com.example.autocarservice.ui.components.MessageBubble
import com.example.autocarservice.ui.components.ServiceProgressBar
import com.example.autocarservice.ui.components.ServiceStatusIndicator
import com.example.autocarservice.ui.viewmodels.AuthViewModel
import com.example.autocarservice.ui.viewmodels.ChatViewModel
import com.example.autocarservice.ui.viewmodels.ServiceViewModel
import com.example.autocarservice.utils.formatDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestDetailScreen(
    serviceRequestId: String,
    navigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val serviceRequest by serviceViewModel.currentServiceRequest.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Load service request details
    LaunchedEffect(serviceRequestId) {
        serviceViewModel.getServiceRequestById(serviceRequestId)
        chatViewModel.getMessagesForServiceRequest(serviceRequestId)
    }
    
    // Mark messages as read
    LaunchedEffect(messages) {
        currentUser?.id?.let { userId ->
            chatViewModel.markMessagesAsRead(serviceRequestId, userId)
        }
    }
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали заявки") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (serviceRequest == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Service request details
                    item {
                        ServiceRequestDetailsCard(
                            serviceRequest = serviceRequest!!,
                            currentUserRole = currentUser?.role ?: UserRole.CLIENT,
                            onStatusChange = { newStatus ->
                                serviceViewModel.updateServiceRequestStatus(
                                    serviceRequestId = serviceRequestId,
                                    newStatus = newStatus
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Chat messages
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Чат с сервисом",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                if (messages.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Нет сообщений. Начните общение!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Display messages
                    if (messages.isNotEmpty()) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isFromCurrentUser = message.senderId == currentUser?.id,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        // Add some space at the bottom
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Chat input
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    ChatInput(
                        onSendMessage = { messageText ->
                            currentUser?.let { user ->
                                chatViewModel.sendMessage(
                                    serviceRequestId = serviceRequestId,
                                    sender = user,
                                    content = messageText
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceRequestDetailsCard(
    serviceRequest: ServiceRequest,
    currentUserRole: UserRole,
    onStatusChange: (ServiceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = serviceRequest.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                ServiceStatusIndicator(status = serviceRequest.status)
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Car details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = serviceRequest.carModel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Год: ${serviceRequest.carYear}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Описание проблемы:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = serviceRequest.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Создано:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = formatDate(serviceRequest.createdAt),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (serviceRequest.updatedAt != serviceRequest.createdAt) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Обновлено:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = formatDate(serviceRequest.updatedAt),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            ServiceProgressBar(status = serviceRequest.status)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status change buttons (only for employees)
            if (currentUserRole == UserRole.EMPLOYEE) {
                Text(
                    text = "Изменить статус:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (serviceRequest.status) {
                        ServiceStatus.PENDING -> {
                            Button(
                                onClick = { onStatusChange(ServiceStatus.ACCEPTED) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Принять заявку")
                            }
                            
                            Button(
                                onClick = { onStatusChange(ServiceStatus.CANCELLED) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Отклонить")
                            }
                        }
                        ServiceStatus.ACCEPTED -> {
                            Button(
                                onClick = { onStatusChange(ServiceStatus.IN_PROGRESS) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Начать работу")
                            }
                        }
                        ServiceStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { onStatusChange(ServiceStatus.PAUSED) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Приостановить")
                            }
                            
                            Button(
                                onClick = { onStatusChange(ServiceStatus.COMPLETED) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Text("Завершить")
                            }
                        }
                        ServiceStatus.PAUSED -> {
                            Button(
                                onClick = { onStatusChange(ServiceStatus.IN_PROGRESS) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Возобновить")
                            }
                        }
                        ServiceStatus.COMPLETED -> {
                            // No actions for completed requests
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Работа завершена",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        ServiceStatus.CANCELLED -> {
                            // No actions for cancelled requests
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Заявка отменена",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            } else {
                // Client view of status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (serviceRequest.status) {
                                ServiceStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                                ServiceStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer
                                ServiceStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                                ServiceStatus.PAUSED -> MaterialTheme.colorScheme.secondaryContainer
                                ServiceStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                                ServiceStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (serviceRequest.status) {
                            ServiceStatus.PENDING -> "Ожидает рассмотрения"
                            ServiceStatus.ACCEPTED -> "Заявка принята"
                            ServiceStatus.IN_PROGRESS -> "В работе"
                            ServiceStatus.PAUSED -> "Работа приостановлена"
                            ServiceStatus.COMPLETED -> "Работа завершена"
                            ServiceStatus.CANCELLED -> "Заявка отменена"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (serviceRequest.status) {
                            ServiceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            ServiceStatus.ACCEPTED -> MaterialTheme.colorScheme.onPrimaryContainer
                            ServiceStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                            ServiceStatus.PAUSED -> MaterialTheme.colorScheme.onSecondaryContainer
                            ServiceStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
                            ServiceStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    }
}
