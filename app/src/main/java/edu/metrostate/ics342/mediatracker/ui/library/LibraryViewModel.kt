package edu.metrostate.ics342.mediatracker.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.network.DefaultLibraryRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {

    private val libraryRepository = DefaultLibraryRepository()

    private val _libraryItems = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraryItems: StateFlow<List<LibraryItem>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // null when the last load worked. holds a message when it didn't, so the screen can
    // show it with a Retry instead of pretending the library is empty.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _filterState = MutableStateFlow( value = LibraryStatus.WANT_TO)
    val filterState: StateFlow<LibraryStatus> = _filterState.asStateFlow()

    init {
        loadLibrary()
    }

    // loads whichever tab the user is on. the server filters by status, so switching
    // tabs means a fresh request rather than filtering a list we already have.
    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null
            try {
                _libraryItems.value = libraryRepository.getLibrary(_filterState.value)
            } catch (e: Exception) {
                Log.w("Library", "GET /library failed", e)
                _libraryItems.value = emptyList()
                _errorMessage.value = e.message.orEmpty()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // both of these tell the server first, then reload the tab so what's on screen is
    // whatever the server actually has. that's why a removed item no longer comes back
    // when you switch tabs ; it's really gone now, not just hidden from our copy.
    fun removeItem(mediaId: Int) {
        viewModelScope.launch {
            try {
                libraryRepository.removeFromLibrary(mediaId)
                loadLibrary()
            } catch (e: Exception) {
                Log.w("Library", "DELETE /library/$mediaId failed", e)
                _errorMessage.value = e.message.orEmpty()
            }
        }
    }

    // moves an item to another tab. it disappears from this one on the reload, because
    // the request we send asks for one status at a time.
    fun updateStatus(mediaId: Int, newStatus: LibraryStatus) {
        viewModelScope.launch {
            try {
                libraryRepository.updateStatus(mediaId, newStatus)
                loadLibrary()
            } catch (e: Exception) {
                Log.w("Library", "PUT /library/$mediaId failed", e)
                _errorMessage.value = e.message.orEmpty()
            }
        }
    }
    fun updateFilter(status: LibraryStatus) {
        _filterState.value = status
        loadLibrary()          // different tab, different request
    }
}
