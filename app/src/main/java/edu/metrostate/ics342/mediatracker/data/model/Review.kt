package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

// Now comes from GET /reviews instead of being hardcoded, so it needs @Serializable.
// createdAt is an ISO timestamp from the server, not the "2d ago" text I used to fake.
@Serializable
data class Review(
    val id: Int = 0,
    val userId: String,
    val mediaId: Int,
    val rating: Int,
    val reviewText: String? = null,
    val createdAt: String,
    val user: UserProfile? = null,
    val media: Media? = null
)
