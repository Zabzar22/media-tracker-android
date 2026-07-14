package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LibraryApiService {
    // GET /library/{mediaId} — is this item already in the user's library?
    // A 404 here is NORMAL ("not added yet"), so we return Response<LibraryItem> and
    // read the status code ourselves instead of letting a non-2xx throw.
    @GET("library/{mediaId}")
    suspend fun getLibraryItem(@Path("mediaId") mediaId: Int): Response<LibraryItem>

    // POST /library — add the item to the library (used by the "+ Want To" button).
    @POST("library")
    suspend fun addToLibrary(@Body body: AddLibraryItemRequest): Response<LibraryItem>
}
