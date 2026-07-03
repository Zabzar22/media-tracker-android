package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Media

// One page of media results: the items plus the paging info pulled from the
// response headers. `nextCursor` is passed back as `after` to fetch the next page.
data class MediaPage(
    val items: List<Media>,
    val nextCursor: String?,
    val hasMore: Boolean
)

class DefaultMediaRepository(
    private val api: MediaApiService = RetrofitInstance.mediaApiService
) {
    suspend fun search(query: String, type: String?, after: String?): MediaPage {
        val response = api.searchMedia(
            query = query.ifBlank { null },
            type  = type?.ifBlank { null },
            after = after
        )
        val items      = response.body() ?: emptyList()
        val nextCursor = response.headers()["X-Next-Cursor"]
        val hasMore    = response.headers()["X-Has-More"] == "true"
        return MediaPage(items, nextCursor, hasMore)
    }

    // Full details for one item. Returns null on 404 (unknown id) or any non-2xx.
    suspend fun getMedia(id: Int): Media? {
        val response = api.getMediaById(id)
        return if (response.isSuccessful) response.body() else null
    }
}
