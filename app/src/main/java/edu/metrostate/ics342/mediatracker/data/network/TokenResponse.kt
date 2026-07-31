package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import kotlinx.serialization.Serializable

// What POST /tokens sends back. The two tokens are what we came for, but the server also
// hands us the signed-in user's profile in the same response - so we never have to ask a
// second time who just logged in.
@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile
)
