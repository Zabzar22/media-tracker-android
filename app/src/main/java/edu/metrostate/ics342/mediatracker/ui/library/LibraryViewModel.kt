package edu.metrostate.ics342.mediatracker.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.network.DefaultLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// the repository is a constructor parameter with a default instead of being built in here,
// so the test can hand in a mockk fake. @JvmOverloads keeps the no-argument constructor
// that viewModel() needs.
class LibraryViewModel @JvmOverloads constructor(
    private val libraryRepository: DefaultLibraryRepository = DefaultLibraryRepository()
) : ViewModel() {

    private val _libraryItems = MutableStateFlow<List<LibraryItem>>(emptyList())
    val libraryItems: StateFlow<List<LibraryItem>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // null when the last load worked. holds a message when it didn't, so the screen can
    // show it with a Retry instead of pretending the library is empty.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // a tap that failed and got rolled back. this one is separate from errorMessage on
    // purpose ; the list is still fine, so it goes in a snackbar instead of replacing the
    // whole screen the way a failed load does.
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

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

    // optimistic. the row goes away the moment you tap, and DELETE /library/{mediaId} runs
    // after. we keep a copy of the item first so a failed request can put it back.
    fun removeItem(mediaId: Int) {
        val backup = _libraryItems.value.find { it.mediaId == mediaId } ?: return
        _libraryItems.value = _libraryItems.value.filter { it.mediaId != mediaId }

        viewModelScope.launch {
            try {
                libraryRepository.removeFromLibrary(mediaId)
            } catch (e: Exception) {
                Log.w("Library", "DELETE /library/$mediaId failed", e)
                rollBack(backup, "Couldn't remove item. Try again.")
            }
        }
    }

    // same idea for moving an item to another tab. the tab we're looking at only holds one
    // status, so a change means the row leaves this list right away ; PUT runs after.
    fun updateStatus(mediaId: Int, newStatus: LibraryStatus) {
        val backup = _libraryItems.value.find { it.mediaId == mediaId } ?: return
        if (backup.status == newStatus) return          // nothing would change
        _libraryItems.value = _libraryItems.value.filter { it.mediaId != mediaId }

        viewModelScope.launch {
            try {
                libraryRepository.updateStatus(mediaId, newStatus)
            } catch (e: Exception) {
                Log.w("Library", "PUT /library/$mediaId failed", e)
                rollBack(backup, "Couldn't update status. Try again.")
            }
        }
    }

    // puts an item back after a failed request. it lands at the end rather than where it
    // was, which the handout says is fine, and then the snackbar explains what happened.
    private fun rollBack(item: LibraryItem, message: String) {
        _libraryItems.value = _libraryItems.value + item
        _actionError.value = message
    }

    // the screen calls this once it has shown the snackbar, so the same message doesn't
    // come back the next time something recomposes.
    fun clearActionError() {
        _actionError.value = null
    }

    fun updateFilter(status: LibraryStatus) {
        _filterState.value = status
        loadLibrary()          // different tab, different request
    }
}
