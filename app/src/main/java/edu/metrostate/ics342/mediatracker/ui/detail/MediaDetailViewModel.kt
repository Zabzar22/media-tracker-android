package edu.metrostate.ics342.mediatracker.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.DefaultFavoriteRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultLibraryRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultReviewRepository
import edu.metrostate.ics342.mediatracker.data.network.MediaNotFoundException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// What the detail screen can be showing. Same loading / success / error idea I used for
// search, plus NotFound so a bad id gets its own message instead of "something broke".
sealed interface MediaDetailUiState {
    data object Loading : MediaDetailUiState
    data class Success(
        val media: Media,
        val libraryStatus: LibraryStatus? = null,   // null = not in the library yet
        val reviews: List<Review> = emptyList(),
        val isUpdatingLibrary: Boolean = false,      // true while POST /library is running
        val isFavorite: Boolean = false,             // already in favorites?
        val isUpdatingFavorite: Boolean = false      // true while POST /favorites is running
    ) : MediaDetailUiState
    data object NotFound : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
}

class MediaDetailViewModel : ViewModel() {
    private val repository = DefaultMediaRepository()
    private val libraryRepository = DefaultLibraryRepository()
    private val reviewRepository = DefaultReviewRepository()
    private val favoriteRepository = DefaultFavoriteRepository()

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    fun load(mediaId: Int) {
        _uiState.value = MediaDetailUiState.Loading
        viewModelScope.launch {
            // all four go out at once instead of waiting in line. Each one catches its own
            // failure and hands back a Result: an async that throws also kills its siblings,
            // and that escapes a try/catch around await(), so none of them may throw.
            val mediaCall   = async { runCatching { repository.getMedia(mediaId) } }
            val libraryCall = async { runCatching { libraryRepository.getLibraryItem(mediaId) } }
            val reviewsCall = async { runCatching { reviewRepository.getReviews(mediaId) } }
            // asks "have I favorited this?" so the Save button starts out honest.
            val favoriteCall = async { runCatching { favoriteRepository.getFavorite(mediaId) } }

            // Only the media call decides whether we have a screen at all.
            val media = mediaCall.await().getOrElse { e ->
                Log.w("MediaDetail", "GET /media/$mediaId failed", e)
                // Blank message = the screen falls back to its own wording.
                _uiState.value =
                    if (e is MediaNotFoundException) MediaDetailUiState.NotFound
                    else MediaDetailUiState.Error(e.message.orEmpty())
                return@launch
            }

            // the other three are extras ; if they fail we still show the page without them.
            val status = libraryCall.await()
                .onFailure { Log.w("MediaDetail", "GET /library/$mediaId failed", it) }
                .getOrNull()?.status
            val reviews = reviewsCall.await()
                .onFailure { Log.w("MediaDetail", "GET /reviews?mediaId=$mediaId failed", it) }
                .getOrElse { emptyList() }

            // null back from the repo means a 404, which just means "not favorited".
            val isFavorite = favoriteCall.await()
                .onFailure { Log.w("MediaDetail", "GET /favorites/$mediaId failed", it) }
                .getOrNull() != null

            _uiState.value = MediaDetailUiState.Success(media, status, reviews, isFavorite = isFavorite)
        }
    }

    // "+ Want To" tap. Adds the item as want_to. Guards against a double-tap: if a request
    // is already in flight, or it's already in the library, this does nothing.
    fun addToWantTo(mediaId: Int) {
        val current = _uiState.value
        if (current !is MediaDetailUiState.Success) return
        if (current.isUpdatingLibrary || current.libraryStatus != null) return

        _uiState.value = current.copy(isUpdatingLibrary = true)
        viewModelScope.launch {
            _uiState.value = try {
                val item = libraryRepository.addToLibrary(mediaId, LibraryStatus.WANT_TO)
                current.copy(libraryStatus = item.status, isUpdatingLibrary = false)
            } catch (e: Exception) {
                Log.w("MediaDetail", "POST /library failed", e)
                current.copy(isUpdatingLibrary = false)   // let them try again
            }
        }
    }

    // the "Save" tap ; it's a toggle, so tapping it again takes the item back out of
    // favorites. there's nowhere else in the app to un-save something. only guard is
    // ignoring taps while a request is already running.
    fun toggleFavorite(mediaId: Int) {
        val current = _uiState.value
        if (current !is MediaDetailUiState.Success) return
        if (current.isUpdatingFavorite) return

        _uiState.value = current.copy(isUpdatingFavorite = true)
        viewModelScope.launch {
            _uiState.value = try {
                if (current.isFavorite) {
                    favoriteRepository.removeFavorite(mediaId)
                    current.copy(isFavorite = false, isUpdatingFavorite = false)
                } else {
                    favoriteRepository.addFavorite(mediaId)
                    current.copy(isFavorite = true, isUpdatingFavorite = false)
                }
            } catch (e: Exception) {
                Log.w("MediaDetail", "favorites toggle failed", e)
                current.copy(isUpdatingFavorite = false)   // let them try again
            }
        }
    }
}
