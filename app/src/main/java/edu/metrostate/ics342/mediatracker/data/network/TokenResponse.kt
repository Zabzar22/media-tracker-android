package edu.metrostate.ics342.mediatracker.data.network

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
