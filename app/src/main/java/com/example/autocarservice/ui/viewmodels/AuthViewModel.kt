package com.example.autocarservice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autocarservice.model.User
import com.example.autocarservice.model.UserRole
import com.example.autocarservice.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Current user state
    val currentUser = authRepository.getCurrentUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Login state
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    // Register state
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState
    
    // Update profile state
    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrNull()!!)
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Register a new user
     */
    fun register(email: String, password: String, name: String, phone: String, role: UserRole) {
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            val result = authRepository.register(email, password, name, phone, role)
            
            _registerState.value = if (result.isSuccess) {
                RegisterState.Success(result.getOrNull()!!)
            } else {
                RegisterState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Update user profile
     */
    fun updateUserProfile(userId: String, name: String, phone: String) {
        _updateProfileState.value = UpdateProfileState.Loading
        
        viewModelScope.launch {
            val result = authRepository.updateUserProfile(User(userId, name, phone))
            
            _updateProfileState.value = if (result.isSuccess) {
                UpdateProfileState.Success(result.getOrNull()!!)
            } else {
                UpdateProfileState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
    
    /**
     * Reset login state
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    /**
     * Reset register state
     */
    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
    
    /**
     * Reset update profile state
     */
    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }
    
    /**
     * Check if the user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return authRepository.isUserAuthenticated()
    }
}

/**
 * Login state sealed class
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * Register state sealed class
 */
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

/**
 * Update profile state sealed class
 */
sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    data class Success(val user: User) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}
