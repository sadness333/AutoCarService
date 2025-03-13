package com.example.autocarservice.repository

import com.example.autocarservice.model.ServiceNote
import com.example.autocarservice.model.ServiceRequest
import com.example.autocarservice.model.ServiceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for service request operations
 */
interface ServiceRepository {
    /**
     * Create a new service request
     */
    suspend fun createServiceRequest(serviceRequest: ServiceRequest): Result<ServiceRequest>
    
    /**
     * Get a service request by ID
     */
    suspend fun getServiceRequest(id: String): Result<ServiceRequest>
    
    /**
     * Get a service request by ID as a Flow
     */
    fun getServiceRequestFlow(id: String): Flow<ServiceRequest?>
    
    /**
     * Get all service requests for a client
     */
    fun getClientServiceRequests(clientId: String): Flow<List<ServiceRequest>>
    
    /**
     * Get all available service requests (not yet accepted by any employee)
     */
    fun getAvailableServiceRequests(): Flow<List<ServiceRequest>>
    
    /**
     * Get all service requests assigned to an employee
     */
    fun getEmployeeServiceRequests(employeeId: String): Flow<List<ServiceRequest>>
    
    /**
     * Update a service request
     */
    suspend fun updateServiceRequest(serviceRequest: ServiceRequest): Result<ServiceRequest>
    
    /**
     * Accept a service request (employee)
     */
    suspend fun acceptServiceRequest(requestId: String, employeeId: String): Result<ServiceRequest>
    
    /**
     * Update service request status
     */
    suspend fun updateServiceStatus(
        requestId: String, 
        status: ServiceStatus, 
        progress: Int
    ): Result<ServiceRequest>
    
    /**
     * Add a note to a service request
     */
    suspend fun addServiceNote(requestId: String, note: ServiceNote): Result<ServiceRequest>
    fun getServiceRequestById(id: String): Flow<ServiceRequest?>
    fun getAllServiceRequests(): Flow<List<ServiceRequest>>
    suspend fun updateServiceRequestStatus(serviceRequestId: String, newStatus: ServiceStatus): Result<ServiceRequest>
}
