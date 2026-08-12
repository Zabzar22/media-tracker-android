package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Favorite
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApiService {
    // GET /favorites/{mediaId} ; is this one already favorited?
    // a 404 just means "not yet", same as the library check, so we return
    // Response<Favorite> and read the code ourselves instead of letting it throw.
    @GET("favorites/{mediaId}")
    suspend fun getFavorite(@Path("mediaId") mediaId: Int): Response<Favorite>

    // GET /favorites ; everything the signed-in user has saved. used by the little
    // favorites list on the profile screen.
    @GET("favorites")
    suspend fun getFavorites(): Response<List<Favorite>>

    // POST /favorites ; what the "Save" button calls.
    @POST("favorites")
    suspend fun addFavorite(@Body body: AddFavoriteRequest): Response<Favorite>

    // DELETE /favorites/{mediaId} ; the other half of the toggle. safe to call twice,
    // the server answers the same either way, so there's no "wasn't saved" case.
    @DELETE("favorites/{mediaId}")
    suspend fun removeFavorite(@Path("mediaId") mediaId: Int): Response<Unit>
}
