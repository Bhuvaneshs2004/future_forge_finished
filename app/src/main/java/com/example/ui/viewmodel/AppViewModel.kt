package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.FutureForgeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: UserEntity) : AuthUiState
}

class AppViewModel(
    private val repository: FutureForgeRepository
) : ViewModel() {

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val authState: StateFlow<AuthUiState> = repository.activeUserIdFlow
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) {
                flowOf(AuthUiState.Unauthenticated)
            } else {
                repository.observeUser(uid).map { user ->
                    if (user == null || user.suspended) AuthUiState.Unauthenticated
                    else AuthUiState.Authenticated(user)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AuthUiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedItems: StateFlow<List<SavedItemEntity>> = authState
        .flatMapLatest { state ->
            if (state is AuthUiState.Authenticated) repository.observeSavedItems(state.user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = authState
        .flatMapLatest { state ->
            if (state is AuthUiState.Authenticated) repository.observeNotifications(state.user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val achievements: StateFlow<List<AchievementEntity>> = authState
        .flatMapLatest { state ->
            if (state is AuthUiState.Authenticated) repository.observeAchievements(state.user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val res = repository.login(email, password)
            _authLoading.value = false
            res.onFailure { _authError.value = it.message ?: "Sign in failed." }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        organizationOrHeadline: String,
        adminInviteCode: String
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val res = repository.register(name, email, password, role, organizationOrHeadline, adminInviteCode)
            _authLoading.value = false
            res.onFailure { _authError.value = it.message ?: "Registration failed." }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun updateProfile(updated: UserEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(updated)
            _statusBanner.value = "Profile & preferences saved."
        }
    }

    fun toggleSaved(userId: String, itemType: String, itemId: String, title: String, subtitle: String) {
        viewModelScope.launch {
            repository.toggleSaveItem(userId, itemType, itemId, title, subtitle)
        }
    }

    fun markNotificationsRead(userId: String) {
        viewModelScope.launch {
            repository.markAllNotificationsRead(userId)
        }
    }
}
