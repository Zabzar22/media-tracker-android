package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Media
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MediaApiService {
    // GET /media — search + list the catalog. Sorted id ASC on the server so the
    // cursor stays stable. `after` is the opaque cursor from the previous page's
    // X-Next-Cursor header (null on the first page).
    @GET("media")
    suspend fun searchMedia(
        @Query("query") query: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("after") after: String? = null
    ): Response<List<Media>>

    // GET /media/{id} — full details for one item (used by the detail screen).
    // Returns the media object directly (not paginated); 404 if the id doesn't exist.
    @GET("media/{id}")
    suspend fun getMediaById(@Path("id") id: Int): Response<Media>
}
