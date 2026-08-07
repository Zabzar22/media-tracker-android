package edu.metrostate.ics342.mediatracker.ui.review

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.AlreadyReviewedException
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultReviewRepository
import edu.metrostate.ics342.mediatracker.data.network.TokenStore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Both repositories are constructor parameters with defaults, the same way the library and
// detail view models take theirs, so a test can hand in mockk fakes. @JvmOverloads keeps
// the no-argument constructor that viewModel() calls.
//
// Week 12: this drives writing AND editing. There's no separate edit screen - on the way in
// we ask the server whether we've already reviewed this item, and if we have, the form
// fills itself in and saves with PUT instead of POST. Nothing else about the screen changes,
// which is the whole reason it's one screen and not two.
class WriteReviewViewModel @JvmOverloads constructor(
    private val reviewRepository: DefaultReviewRepository = DefaultReviewRepository(),
    private val mediaRepository: DefaultMediaRepository = DefaultMediaRepository()
) : ViewModel() {

    // Where the save has got to. The screen watches this rather than doing anything in the
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

    // The id of the review we're editing, or null when this is a brand new one. This single
    // field is what makes the screen an edit screen: it picks PUT over POST, turns on the
    // Delete button, and swaps the title and button wording.
    private val _editingReviewId = MutableStateFlow<Int?>(null)
    val editingReviewId: StateFlow<Int?> = _editingReviewId.asStateFlow()

    // True while we're finding out whether a review already exists. The form waits behind a
    // spinner for this - showing empty stars and then filling them in a beat later looks
    // like the app forgot what you wrote.
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // Two questions on the way in: what am I rating (for the header), and have I already
    // rated it (which decides POST vs PUT). They go out together rather than in a line,
    // same as the detail screen does with its four calls.
    //
    // The header is a nicety, so if GET /media/{id} fails we just leave it out - you can
    // still rate and write. The existing-review check failing is more awkward: we fall back
    // to treating it as a new review, and a POST that turns out to be a duplicate comes
    // back 409, which submit() below knows how to recover from.
    fun load(mediaId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val mediaCall = async { runCatching { mediaRepository.getMedia(mediaId) } }
            val mineCall  = async { runCatching { fetchMyReview(mediaId) } }

            _media.value = mediaCall.await()
                .onFailure { Log.w("WriteReview", "GET /media/$mediaId failed", it) }
                .getOrNull()

            mineCall.await()
                .onFailure { Log.w("WriteReview", "existing review lookup failed", it) }
                .getOrNull()
                ?.let { fillFormFrom(it) }

            _isLoading.value = false
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

    // The one save button. New review -> POST /reviews. Editing -> PUT /reviews/{id}. The
    // screen doesn't have to know which; it just taps Save and watches submitState.
    //
    // The screen already disables the button until a star is picked; the same rule is
    // repeated here where it can't be got around, so a caller that forgets can't post a 0.
    fun submit(mediaId: Int) {
        if (_rating.value < MIN_RATING) return
        // The button is disabled while this runs, but a fast double tap can still get two
        // calls in before the first recomposition, and the second would come back a 409.
        if (_submitState.value is SubmitState.Submitting) return

        val reviewId = _editingReviewId.value
        _submitState.value = SubmitState.Submitting
        viewModelScope.launch {
            // An empty box means "no text". The server wants that as null rather than an
            // empty string - and on a PUT, null is what clears text you'd written before.
            val text = _reviewText.value.trim().ifBlank { null }

            _submitState.value = try {
                if (reviewId == null) {
                    reviewRepository.createReview(
                        mediaId     = mediaId,
                        rating      = _rating.value,
                        reviewText  = text,
                        shareToFeed = _shareToFeed.value
                    )
                } else {
                    reviewRepository.updateReview(
                        reviewId   = reviewId,
                        rating     = _rating.value,
                        reviewText = text
                    )
                }
                SubmitState.Success
            } catch (e: AlreadyReviewedException) {
                // 409 on a POST: one review per person per item, and we didn't know about
                // the one that's already there (the lookup in load() failed, or it was
                // written somewhere else since). Go and get it, which flips us into edit
                // mode - so the same Save button the user is looking at now sends a PUT
                // and works on the second tap.
                Log.w("WriteReview", "POST /reviews rejected as a duplicate", e)
                runCatching { fetchMyReview(mediaId) }.getOrNull()?.let { fillFormFrom(it) }
                SubmitState.Error(R.string.review_error_already_reviewed)
            } catch (e: Exception) {
                Log.w("WriteReview", "saving the review failed", e)
                // Two wordings, because "couldn't post" is confusing when what you were
                // doing was editing something you posted last week.
                SubmitState.Error(
                    if (reviewId == null) R.string.review_error_generic
                    else                  R.string.review_error_save
                )
            }
        }
    }

    // Delete the review being edited. The screen asks "are you sure?" first - this only
    // runs once that dialog has been confirmed. Success sends the screen back the same way
    // a save does, since either way there's nothing left on this form to look at.
    fun delete() {
        // Null means nothing has been posted yet, so there's nothing on the server to
        // delete. The Delete button isn't shown in that case, but this makes it safe anyway.
        val reviewId = _editingReviewId.value ?: return
        if (_submitState.value is SubmitState.Submitting) return

        _submitState.value = SubmitState.Submitting
        viewModelScope.launch {
            _submitState.value = try {
                reviewRepository.deleteReview(reviewId)
                SubmitState.Success
            } catch (e: Exception) {
                Log.w("WriteReview", "DELETE /reviews/$reviewId failed", e)
                SubmitState.Error(R.string.review_error_delete)
            }
        }
    }

    // The screen calls this once it has shown the message. Going back to Idle also puts the
    // Save button back in play, so a failure is retryable without leaving the screen.
    fun clearError() {
        if (_submitState.value is SubmitState.Error) _submitState.value = SubmitState.Idle
    }

    // Our own review of this item, or null if we haven't written one. Null user id means
    // nobody is signed in, which shouldn't happen behind the login screen - treated as
    // "no existing review" rather than crashing the form.
    private suspend fun fetchMyReview(mediaId: Int): Review? {
        val userId = TokenStore.currentUserId ?: return null
        return reviewRepository.getMyReview(mediaId, userId)
    }

    // Drop an existing review into the form so the user edits what they wrote instead of
    // starting over. Remembering the id here is what turns the next save into a PUT.
    private fun fillFormFrom(review: Review) {
        _editingReviewId.value = review.id
        _rating.value          = review.rating
        _reviewText.value      = review.reviewText.orEmpty()
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val MAX_REVIEW_LENGTH = 500
    }
}
