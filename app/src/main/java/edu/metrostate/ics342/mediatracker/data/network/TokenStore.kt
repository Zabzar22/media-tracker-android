package edu.metrostate.ics342.mediatracker.data.network

// Simple in-memory holder for the auth tokens returned by POST /tokens.
// This is the "hand-off" point: a successful login writes the access token here
// so future authenticated requests can read it. (A later week swaps this for
// persistent storage via DataStore.)
object TokenStore {
    var accessToken: String? = null
    var refreshToken: String? = null
}
