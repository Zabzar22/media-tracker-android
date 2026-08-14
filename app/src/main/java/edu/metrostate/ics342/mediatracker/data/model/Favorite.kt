package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

// what the favorites endpoints send back; same as LibraryItem but with no status.
// media isn't always included, so it's optional.
@Serializable
data class Favorite(
    val userId: String,
    val mediaId: Int,
    val createdAt: String = "",
    val media: Media? = null
)
