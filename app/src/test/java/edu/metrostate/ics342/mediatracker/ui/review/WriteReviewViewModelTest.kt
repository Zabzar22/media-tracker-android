package edu.metrostate.ics342.mediatracker.ui.review

import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaType
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultReviewRepository
import edu.metrostate.ics342.mediatracker.data.network.TokenStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

// The ViewModel test on the submit flow that the Week 2 target asks for.
//
// The thing worth testing here is the one decision this screen makes: the same Save button
// has to POST a new review and PUT an edit, and it picks between them on its own. Both
// repositories are mockk fakes, so nothing here touches the real API.
@OptIn(ExperimentalCoroutinesApi::class)
class WriteReviewViewModelTest {

    // viewModelScope runs on Dispatchers.Main, which doesn't exist in a plain unit test,
    // so we swap in a test dispatcher we can step through by hand.
    private val dispatcher = StandardTestDispatcher()

    private val reviewRepository = mockk<DefaultReviewRepository>()
    private val mediaRepository  = mockk<DefaultMediaRepository>()

    private val media = Media(id = 1, mediaType = MediaType.BOOK, title = "Dune")

    private val myReview = Review(
        id         = 42,          // the review's own id - what PUT and DELETE need
        userId     = "user-1",
        mediaId    = 1,           // NOT the id above, and mixing them up is the classic bug
        rating     = 3,
        reviewText = "It was fine.",
        createdAt  = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // The view model asks TokenStore who's signed in before it can ask "have I already
        // reviewed this?", so the test has to sign somebody in.
        TokenStore.currentUser = UserProfile(
            id = "user-1", email = "me@example.com",
            username = "me", displayName = "Me"
        )
        coEvery { mediaRepository.getMedia(1) } returns media
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        TokenStore.clear()          // or the next test inherits this login
    }

    @Test
    fun `a new review is posted`() = runTest(dispatcher) {
        coEvery { reviewRepository.getMyReview(1, "user-1") } returns null   // none yet
        coEvery { reviewRepository.createReview(1, 4, "Good.", true) } returns myReview

        val viewModel = WriteReviewViewModel(reviewRepository, mediaRepository)
        viewModel.load(1)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onRatingChange(4)
        viewModel.onReviewTextChange("Good.")
        viewModel.submit(1)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { reviewRepository.createReview(1, 4, "Good.", true) }
        coVerify(exactly = 0) { reviewRepository.updateReview(any(), any(), any()) }
        assertTrue(viewModel.submitState.value is WriteReviewViewModel.SubmitState.Success)
    }

    @Test
    fun `an existing review is edited, and PUT gets the review id not the media id`() =
        runTest(dispatcher) {
            coEvery { reviewRepository.getMyReview(1, "user-1") } returns myReview
            coEvery { reviewRepository.updateReview(42, 5, "Better than I said.") } returns myReview

            val viewModel = WriteReviewViewModel(reviewRepository, mediaRepository)
            viewModel.load(1)
            dispatcher.scheduler.advanceUntilIdle()

            // The form filled itself in from the review that already existed - this is what
            // makes it an edit screen rather than a blank one.
            assertEquals(42, viewModel.editingReviewId.value)
            assertEquals(3, viewModel.rating.value)
            assertEquals("It was fine.", viewModel.reviewText.value)

            viewModel.onRatingChange(5)
            viewModel.onReviewTextChange("Better than I said.")
            viewModel.submit(1)
            dispatcher.scheduler.advanceUntilIdle()

            // 42, not 1. Both are ints sitting next to each other on the Review object, and
            // sending the wrong one edits somebody else's review or 404s.
            coVerify(exactly = 1) { reviewRepository.updateReview(42, 5, "Better than I said.") }
            coVerify(exactly = 0) { reviewRepository.createReview(any(), any(), any(), any()) }
            assertTrue(viewModel.submitState.value is WriteReviewViewModel.SubmitState.Success)
        }

    @Test
    fun `emptying the text box sends null so the old text is wiped`() = runTest(dispatcher) {
        coEvery { reviewRepository.getMyReview(1, "user-1") } returns myReview
        coEvery { reviewRepository.updateReview(42, 3, null) } returns myReview

        val viewModel = WriteReviewViewModel(reviewRepository, mediaRepository)
        viewModel.load(1)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onReviewTextChange("   ")     // cleared out (the spaces get trimmed)
        viewModel.submit(1)
        dispatcher.scheduler.advanceUntilIdle()

        // null, not "". An empty string would store blank text instead of removing it.
        coVerify(exactly = 1) { reviewRepository.updateReview(42, 3, null) }
    }

    @Test
    fun `a failed save keeps you on the form so you can try again`() = runTest(dispatcher) {
        coEvery { reviewRepository.getMyReview(1, "user-1") } returns myReview
        coEvery { reviewRepository.updateReview(42, 3, "It was fine.") } throws
            IOException("network down")

        val viewModel = WriteReviewViewModel(reviewRepository, mediaRepository)
        viewModel.load(1)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.submit(1)
        dispatcher.scheduler.advanceUntilIdle()

        // Error, not Success - Success is what navigates the screen away, and nothing saved.
        assertTrue(viewModel.submitState.value is WriteReviewViewModel.SubmitState.Error)

        // clearError() runs once the snackbar has been shown, which puts the Save button
        // back in play. We're still editing the same review, so the retry is still a PUT.
        viewModel.clearError()
        assertTrue(viewModel.submitState.value is WriteReviewViewModel.SubmitState.Idle)
        assertEquals(42, viewModel.editingReviewId.value)
    }
}
