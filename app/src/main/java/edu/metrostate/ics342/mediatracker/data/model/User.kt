package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

// @Serializable because a Review has the reviewer's profile nested inside it.
@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val trackedCount: Int = 0,
    /** Whether the currently authenticated user follows this profile. Null on /users/me. */
    val isFollowing: Boolean? = null,
    val createdAt: String? = null
)
