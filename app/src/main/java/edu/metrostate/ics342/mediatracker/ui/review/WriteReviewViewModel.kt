package edu.metrostate.ics342.mediatracker.ui.review

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.AlreadyReviewedException
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Both repositories are constructor parameters with defaults, the same way the library and
// detail view models take theirs, so a test can hand in mockk fakes. @JvmOverloads keeps
// the no-argument constructor that viewModel() calls.
class WriteReviewViewModel @JvmOverloads constructor(
    private val reviewRepository: DefaultReviewRepository = DefaultReviewRepository(),
    private val mediaRepository: DefaultMediaRepository = DefaultMediaRepository()
) : ViewModel() {

    // Where the POST has got to. The screen watches this rather than doing anything in the
    // button's onClick, because the request is async — navigating back the moment you tap
    // would leave before the server had actually taken the review.
    sealed interface SubmitState {
        data object Idle : SubmitState
        data object Submitting : SubmitState
        data object Success : SubmitState
        // A string id, not a string, so the message stays in strings.xml and this class
        // stays testable without an Android context. Same shape as AuthViewModel's error.
        data class Error(@param:StringRes val messageRes: Int) : SubmitState
    }

    // The summary at the top of the form. Nullable because it arrives a moment after the
    // screen does, and because the form still works if it never arrives at all.
    private val _media = MutableStateFlow<Media?>(null)
    val media: StateFlow<Media?> = _media.asStateFlow()

    private val _rating = MutableStateFlow(0)          // 0 = nothing picked yet
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _reviewText = MutableStateFlow("")
    val reviewText: StateFlow<String> = _reviewText.asStateFlow()

    private val _shareToFeed = MutableStateFlow(true)  // checked by default, like the spec
    val shareToFeed: StateFlow<Boolean> = _shareToFeed.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // The cover and title at the top are a nicety, not the point of the screen, so if this
    // fails we just leave them out - you can still rate and write. Same GET /media/{id} the
    // detail screen you came from already made.
    fun load(mediaId: Int) {
        viewModelScope.launch {
            try {
                _media.value = mediaRepository.getMedia(mediaId)
            } catch (e: Exception) {
                Log.w("WriteReview", "GET /media/$mediaId failed", e)
            }
        }
    }

    // Tapping star N sets the rating to N. Not five independent toggles, and not something
    // that only climbs — tapping star 2 after star 5 has to leave you on 2. That means no
    // comparing `value` against the rating we already have before accepting it.
    fun onRatingChange(value: Int) {
        _rating.value = value.coerceIn(0, MAX_RATING)
    }

    // Capping here rather than in the text field means the counter under the box and the
    // thing we'd actually send can never disagree, and the server never has to reject us
    // for length.
    fun onReviewTextChange(value: String) {
        _reviewText.value = value.take(MAX_REVIEW_LENGTH)
    }

    fun onShareToFeedChange(value: Boolean) {
        _shareToFeed.value = value
    }

    // The screen already disables the button until a star is picked; this is the same rule
    // stated where it can't be got around, so a caller that forgets can't post a 0.
    fun submit(mediaId: Int) {
        if (_rating.value < MIN_RATING) return
        // The button is disabled while this runs, but a fast double tap can still get two
        // calls in before the first recomposition, and the second would come back a 409.
        if (_submitState.value is SubmitState.Submitting) return

        _submitState.value = SubmitState.Submitting
        viewModelScope.launch {
            _submitState.value = try {
                reviewRepository.createReview(
                    mediaId     = mediaId,
                    rating      = _rating.value,
                    // An empty box means "no text", which the server wants as null rather
                    // than an empty string.
                    reviewText  = _reviewText.value.trim().ifBlank { null },
                    shareToFeed = _shareToFeed.value
                )
                SubmitState.Success
            } catch (e: AlreadyReviewedException) {
                // 409. One review per person per item — editing the one you already wrote
                // is next week's job, so for now we just say so and stay on the form.
                Log.w("WriteReview", "POST /reviews rejected as a duplicate", e)
                SubmitState.Error(R.string.review_error_already_reviewed)
            } catch (e: Exception) {
                Log.w("WriteReview", "POST /reviews failed", e)
                SubmitState.Error(R.string.review_error_generic)
            }
        }
    }

    // The screen calls this once it has shown the message. Going back to Idle also puts the
    // Post button back in play, so a failure is retryable without leaving the screen.
    fun clearError() {
        if (_submitState.value is SubmitState.Error) _submitState.value = SubmitState.Idle
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val MAX_REVIEW_LENGTH = 500
    }
}
