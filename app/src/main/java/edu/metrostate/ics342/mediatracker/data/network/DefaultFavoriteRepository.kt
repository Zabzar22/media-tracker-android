package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Favorite
import retrofit2.HttpException

class DefaultFavoriteRepository(
    private val api: FavoriteApiService = RetrofitInstance.favoriteApiService
) {
    // the whole saved list. an empty library is a normal 200 with an empty array, so
    // there's no 404 case here like there is on the single check below.
    suspend fun getFavorites(): List<Favorite> {
        val response = api.getFavorites()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // is this favorited? gives back the favorite, or null if it isn't.
    // same rule as the library check ; a 404 is a normal answer, not a failure.
    suspend fun getFavorite(mediaId: Int): Favorite? {
        val response = api.getFavorite(mediaId)
        return when {
            response.isSuccessful  -> response.body()
            response.code() == 404 -> null
            else                   -> throw HttpException(response)
        }
    }

    // save it. a 409 means it was already saved, which is what we wanted anyway,
    // so we let that one through instead of throwing.
    suspend fun addFavorite(mediaId: Int) {
        val response = api.addFavorite(AddFavoriteRequest(mediaId))
        if (!response.isSuccessful && response.code() != 409) {
            throw HttpException(response)
        }
    }

    // take it back out. no special cases here ; the server is fine with being told to
    // remove something that isn't there.
    suspend fun removeFavorite(mediaId: Int) {
        val response = api.removeFavorite(mediaId)
        if (!response.isSuccessful) throw HttpException(response)
    }
}
