package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Media
import kotlinx.serialization.decodeFromString
import retrofit2.HttpException
import retrofit2.Response

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
            throw MediaNotFoundException(errorMessage(response) ?: "Media item not found.")
        }
        if (!response.isSuccessful) {
            // The server's message is written to be read by users, so pass it along.
            // No message = throw a blank one and let the screen use its own wording.
            error(errorMessage(response).orEmpty())
        }
        return response.body() ?: throw HttpException(response)
    }

    // The server explains itself in the error body ({ "message": ... }), so show that
    // wording rather than a status code. Returns null if the body isn't what we expect.
    private fun errorMessage(response: Response<*>): String? = try {
        response.errorBody()?.string()?.let {
            RetrofitInstance.json.decodeFromString<ErrorResponse>(it).message
        }
    } catch (e: Exception) {
        null
    }
}
