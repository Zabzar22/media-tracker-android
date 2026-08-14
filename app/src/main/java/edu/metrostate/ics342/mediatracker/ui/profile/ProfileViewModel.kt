package edu.metrostate.ics342.mediatracker.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Favorite
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import edu.metrostate.ics342.mediatracker.data.network.DefaultFavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val favoriteRepository = DefaultFavoriteRepository()

    // the profile itself is still mock data, but the favorites list underneath it is
    // real ; it's the only place in the app you can see what you've saved.
    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _libraryPreview = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraryPreview: StateFlow<List<LibraryItem>> = _libraryPreview.asStateFlow()

    private val _editDisplayName = MutableStateFlow("")
    val editDisplayName: StateFlow<String> = _editDisplayName.asStateFlow()

    private val _editUsername = MutableStateFlow("")
    val editUsername: StateFlow<String> = _editUsername.asStateFlow()

    private val _editBio = MutableStateFlow("")
    val editBio: StateFlow<String> = _editBio.asStateFlow()

    init {
        _currentUser.value   = FakeMediaRepository.currentUser
        _libraryPreview.value = FakeMediaRepository.libraryItems.take(6)
        _editDisplayName.value = FakeMediaRepository.currentUser.displayName
        _editUsername.value    = FakeMediaRepository.currentUser.username
        _editBio.value         = FakeMediaRepository.currentUser.bio ?: ""
        loadFavorites()
    }

    // if this fails the rest of the profile still draws, so we just log it and leave
    // the list empty rather than erroring the whole screen.
    fun loadFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = favoriteRepository.getFavorites()
            } catch (e: Exception) {
                Log.w("Profile", "GET /favorites failed", e)
                _favorites.value = emptyList()
            }
        }
    }

    fun onEditDisplayNameChange(value: String) { _editDisplayName.value = value }
    fun onEditUsernameChange(value: String)    { _editUsername.value    = value }
    fun onEditBioChange(value: String)          { _editBio.value        = value }

    fun saveProfile() {
        // TODO (Week 10): Call PUT /users/me with Retrofit
        _currentUser.value = _currentUser.value?.copy(
            displayName = _editDisplayName.value,
            username    = _editUsername.value,
            bio         = _editBio.value.ifBlank { null }
        )
    }

    fun loadUserById(userId: String): UserProfile? {
        // TODO (Week 10): Call GET /users/{id} with Retrofit
        return FakeMediaRepository.followers.find { it.id == userId }
            ?: FakeMediaRepository.following.find { it.id == userId }
    }
}
