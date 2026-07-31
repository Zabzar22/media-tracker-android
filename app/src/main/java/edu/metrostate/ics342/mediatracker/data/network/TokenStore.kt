package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.UserProfile

// Simple in-memory holder for the signed-in session: the tokens from POST /tokens, plus
// the user profile that same response hands back.
//
// This is the "hand-off" point: a successful login writes the access token here so future
// authenticated requests can read it.
//
// On purpose, none of this is saved to disk. We each test with our own account, so every
// run starting at the login screen is the behaviour we want - it makes switching accounts
// as easy as restarting the app.
object TokenStore {
    var accessToken: String? = null
    var refreshToken: String? = null

    // Set by DefaultUserRepository.login() straight from the POST /tokens response, so it's
    // already here before any screen needs it - no extra request. Lives next to the tokens
    // because it belongs to the same login: signing out has to forget both, or the next
    // person to log in would inherit this one's identity.
    var currentUser: UserProfile? = null

    /** The signed-in user's id, or null when nobody is signed in. */
    val currentUserId: String? get() = currentUser?.id

    // Called on sign out. Without this the old token and profile would sit here until the
    // next login overwrote them, and "is this my review?" would answer for the wrong person.
    fun clear() {
        accessToken = null
        refreshToken = null
        currentUser = null
    }
}
