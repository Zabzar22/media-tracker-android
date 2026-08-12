package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.RegisterResult
import edu.metrostate.ics342.mediatracker.data.UserRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository = DefaultUserRepository()
) : ViewModel() {

    sealed class RegisterUiState {
        data object Idle    : RegisterUiState()
        data object Loading : RegisterUiState()
        data object Success : RegisterUiState()
        data class Error(val msgResId: Int) : RegisterUiState()
    }

    // ── Form fields ───────────────────────────────────────────────────────

    private val _displayName     = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _email           = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _username        = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password        = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // ── UI state ──────────────────────────────────────────────────────────

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // ── Event handlers ────────────────────────────────────────────────────

    fun onDisplayNameChange(value: String)     { _displayName.value     = value }
    fun onEmailChange(value: String)           { _email.value           = value }
    fun onUsernameChange(value: String)        { _username.value        = value }
    fun onPasswordChange(value: String)        { _password.value        = value }
    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value }

    fun onRegisterClick() {
        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading

            if (_displayName.value.isBlank() || _email.value.isBlank() ||
                _username.value.isBlank() || _password.value.isBlank() ||
                _confirmPassword.value.isBlank()
            ) {
                _registerState.value = RegisterUiState.Error(R.string.error_empty_fields)
                return@launch
            }

            if (_password.value != _confirmPassword.value) {
                _registerState.value = RegisterUiState.Error(R.string.error_passwords_mismatch)
                return@launch
            }

            val result = userRepository.register(
                email       = _email.value,
                password    = _password.value,
                username    = _username.value,
                displayName = _displayName.value
            )

            _registerState.value = when (result) {
                RegisterResult.Success      -> RegisterUiState.Success
                RegisterResult.Conflict     -> RegisterUiState.Error(R.string.error_email_or_username_taken)
                RegisterResult.NetworkError -> RegisterUiState.Error(R.string.error_network)
                RegisterResult.UnknownError -> RegisterUiState.Error(R.string.error_generic)
            }
        }
    }

    fun resetRegisterState() { _registerState.value = RegisterUiState.Idle }
}
