package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable

// the body we send on POST /favorites; just the id, no status like the library has.
@Serializable
data class AddFavoriteRequest(
    val mediaId: Int
)
