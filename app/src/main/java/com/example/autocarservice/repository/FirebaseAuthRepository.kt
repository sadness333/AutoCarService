package com.example.autocarservice.repository

import com.example.autocarservice.model.User
import com.example.autocarservice.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(IllegalStateException("User ID is null"))
            
            val userDoc = usersCollection.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(IllegalStateException("User data not found"))
            
            Result.success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(IllegalArgumentException("User not found"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException("Invalid credentials"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String, 
        password: String, 
        name: String, 
        phone: String, 
        role: UserRole
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: return Result.failure(IllegalStateException("User ID is null"))
            
            val user = User(
                id = userId,
                email = email,
                name = name,
                phone = phone,
                role = role
            )
            
            usersCollection.document(userId).set(user).await()
            
            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("Email already in use"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        return try {
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null) {
                trySend(null)
            } else {
                // We need to get the user data from Firestore
                // This is done in a coroutine, but we can't use suspend functions in a callback
                // So we launch a coroutine to get the user data
                firestore.collection("users").document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            }
        }
        
        auth.addAuthStateListener(listener)
        
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
