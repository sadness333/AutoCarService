package com.example.autocarservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autocarservice.ui.components.AutoServiceCard
import com.example.autocarservice.ui.viewmodels.AuthViewModel
import com.example.autocarservice.ui.viewmodels.CreateRequestState
import com.example.autocarservice.ui.viewmodels.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceRequestScreen(
    navigateBack: () -> Unit,
    navigateToServiceRequestDetail: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val createRequestState by serviceViewModel.createRequestState.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    
    // Handle create request success
    LaunchedEffect(createRequestState) {
        if (createRequestState is CreateRequestState.Success) {
            val serviceRequest = (createRequestState as CreateRequestState.Success).serviceRequest
            navigateToServiceRequestDetail(serviceRequest.id)
            serviceViewModel.resetCreateRequestState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание заявки") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoServiceCard(
                title = "Новая заявка на обслуживание",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название заявки") },
                    placeholder = { Text("Например: Замена масла") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание проблемы") },
                    placeholder = { Text("Опишите подробно проблему с автомобилем") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Car model field
                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("Модель автомобиля") },
                    placeholder = { Text("Например: Toyota Camry") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Модель автомобиля"
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Car year field
                OutlinedTextField(
                    value = carYear,
                    onValueChange = { 
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            carYear = it
                        }
                    },
                    label = { Text("Год выпуска") },
                    placeholder = { Text("Например: 2020") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit button
                Button(
                    onClick = {
                        if (isFormValid(title, description, carModel, carYear)) {
                            currentUser?.id?.let { clientId ->
                                serviceViewModel.createServiceRequest(
                                    clientId = clientId,
                                    title = title,
                                    description = description,
                                    carModel = carModel,
                                    carYear = carYear.toInt()
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid(title, description, carModel, carYear) && 
                            createRequestState !is CreateRequestState.Loading &&
                            currentUser != null
                ) {
                    if (createRequestState is CreateRequestState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Отправить заявку")
                    }
                }
                
                // Error message
                if (createRequestState is CreateRequestState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (createRequestState as CreateRequestState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun isFormValid(
    title: String,
    description: String,
    carModel: String,
    carYear: String
): Boolean {
    return title.isNotBlank() &&
            description.isNotBlank() &&
            carModel.isNotBlank() &&
            carYear.isNotBlank() &&
            carYear.toIntOrNull() != null &&
            carYear.toInt() in 1900..2100 // Reasonable year range
}
