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
import edu.metrostate.ics342.mediatracker.data.network.TokenStore
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
        // GET /reviews failed while the rest of the page loaded fine. Without this an
        // empty list would read as "no reviews yet", which is a different thing and the
        // wrong thing to tell someone whose connection dropped.
        val reviewsFailed: Boolean = false,
        val isFavorite: Boolean = false,            // already in favorites?
        // who we are, so a review card can tell "yours" from everyone else's. comes from
        // the login response, so it's already known by the time we get here.
        val currentUserId: String? = null
    ) : MediaDetailUiState
    data object NotFound : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
}

// the four repositories are constructor parameters with defaults now, same reason as the
// library view model ; a test can pass fakes in. @JvmOverloads keeps the no-argument
// constructor that viewModel() calls.
class MediaDetailViewModel @JvmOverloads constructor(
    private val repository: DefaultMediaRepository = DefaultMediaRepository(),
    private val libraryRepository: DefaultLibraryRepository = DefaultLibraryRepository(),
    private val reviewRepository: DefaultReviewRepository = DefaultReviewRepository(),
    private val favoriteRepository: DefaultFavoriteRepository = DefaultFavoriteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    // a tap that failed and got undone. goes in a snackbar, not the error state, because
    // the page itself is still fine ; only the button had to go back.
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

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

            // who we are. no request for this one - POST /tokens handed us the profile when
            // we logged in, so it's just sitting in TokenStore.
            val currentUserId = TokenStore.currentUserId

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
            val reviewsResult = reviewsCall.await()
                .onFailure { Log.w("MediaDetail", "GET /reviews?mediaId=$mediaId failed", it) }
            val reviews = reviewsResult.getOrElse { emptyList() }

            // null back from the repo means a 404, which just means "not favorited".
            val isFavorite = favoriteCall.await()
                .onFailure { Log.w("MediaDetail", "GET /favorites/$mediaId failed", it) }
                .getOrNull() != null

            _uiState.value = MediaDetailUiState.Success(
                media         = media,
                libraryStatus = status,
                reviews       = mineFirst(reviews, currentUserId),
                reviewsFailed = reviewsResult.isFailure,
                isFavorite    = isFavorite,
                currentUserId = currentUserId
            )
        }
    }

    // asks for the item and its reviews again, without the loading spinner. this is what
    // makes a review you just posted show up, since coming back from Write Review doesn't
    // re-run load(). we ask for the media too because a new review changes the header
    // numbers as well ; the count goes up and your stars move the average.
    //
    // if the page hasn't loaded yet there's nothing to refresh, so we leave. that also
    // stops a second set of requests going out while load() is still running.
    fun refresh(mediaId: Int) {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        viewModelScope.launch {
            // both at once, same as load() does above.
            val mediaCall   = async { runCatching { repository.getMedia(mediaId) } }
            val reviewsCall = async { runCatching { reviewRepository.getReviews(mediaId) } }

            // if a call fails we keep what's already on screen. the page was fine a second
            // ago, so slightly stale numbers beat blanking it out.
            val media = mediaCall.await()
                .onFailure { Log.w("MediaDetail", "refresh GET /media/$mediaId failed", it) }
                .getOrDefault(current.media)
            val reviewsResult = reviewsCall.await()
                .onFailure { Log.w("MediaDetail", "refresh GET /reviews?mediaId=$mediaId failed", it) }
            val reviews = reviewsResult.getOrDefault(current.reviews)

            // read the state again instead of reusing `current` ; a Save or Want To tap
            // could have happened while we were waiting, and that should stick.
            val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
            _uiState.value = latest.copy(
                media         = media,
                reviews       = mineFirst(reviews, latest.currentUserId),
                reviewsFailed = reviewsResult.isFailure
            )
        }
    }

    // Puts your own review at the top and leaves everyone else in the order the server sent
    // (newest first). Two filters rather than a sort so it's obvious what comes out, and so
    // the rest of the list keeps its order exactly.
    private fun mineFirst(reviews: List<Review>, currentUserId: String?): List<Review> {
        if (currentUserId == null) return reviews
        val mine   = reviews.filter { it.userId == currentUserId }
        val theirs = reviews.filter { it.userId != currentUserId }
        return mine + theirs
    }

    // "+ Want To" tap. optimistic ; the button says Want To right away and POST /library
    // runs after. does nothing if it's already in the library.
    fun addToWantTo(mediaId: Int) {
        val current = _uiState.value
        if (current !is MediaDetailUiState.Success) return
        if (current.libraryStatus != null) return

        _uiState.value = current.copy(libraryStatus = LibraryStatus.WANT_TO)
        viewModelScope.launch {
            try {
                libraryRepository.addToLibrary(mediaId, LibraryStatus.WANT_TO)
            } catch (e: Exception) {
                Log.w("MediaDetail", "POST /library failed", e)
                // put the button back the way it was and say why.
                val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = latest.copy(libraryStatus = null)
                _actionError.value = "Couldn't add to library. Try again."
            }
        }
    }

    // the "Save" tap ; a toggle, so tapping again takes the item back out of favorites.
    // the heart flips first and the request follows, so a failure has to flip it back.
    fun toggleFavorite(mediaId: Int) {
        val current = _uiState.value
        if (current !is MediaDetailUiState.Success) return

        val wasFavorite = current.isFavorite
        _uiState.value = current.copy(isFavorite = !wasFavorite)
        viewModelScope.launch {
            try {
                if (wasFavorite) favoriteRepository.removeFavorite(mediaId)
                else             favoriteRepository.addFavorite(mediaId)
            } catch (e: Exception) {
                Log.w("MediaDetail", "favorites toggle failed", e)
                val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = latest.copy(isFavorite = wasFavorite)
                _actionError.value = "Couldn't update saved items. Try again."
            }
        }
    }

    // Delete straight from the review card, once its confirm dialog has been agreed to.
    // Optimistic like the two above: the card goes away on the tap and DELETE runs after,
    // so a failure has to put it back.
    //
    // reviewId is the Review's own id, not the mediaId - see DefaultReviewRepository.
    fun deleteReview(mediaId: Int, reviewId: Int) {
        val current = _uiState.value
        if (current !is MediaDetailUiState.Success) return
        // keep a copy so a failed request can restore it, same as the library screen does.
        val removed = current.reviews.find { it.id == reviewId } ?: return

        _uiState.value = current.copy(
            reviews = current.reviews.filter { it.id != reviewId },
            // the header count is the server's number, so it doesn't drop on its own.
            // coerce because a stale count could already be 0 and we'd go negative.
            media   = current.media.copy(
                reviewCount = (current.media.reviewCount - 1).coerceAtLeast(0)
            )
        )

        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(reviewId)
                // our stars were part of the average, and only the server knows what it is
                // now, so ask again rather than trying to work it out here.
                refresh(mediaId)
            } catch (e: Exception) {
                Log.w("MediaDetail", "DELETE /reviews/$reviewId failed", e)
                val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = latest.copy(
                    // mineFirst puts it back at the top where it was, since it's ours.
                    reviews = mineFirst(latest.reviews + removed, latest.currentUserId),
                    media   = latest.media.copy(
                        reviewCount = latest.media.reviewCount + 1
                    )
                )
                _actionError.value = "Couldn't delete your review. Try again."
            }
        }
    }

    // the screen calls this after showing the snackbar so it doesn't come back.
    fun clearActionError() {
        _actionError.value = null
    }
}
