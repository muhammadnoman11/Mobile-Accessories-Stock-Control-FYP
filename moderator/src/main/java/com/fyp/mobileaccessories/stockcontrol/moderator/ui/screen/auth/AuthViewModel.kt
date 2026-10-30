package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.data.repository.UserRepository
import com.fyp.mobileaccessories.stockcontrol.util.FcmTokenHelper
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import com.fyp.mobileaccessories.stockcontrol.util.SessionData
import com.fyp.mobileaccessories.stockcontrol.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// FILE: presentation/auth/AuthViewModel.kt
// Handles login, registration (Moderator), and session management with FCM tokens.
// ============================================================

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "Male",
    val domain: String = "",
    val address: String = "",
    val contactNo: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val session: StateFlow<SessionData?> = sessionManager.sessionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    fun onEmailChange(v: String) = _loginState.update { it.copy(email = v.trim(), error = null) }
    fun onPasswordChange(v: String) = _loginState.update { it.copy(password = v, error = null) }

    fun onRegFirstName(v: String) = _registerState.update { it.copy(firstName = v, error = null) }
    fun onRegLastName(v: String) = _registerState.update { it.copy(lastName = v, error = null) }
    fun onRegGender(v: String) = _registerState.update { it.copy(gender = v, error = null) }
    fun onRegDomain(v: String) = _registerState.update { it.copy(domain = v, error = null) }
    fun onRegAddress(v: String) = _registerState.update { it.copy(address = v, error = null) }
    fun onRegContactNo(v: String) = _registerState.update { it.copy(contactNo = v, error = null) }
    fun onRegEmail(v: String) = _registerState.update { it.copy(email = v.trim(), error = null) }
    fun onRegPassword(v: String) = _registerState.update { it.copy(password = v, error = null) }
    fun onRegConfirmPassword(v: String) =
        _registerState.update { it.copy(confirmPassword = v, error = null) }

    fun login(onSuccess: (role: String) -> Unit) {
        val s = _loginState.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _loginState.update { it.copy(error = "Email and password are required") }
            return
        }
        _loginState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = userRepository.login(s.email, s.password)
                if (user != null) {
                    if (user.role == "MODERATOR") {
                        sessionManager.saveSession(
                            userId = user.id,
                            role = user.role,
                            name = "${user.firstName} ${user.lastName}",
                            email = user.email
                        )
                        persistUserFcmToken(user.id)

                        _loginState.update { it.copy(isLoading = false) }
                        onSuccess(user.role)
                    } else {
                        _loginState.update {
                            it.copy(
                                isLoading = false,
                                error = "Access Denied: Moderator account required."
                            )
                        }
                    }
                } else {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid email or password"
                        )
                    }
                }
            } catch (e: Exception) {
                val localUser = userRepository.loginLocally(s.email, PasswordHasher.hash(s.password))
                if (localUser != null && localUser.role == "MODERATOR") {
                    sessionManager.saveSession(
                        userId = localUser.id,
                        role = localUser.role,
                        name = "${localUser.firstName} ${localUser.lastName}",
                        email = localUser.email
                    )
                    persistUserFcmToken(localUser.id)

                    _loginState.update { it.copy(isLoading = false) }
                    onSuccess(localUser.role)
                } else {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Login failed. Check your internet connection."
                        )
                    }
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val s = _registerState.value
        val error = when {
            s.firstName.isBlank() -> "First name is required"
            s.lastName.isBlank() -> "Last name is required"
            s.domain.isBlank() -> "Product domain is required"
            s.address.isBlank() -> "Address is required"
            s.contactNo.isBlank() -> "Contact number is required"
            s.email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
                -> "Enter a valid email address"
            s.password.length < 6 -> "Password must be at least 6 characters"
            s.password != s.confirmPassword -> "Passwords do not match"
            else -> null
        }
        if (error != null) {
            _registerState.update { it.copy(error = error) }
            return
        }

        _registerState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = userRepository.register(
                UserEntity(
                    firstName = s.firstName.trim(),
                    lastName = s.lastName.trim(),
                    gender = s.gender,
                    domain = s.domain.trim(),
                    address = s.address.trim(),
                    contactNo = s.contactNo.trim(),
                    email = s.email,
                    passwordHash = PasswordHasher.hash(s.password),
                    role = "MODERATOR",
                    dateOfRegistration = System.currentTimeMillis()
                ),
                plainPassword = s.password
            )
            if (result.isSuccess) {
                val userId = result.getOrNull()?.toInt() ?: 0
                val newUser = userRepository.getUserById(userId)
                if (newUser != null) {
                    persistUserFcmToken(newUser.id)
                }

                _registerState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } else {
                _registerState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            }
        }
    }

    private suspend fun persistUserFcmToken(userId: Int) {
        val token = FcmTokenHelper.fetchCurrentToken()
        userRepository.updateFcmToken(userId, token)
    }

    suspend fun checkUserExists(userId: Int): UserEntity? = userRepository.getUserById(userId)

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onLogout()
        }
    }
}
