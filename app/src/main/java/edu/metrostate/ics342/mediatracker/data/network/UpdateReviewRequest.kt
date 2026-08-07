package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable

// The body we send on PUT /reviews/{id}. Only the two things you can actually change —
// the id goes in the URL, and mediaId can't move, so neither belongs in here.
//
// Note there are no default values, unlike CreateReviewRequest. That's deliberate: the
// server treats a missing field as "leave it alone", so if reviewText defaulted to null
// the serializer could drop it and clearing your text would silently do nothing. With no
// default it's always written out, and `"reviewText": null` is what wipes the old text.
//
// shareToFeed isn't here because PUT doesn't take it — the feed post was made when you
// first reviewed, and editing doesn't re-share.
@Serializable
data class UpdateReviewRequest(
    val rating: Int,
    val reviewText: String?
)
