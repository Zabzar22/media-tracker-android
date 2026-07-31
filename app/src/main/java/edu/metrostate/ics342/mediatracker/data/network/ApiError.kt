package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable
import retrofit2.Response

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

// POST /reviews answers 409 when you've already reviewed that item. That's the one-review-
// per-item rule doing its job, not something breaking, so it gets its own type and the
// screen can say so instead of falling back to the generic "something went wrong".
class AlreadyReviewedException(message: String) : Exception(message)

// The server explains itself in the error body ({ "message": ... }) and that wording is
// written to be read by users, so prefer it over a bare status code. Retrofit only parses
// successful bodies for us, so we decode this one by hand.
//
// This used to be a private helper inside DefaultMediaRepository. It's here now because
// every repository that wants to pass the server's wording along needs it. Returns null
// if the body isn't what we expect, and the caller falls back to its own wording.
internal fun Response<*>.errorMessage(): String? = try {
    errorBody()?.string()?.let {
        RetrofitInstance.json.decodeFromString<ErrorResponse>(it).message
    }
} catch (e: Exception) {
    null
}
