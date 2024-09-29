package dev.rm.hearmeapp.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.rm.hearmeapp.data.repository.UserRepository

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (userRepository.getCurrentUser() == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun register(username: String, email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        userRepository.registerUser(username, email, password) { user, error ->
            if (user != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(error?.message ?: "Something went wrong")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        userRepository.loginUser(email, password) { user, error ->
            if (user != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(error?.message ?: "Something went wrong")
            }
        }
    }

    fun logout() {
        userRepository.logoutUser()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUserInfo(): Pair<String?, String?> {
        val firebaseUser = userRepository.getCurrentUser()
        val username = firebaseUser?.displayName ?: "User"
        val email = firebaseUser?.email ?: "No Email"
        return Pair(username, email)
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object SavedToDB : AuthState()
    data class Error(val message: String) : AuthState()
}