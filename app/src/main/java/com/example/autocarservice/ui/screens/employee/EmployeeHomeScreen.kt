package com.example.autocarservice.ui.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.example.autocarservice.model.ServiceStatus
import com.example.autocarservice.ui.components.ServiceRequestCard
import com.example.autocarservice.ui.viewmodels.AuthViewModel
import com.example.autocarservice.ui.viewmodels.ChatViewModel
import com.example.autocarservice.ui.viewmodels.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    navigateToServiceRequestDetail: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val allServiceRequests by serviceViewModel.allServiceRequests.collectAsState()
    
    // Selected filter tab
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Все", "Новые", "В работе", "Завершенные", "Отмененные")
    
    // Load all service requests
    LaunchedEffect(Unit) {
        serviceViewModel.getAllServiceRequests()
    }
    
    // Track unread messages for each service request
    val unreadMessageCounts = remember { mutableStateMapOf<String, Int>() }
    
    // Update unread message counts for each service request
    LaunchedEffect(allServiceRequests) {
        allServiceRequests.forEach { request ->
            currentUser?.id?.let { userId ->
                chatViewModel.getUnreadMessageCount(request.id, userId)
                chatViewModel.unreadCount.collect { count ->
                    unreadMessageCounts[request.id] = count
                }
            }
        }
    }
    
    // Filter service requests based on selected tab
    val filteredRequests = when (selectedTab) {
        0 -> allServiceRequests // All
        1 -> allServiceRequests.filter { it.status == ServiceStatus.PENDING }
        2 -> allServiceRequests.filter { it.status == ServiceStatus.IN_PROGRESS }
        3 -> allServiceRequests.filter { it.status == ServiceStatus.COMPLETED }
        4 -> allServiceRequests.filter { it.status == ServiceStatus.CANCELLED }
        else -> allServiceRequests
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Welcome message
            Text(
                text = "Добро пожаловать, ${currentUser?.name ?: "Сотрудник"}!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            // Filter tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }
            
            // Service requests
            if (filteredRequests.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет заявок в этой категории",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // List of service requests
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRequests) { request ->
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
