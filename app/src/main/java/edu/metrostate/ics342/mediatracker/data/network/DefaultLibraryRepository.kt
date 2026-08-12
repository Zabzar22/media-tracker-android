package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import retrofit2.HttpException

class DefaultLibraryRepository(
    private val api: LibraryApiService = RetrofitInstance.libraryApiService
) {
    // the list behind the My Library screen. toApiString() turns WANT_TO into "want_to",
    // which is the wording the query parameter expects.
    suspend fun getLibrary(status: LibraryStatus): List<LibraryItem> {
        val response = api.getLibrary(status.toApiString())
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // Is this item in the user's library? Returns the item, or null if it isn't.
    // A 404 is normal here ("not added yet") so we do NOT throw for it - we read the
    // status code. Anything else that isn't a success (500, 401, ...) is a real error.
    suspend fun getLibraryItem(mediaId: Int): LibraryItem? {
        val response = api.getLibraryItem(mediaId)
        return when {
            response.isSuccessful  -> response.body()
            response.code() == 404 -> null
            else                   -> throw HttpException(response)
        }
    }

    // change an item's status. the server sends the updated item back, but the screen
    // reloads the whole tab afterwards so we don't need it.
    suspend fun updateStatus(mediaId: Int, status: LibraryStatus) {
        val response = api.updateLibraryStatus(mediaId, UpdateLibraryStatusRequest(status))
        if (!response.isSuccessful) throw HttpException(response)
    }

    // take the item out of the library.
    suspend fun removeFromLibrary(mediaId: Int) {
        val response = api.removeFromLibrary(mediaId)
        if (!response.isSuccessful) throw HttpException(response)
    }

    // add the item to the library. a 409 means it's already in there, which is what we
    // wanted anyway, so we let that one through like favorites does. nothing is returned
    // because the button already flipped itself before this ran.
    suspend fun addToLibrary(mediaId: Int, status: LibraryStatus) {
        val response = api.addToLibrary(AddLibraryItemRequest(mediaId, status))
        if (!response.isSuccessful && response.code() != 409) {
            throw HttpException(response)
        }
    }
}
