package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Media
import retrofit2.HttpException

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

    // Get one item's full details. A 404 here means "no such item" and gets its own
    // exception so the screen can say so; anything else non-2xx is a real failure.
    suspend fun getMedia(id: Int): Media {
        val response = api.getMediaById(id)
        if (response.code() == 404) {
            throw MediaNotFoundException(response.errorMessage() ?: "Media item not found.")
        }
        if (!response.isSuccessful) {
            // The server's message is written to be read by users, so pass it along.
            // No message = throw a blank one and let the screen use its own wording.
            error(response.errorMessage().orEmpty())
        }
        return response.body() ?: throw HttpException(response)
    }
}
