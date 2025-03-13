package com.example.autocarservice.repository

import com.example.autocarservice.model.User
import com.example.autocarservice.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Register a new user with email and password
     */
    suspend fun register(email: String, password: String, name: String, phone: String, role: UserRole): Result<User>
    
    /**
     * Sign out the current user
     */
    suspend fun signOut()
    
    /**
     * Get the current authenticated user
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Get the current authenticated user as a Flow
     */
    fun getCurrentUserFlow(): Flow<User?>
    
    /**
     * Check if the user is authenticated
     */
    fun isUserAuthenticated(): Boolean
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(user: User): Result<User>
}
