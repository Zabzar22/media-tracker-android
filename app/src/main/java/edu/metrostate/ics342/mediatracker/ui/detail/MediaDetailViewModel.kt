package edu.metrostate.ics342.mediatracker.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// What the detail screen renders. The detail screen is the ONLY place that pulls a
// single item from the server (GET /media/{id}); search / library / feed keep using
// their own data sources.
sealed interface MediaDetailUiState {
    data object Loading : MediaDetailUiState
    data class Success(val media: Media) : MediaDetailUiState
    data object NotFound : MediaDetailUiState
}

class MediaDetailViewModel : ViewModel() {
    private val repository = DefaultMediaRepository()

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    fun load(mediaId: Int) {
        _uiState.value = MediaDetailUiState.Loading
        viewModelScope.launch {
            val fromServer = try {
                repository.getMedia(mediaId)
            } catch (e: Exception) {
                Log.w("MediaDetail", "GET /media/$mediaId failed", e)
                null
            }

            // Fall back to the bundled sample item only when the server doesn't have
            // this id — e.g. tapping one of the hardcoded library/feed entries whose
            // ids don't exist on the server. Real search results resolve from the
            // server above and show their real cover.
            val media = fromServer ?: FakeMediaRepository.mediaList.find { it.id == mediaId }

            _uiState.value =
                if (media != null) MediaDetailUiState.Success(media)
                else MediaDetailUiState.NotFound
        }
    }
}
