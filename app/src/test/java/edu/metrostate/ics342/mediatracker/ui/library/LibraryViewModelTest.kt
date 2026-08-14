package edu.metrostate.ics342.mediatracker.ui.library

import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaType
import edu.metrostate.ics342.mediatracker.data.network.DefaultLibraryRepository
import io.mockk.coEvery
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

// tests for the optimistic remove. the repository is a mockk fake, so nothing here talks
// to the real API ; we decide whether the call succeeds or throws.
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    // viewModelScope runs on Dispatchers.Main, which doesn't exist in a plain unit test,
    // so we swap in a test dispatcher we can step through by hand.
    private val dispatcher = StandardTestDispatcher()

    private val repository = mockk<DefaultLibraryRepository>()

    private val item = LibraryItem(
        userId    = "user-1",
        mediaId   = 1,
        status    = LibraryStatus.WANT_TO,
        addedAt   = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
        media     = Media(id = 1, mediaType = MediaType.BOOK, title = "Dune")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // the view model loads the Want To tab in its init block.
        coEvery { repository.getLibrary(LibraryStatus.WANT_TO) } returns listOf(item)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeItem takes the item out right away`() = runTest(dispatcher) {
        coEvery { repository.removeFromLibrary(1) } returns Unit

        val viewModel = LibraryViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()   // let the initial load finish

        viewModel.removeItem(1)

        // no advanceUntilIdle here on purpose ; DELETE hasn't run yet and the row is
        // already gone. that's the whole point of doing it optimistically.
        assertTrue(viewModel.libraryItems.value.none { it.mediaId == 1 })
    }

    @Test
    fun `removeItem puts the item back when the call fails`() = runTest(dispatcher) {
        coEvery { repository.removeFromLibrary(1) } throws IOException("network down")

        val viewModel = LibraryViewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.removeItem(1)
        dispatcher.scheduler.advanceUntilIdle()   // now let DELETE run and fail

        assertEquals(1, viewModel.libraryItems.value.size)
        assertTrue(viewModel.libraryItems.value.any { it.mediaId == 1 })
        assertEquals("Couldn't remove item. Try again.", viewModel.actionError.value)
    }
}
