package com.example.autocarservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autocarservice.model.ServiceRequest
import com.example.autocarservice.ui.components.ServiceRequestCard
import com.example.autocarservice.ui.viewmodels.AuthViewModel
import com.example.autocarservice.ui.viewmodels.ChatViewModel
import com.example.autocarservice.ui.viewmodels.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    navigateToCreateServiceRequest: () -> Unit,
    navigateToServiceRequestDetail: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val clientRequests by serviceViewModel.clientRequests.collectAsState()
    
    // Load client service requests
    LaunchedEffect(currentUser) {
        currentUser?.id?.let { clientId ->
            serviceViewModel.getClientServiceRequests(clientId)
        }
    }
    
    // Track unread messages for each service request
    val unreadMessageCounts = remember { mutableStateMapOf<String, Int>() }
    
    // Update unread message counts for each service request
    LaunchedEffect(clientRequests) {
        clientRequests.forEach { request ->
            currentUser?.id?.let { userId ->
                chatViewModel.getUnreadMessageCount(request.id, userId)
                chatViewModel.unreadCount.collect { count ->
                    unreadMessageCounts[request.id] = count
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "АвтоСервис",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    // Profile button
                    IconButton(onClick = navigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Профиль"
                        )
                    }
                    
                    // Logout button
                    IconButton(
                        onClick = {
                            authViewModel.signOut()
                            navigateToLogin()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToCreateServiceRequest,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать заявку"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Welcome message
            Text(
                text = "Добро пожаловать, ${currentUser?.name ?: "Клиент"}!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Service requests
            if (clientRequests.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "У вас пока нет заявок",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = navigateToCreateServiceRequest
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Создать заявку")
                        }
                    }
                }
            } else {
                // List of service requests
                Text(
                    text = "Ваши заявки",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Add padding for FAB
                ) {
                    items(clientRequests) { request ->
                        ServiceRequestCard(
                            serviceRequest = request,
                            unreadMessageCount = unreadMessageCounts[request.id] ?: 0,
                            onClick = { navigateToServiceRequestDetail(request.id) }
                        )
                    }
                }
            }
        }
    }
}
