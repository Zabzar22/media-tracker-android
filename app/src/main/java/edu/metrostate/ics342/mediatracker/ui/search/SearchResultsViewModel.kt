package edu.metrostate.ics342.mediatracker.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.fakeSearchResults
import edu.metrostate.ics342.mediatracker.data.model.Media
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchResultsViewModel : ViewModel() {

    private val pageSize = 20

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    // Total number of matches (shown above the list).
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    // Whether another page of results is still available to load.
    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    // True while a page is being fetched (drives the bottom spinner).
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentQuery = ""
    private var allMatches: List<Media> = emptyList()
    private var loadedCount = 0
    private var pagingJob: Job? = null

    fun search(query: String) {
        currentQuery = query
        applyFilter()
    }

    fun onTypeSelect(type: String) {
        _selectedType.value = type
        applyFilter()
    }

    /**
     * Loads the NEXT 20 results and appends them.
     *
     * The `isLoadingMore` guard ensures only ONE page loads at a time, so a
     * single scroll-to-bottom adds exactly 20 — never the whole list. The delay
     * simulates fetching one page; when GET /media is wired this becomes the real
     * request, and `canLoadMore` / the next offset come from its response headers.
     * This is just a DevTool for now, because I couldn't see if we were appending the whole list - or just 20 at a time.
     */
    fun loadNextPage() {
        if (_isLoadingMore.value) return            // a page is already loading
        if (loadedCount >= allMatches.size) return  // no more pages
        pagingJob = viewModelScope.launch {
            _isLoadingMore.value = true
            delay(500)                              // simulate fetching one page
            val nextPage = allMatches.drop(loadedCount).take(pageSize)
            loadedCount += nextPage.size
            _results.value = _results.value + nextPage
            _canLoadMore.value = loadedCount < allMatches.size
            _isLoadingMore.value = false
            Log.d("SearchPaging", "Loaded +${nextPage.size} → showing $loadedCount of ${allMatches.size}")
        }
    }

    private fun applyFilter() {
        pagingJob?.cancel()
        _isLoadingMore.value = false
        val q = currentQuery.trim()
        val type = _selectedType.value
        allMatches = fakeSearchResults.filter { media ->
            (type.isEmpty() || media.mediaType == type) &&
                (q.isEmpty() || media.matchesQuery(q))
        }
        _totalCount.value = allMatches.size
        loadedCount = 0
        _results.value = emptyList()
        loadNextPage()   // load the first page
    }
}

/** True if the query appears in the title or the type-appropriate credit. */
private fun Media.matchesQuery(q: String): Boolean =
    title.contains(q, ignoreCase = true) ||
        author?.contains(q, ignoreCase = true) == true ||
        director?.contains(q, ignoreCase = true) == true ||
        creator?.contains(q, ignoreCase = true) == true
