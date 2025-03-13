package com.example.autocarservice.model

import com.google.firebase.firestore.DocumentId

/**
 * User model representing both clients and employees in the system
 */
data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CLIENT,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    CLIENT, EMPLOYEE
}
