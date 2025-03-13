package com.example.autocarservice.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autocarservice.model.UserRole
import com.example.autocarservice.ui.components.AutoServiceCard
import com.example.autocarservice.ui.viewmodels.AuthViewModel
import com.example.autocarservice.ui.viewmodels.UpdateProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val updateProfileState by authViewModel.updateProfileState.collectAsState()
    
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    
    // Initialize fields when user data is loaded
    LaunchedEffect(currentUser) {
        name = currentUser?.name ?: ""
        phone = currentUser?.phone ?: ""
    }
    
    // Handle update profile success
    LaunchedEffect(updateProfileState) {
        if (updateProfileState is UpdateProfileState.Success) {
            isEditing = false
            authViewModel.resetUpdateProfileState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile avatar
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User role badge
            Surface(
                color = when (currentUser?.role) {
                    UserRole.CLIENT -> MaterialTheme.colorScheme.tertiary
                    UserRole.EMPLOYEE -> MaterialTheme.colorScheme.secondary
                    null -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (currentUser?.role) {
                        UserRole.CLIENT -> "Клиент"
                        UserRole.EMPLOYEE -> "Сотрудник"
                        null -> "Неизвестно"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile info card
            AutoServiceCard(
                title = "Информация о пользователе",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Email (non-editable)
                OutlinedTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = { },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Имя"
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    readOnly = !isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Телефон"
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    readOnly = !isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isEditing) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                name = currentUser?.name ?: ""
                                phone = currentUser?.phone ?: ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Отмена")
                        }
                        
                        // Save button
                        Button(
                            onClick = {
                                currentUser?.id?.let { userId ->
                                    authViewModel.updateUserProfile(
                                        userId = userId,
                                        name = name,
                                        phone = phone
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotBlank() && phone.isNotBlank() && 
                                    updateProfileState !is UpdateProfileState.Loading
                        ) {
                            if (updateProfileState is UpdateProfileState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Сохранить")
                            }
                        }
                    }
                    
                    // Error message
                    if (updateProfileState is UpdateProfileState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (updateProfileState as UpdateProfileState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account actions card
            AutoServiceCard(
                title = "Действия с аккаунтом",
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { authViewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}
