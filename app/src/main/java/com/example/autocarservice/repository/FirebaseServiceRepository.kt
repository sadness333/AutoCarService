package com.example.autocarservice.repository

import com.example.autocarservice.model.ServiceNote
import com.example.autocarservice.model.ServiceRequest
import com.example.autocarservice.model.ServiceStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseServiceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ServiceRepository {


    private val serviceRequestsCollection = firestore.collection("service_requests")

    override suspend fun createServiceRequest(serviceRequest: ServiceRequest): Result<ServiceRequest> {
        return try {
            val docRef = serviceRequestsCollection.document()
            val request = serviceRequest.copy(id = docRef.id)
            docRef.set(request).await()
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceRequest(id: String): Result<ServiceRequest> {
        return try {
            val docSnapshot = serviceRequestsCollection.document(id).get().await()
            val serviceRequest = docSnapshot.toObject(ServiceRequest::class.java)
                ?: return Result.failure(IllegalStateException("Service request not found"))
            Result.success(serviceRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServiceRequestFlow(id: String): Flow<ServiceRequest?> = callbackFlow {
        val listener = serviceRequestsCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val serviceRequest = snapshot?.toObject(ServiceRequest::class.java)
                trySend(serviceRequest)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getClientServiceRequests(clientId: String): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = serviceRequestsCollection
            .whereEqualTo("clientId", clientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val serviceRequests = snapshot?.documents?.mapNotNull {
                    it.toObject(ServiceRequest::class.java)
                } ?: emptyList()
                
                trySend(serviceRequests)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getAvailableServiceRequests(): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = serviceRequestsCollection
            .whereEqualTo("status", ServiceStatus.PENDING)
            .whereEqualTo("employeeId", null)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val serviceRequests = snapshot?.documents?.mapNotNull {
                    it.toObject(ServiceRequest::class.java)
                } ?: emptyList()
                
                trySend(serviceRequests)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getEmployeeServiceRequests(employeeId: String): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = serviceRequestsCollection
            .whereEqualTo("employeeId", employeeId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val serviceRequests = snapshot?.documents?.mapNotNull {
                    it.toObject(ServiceRequest::class.java)
                } ?: emptyList()
                
                trySend(serviceRequests)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun updateServiceRequest(serviceRequest: ServiceRequest): Result<ServiceRequest> {
        return try {
            val updatedRequest = serviceRequest.copy(updatedAt = System.currentTimeMillis())
            serviceRequestsCollection.document(serviceRequest.id).set(updatedRequest).await()
            Result.success(updatedRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptServiceRequest(requestId: String, employeeId: String): Result<ServiceRequest> {
        return try {
            val docSnapshot = serviceRequestsCollection.document(requestId).get().await()
            val serviceRequest = docSnapshot.toObject(ServiceRequest::class.java)
                ?: return Result.failure(IllegalStateException("Service request not found"))
            
            if (serviceRequest.employeeId != null) {
                return Result.failure(IllegalStateException("Service request already accepted"))
            }
            
            val updatedRequest = serviceRequest.copy(
                employeeId = employeeId,
                status = ServiceStatus.ACCEPTED,
                updatedAt = System.currentTimeMillis()
            )
            
            serviceRequestsCollection.document(requestId).set(updatedRequest).await()
            Result.success(updatedRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateServiceStatus(
        requestId: String,
        status: ServiceStatus,
        progress: Int
    ): Result<ServiceRequest> {
        return try {
            val docSnapshot = serviceRequestsCollection.document(requestId).get().await()
            val serviceRequest = docSnapshot.toObject(ServiceRequest::class.java)
                ?: return Result.failure(IllegalStateException("Service request not found"))
            
            val updatedRequest = serviceRequest.copy(
                status = status,
                progress = progress,
                updatedAt = System.currentTimeMillis(),
                completedAt = if (status == ServiceStatus.COMPLETED) System.currentTimeMillis() else serviceRequest.completedAt
            )
            
            serviceRequestsCollection.document(requestId).set(updatedRequest).await()
            Result.success(updatedRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addServiceNote(requestId: String, note: ServiceNote): Result<ServiceRequest> {
        return try {
            val docSnapshot = serviceRequestsCollection.document(requestId).get().await()
            val serviceRequest = docSnapshot.toObject(ServiceRequest::class.java)
                ?: return Result.failure(IllegalStateException("Service request not found"))
            
            val noteWithId = note.copy(id = java.util.UUID.randomUUID().toString())
            val updatedNotes = serviceRequest.notes + noteWithId
            
            val updatedRequest = serviceRequest.copy(
                notes = updatedNotes,
                updatedAt = System.currentTimeMillis()
            )
            
            serviceRequestsCollection.document(requestId).set(updatedRequest).await()
            Result.success(updatedRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServiceRequestById(id: String): Flow<ServiceRequest?> {
        return getServiceRequestFlow(id)
    }


    override fun getAllServiceRequests(): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = serviceRequestsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)  // You can adjust the ordering as per your requirement
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val serviceRequests = snapshot?.documents?.mapNotNull {
                    it.toObject(ServiceRequest::class.java)
                } ?: emptyList()

                trySend(serviceRequests)
            }

        awaitClose { listener.remove() }
    }


    override suspend fun updateServiceRequestStatus(
        serviceRequestId: String,
        newStatus: ServiceStatus
    ): Result<ServiceRequest> {
        try {
            val docSnapshot = serviceRequestsCollection.document(serviceRequestId).get().await()
            val serviceRequest = docSnapshot.toObject(ServiceRequest::class.java)
                ?: throw IllegalStateException("Service request not found")
            
            // Calculate progress based on status
            val progress = when (newStatus) {
                ServiceStatus.PENDING -> 0
                ServiceStatus.ACCEPTED -> 20
                ServiceStatus.IN_PROGRESS -> 50
                ServiceStatus.PAUSED -> 50
                ServiceStatus.COMPLETED -> 100
                ServiceStatus.CANCELLED -> 0
            }
            
            val updatedRequest = serviceRequest.copy(
                status = newStatus,
                progress = progress,
                updatedAt = System.currentTimeMillis(),
                completedAt = if (newStatus == ServiceStatus.COMPLETED) System.currentTimeMillis() else serviceRequest.completedAt
            )
            
            serviceRequestsCollection.document(serviceRequestId).set(updatedRequest).await()
            return Result.success(updatedRequest)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
