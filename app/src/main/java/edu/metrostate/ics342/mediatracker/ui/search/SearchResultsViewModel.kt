package edu.metrostate.ics342.mediatracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchResultsViewModel : ViewModel() {

    private val mediaRepository = DefaultMediaRepository()

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // True while a page is in flight (drives the bottom spinner).
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentQuery = ""
    private var nextCursor: String? = null   // server cursor for the next page; null = start
    private var hasMore = true               // false once the server stops handing out cursors

    fun search(query: String) {
        currentQuery = query
        resetAndLoadFirstPage()
    }

    fun onTypeSelect(type: String) {
        _selectedType.value = type
        resetAndLoadFirstPage()
    }

    private fun resetAndLoadFirstPage() {
        _results.value = emptyList()
        nextCursor = null
        hasMore = true
        loadNextPage()
    }

    /**
     * Fetches the next 20 results from GET /media and appends them.
     *
     * `after = nextCursor` asks the server for whatever comes after the last item we
     * have (null on the first page). The response headers tell us the next cursor and
     * whether more pages remain, so we never track offsets ourselves. The isLoading
     * guard stops a single scroll-to-bottom from firing several overlapping requests,
     * and the hasMore guard stops us asking once the last page is in.
     */
    fun loadNextPage() {
        if (_isLoading.value || !hasMore) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val page = mediaRepository.search(
                    query = currentQuery,
                    type  = _selectedType.value.ifBlank { null },
                    after = nextCursor
                )
                _results.value += page.items
                nextCursor      = page.nextCursor
                hasMore         = page.hasMore
            } catch (e: Exception) {
                // Network/parse error: leave what we have and stop this page. The next
                // scroll-to-bottom will try again.
            } finally {
                _isLoading.value = false
            }
        }
    }
}
