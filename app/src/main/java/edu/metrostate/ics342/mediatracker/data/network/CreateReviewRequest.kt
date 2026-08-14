package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable

// The body we send on POST /reviews, same shape as AddLibraryItemRequest next door.
//
// reviewText is optional — a rating on its own is a valid review — and a blank box is sent
// as null rather than "", so the server stores "no text" instead of an empty string.
//
// shareToFeed already defaults to true on the server if we leave it off. We send it anyway
// because the form has a checkbox for it, and sending it explicitly means the request says
// what the user actually chose instead of relying on a default we don't control.
@Serializable
data class CreateReviewRequest(
    val mediaId: Int,
    val rating: Int,
    val reviewText: String? = null,
    val shareToFeed: Boolean = true
)
