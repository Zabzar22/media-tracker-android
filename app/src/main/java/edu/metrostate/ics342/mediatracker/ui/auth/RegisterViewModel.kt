package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    fun setDisplayName(newValue: String) {
        _displayName.value = newValue
    }

    fun onSignUpClicked() {
        viewModelScope.launch {
            userRepository.createAccount(
                displayName = displayName.value,
                username = "",
                email = "",
                password = ""
            )
        }
    }
}