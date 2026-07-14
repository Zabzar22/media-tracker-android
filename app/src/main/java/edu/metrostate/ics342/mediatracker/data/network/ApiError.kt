package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable

// The body the API sends on an error. Checked the spec and the server's errorResponse()
// helper - it's only ever { "message": "..." }, so that's all I'm modelling. Nullable
// so an unexpected/empty body still parses while we're already handling an error.
@Serializable
data class ErrorResponse(
    val message: String? = null
)

// Its own type so the screen can say "Media not found." for a 404 on GET /media/{id}
// (a normal thing - stale id) instead of the generic error.
class MediaNotFoundException(message: String) : Exception(message)
