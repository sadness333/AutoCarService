package com.example.autocarservice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autocarservice.model.ServiceNote
import com.example.autocarservice.model.ServiceRequest
import com.example.autocarservice.model.ServiceStatus
import com.example.autocarservice.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for service request operations
 */
@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {
    
    // Create service request state
    private val _createRequestState = MutableStateFlow<CreateRequestState>(CreateRequestState.Idle)
    val createRequestState: StateFlow<CreateRequestState> = _createRequestState
    
    // Current service request
    private val _currentServiceRequest = MutableStateFlow<ServiceRequest?>(null)
    val currentServiceRequest: StateFlow<ServiceRequest?> = _currentServiceRequest
    
    // Client service requests
    private val _clientRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val clientRequests: StateFlow<List<ServiceRequest>> = _clientRequests
    
    // All service requests (for employees)
    private val _allServiceRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val allServiceRequests: StateFlow<List<ServiceRequest>> = _allServiceRequests
    
    /**
     * Create a new service request
     */
    fun createServiceRequest(
        clientId: String,
        title: String,
        description: String,
        carModel: String,
        carYear: Int
    ) {
        _createRequestState.value = CreateRequestState.Loading
        
        val serviceRequest = ServiceRequest(
            clientId = clientId,
            title = title,
            description = description,
            carModel = carModel,
            carYear = carYear
        )
        
        viewModelScope.launch {
            val result = serviceRepository.createServiceRequest(serviceRequest)
            
            _createRequestState.value = if (result.isSuccess) {
                CreateRequestState.Success(result.getOrNull()!!)
            } else {
                CreateRequestState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Get a service request by ID
     */
    fun getServiceRequestById(id: String) {
        viewModelScope.launch {
            serviceRepository.getServiceRequestById(id)
                .collect { serviceRequest ->
                    _currentServiceRequest.value = serviceRequest
                }
        }
    }
    
    /**
     * Get all service requests for a client
     */
    fun getClientServiceRequests(clientId: String) {
        viewModelScope.launch {
            serviceRepository.getClientServiceRequests(clientId)
                .collect { requests ->
                    _clientRequests.value = requests
                }
        }
    }
    
    /**
     * Get all service requests (for employees)
     */
    fun getAllServiceRequests() {
        viewModelScope.launch {
            serviceRepository.getAllServiceRequests()
                .collect { requests ->
                    _allServiceRequests.value = requests
                }
        }
    }
    
    /**
     * Update service request status
     */
    fun updateServiceRequestStatus(serviceRequestId: String, newStatus: ServiceStatus) {
        viewModelScope.launch {
            serviceRepository.updateServiceRequestStatus(serviceRequestId, newStatus)
        }
    }
    
    /**
     * Add a note to a service request
     */
    fun addServiceNote(requestId: String, note: ServiceNote) {
        viewModelScope.launch {
            serviceRepository.addServiceNote(requestId, note)
        }
    }
    
    /**
     * Reset create request state
     */
    fun resetCreateRequestState() {
        _createRequestState.value = CreateRequestState.Idle
    }
}

/**
 * Create request state sealed class
 */
sealed class CreateRequestState {
    object Idle : CreateRequestState()
    object Loading : CreateRequestState()
    data class Success(val serviceRequest: ServiceRequest) : CreateRequestState()
    data class Error(val message: String) : CreateRequestState()
}
