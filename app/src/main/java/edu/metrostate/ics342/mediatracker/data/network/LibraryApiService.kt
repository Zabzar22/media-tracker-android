package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApiService {
    // GET /library ; the whole library for whoever is signed in. status narrows it to
    // one tab, so the server does the filtering instead of us.
    @GET("library")
    suspend fun getLibrary(
        @Query("status") status: String? = null
    ): Response<List<LibraryItem>>
    // GET /library/{mediaId} — is this item already in the user's library?
    // A 404 here is NORMAL ("not added yet"), so we return Response<LibraryItem> and
    // read the status code ourselves instead of letting a non-2xx throw.
    @GET("library/{mediaId}")
    suspend fun getLibraryItem(@Path("mediaId") mediaId: Int): Response<LibraryItem>

    // POST /library — add the item to the library (used by the "+ Want To" button).
    @POST("library")
    suspend fun addToLibrary(@Body body: AddLibraryItemRequest): Response<LibraryItem>

    // PUT /library/{mediaId} ; moves an item to a different tab. sends back the updated
    // item, same shape we already get everywhere else.
    @PUT("library/{mediaId}")
    suspend fun updateLibraryStatus(
        @Path("mediaId") mediaId: Int,
        @Body body: UpdateLibraryStatusRequest
    ): Response<LibraryItem>

    // DELETE /library/{mediaId} ; takes it out of the library for good. nothing comes
    // back in the body, so Response<Unit>.
    @DELETE("library/{mediaId}")
    suspend fun removeFromLibrary(@Path("mediaId") mediaId: Int): Response<Unit>
}
