package com.example.autocarservice.model

import com.google.firebase.firestore.DocumentId

/**
 * Service request model representing a car service request from a client
 */
data class ServiceRequest(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val employeeId: String? = null,
    val title: String = "",
    val description: String = "",
    val carModel: String = "",
    val carYear: Int = 0,
    val status: ServiceStatus = ServiceStatus.PENDING,
    val progress: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val notes: List<ServiceNote> = emptyList()
)

/**
 * Service note model representing notes added to a service request
 */
data class ServiceNote(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: UserRole = UserRole.CLIENT,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing the status of a service request
 */
enum class ServiceStatus {
    PENDING, // Initial state when client creates a request
    ACCEPTED, // Employee has accepted the request
    IN_PROGRESS, // Work has started
    PAUSED, // Work is temporarily paused
    COMPLETED, // Work is completed
    CANCELLED // Request was cancelled
}
