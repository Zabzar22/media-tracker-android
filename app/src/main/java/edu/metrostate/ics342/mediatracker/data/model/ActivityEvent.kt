package edu.metrostate.ics342.mediatracker.data.model

import android.content.Context
import edu.metrostate.ics342.mediatracker.R

/**
 * A single event in the social activity feed.
 *
 * [activityType] is one of: "added", "started", "finished", "review"
 * [rating] and [reviewText] are only present when activityType == "review".
 */
data class ActivityEvent(
    val id: Int,
    val userId: String,
    val activityType: String,
    val mediaId: Int,
    val rating: Int? = null,
    val reviewText: String? = null,
    val createdAt: String,
    // Mock-friendly relative label ("2 hours ago"). Null when we're on the real API,
    // where the feed falls back to formatting createdAt instead.
    val timeAgo: String? = null,
    val user: UserProfile? = null,
    val media: Media? = null
)

fun ActivityEvent.descriptionText(context: Context): String {
    val name  = user?.displayName ?: context.getString(R.string.feed_user_someone)
    val title = media?.title      ?: context.getString(R.string.feed_media_something)
    return when (activityType) {
        "added"    -> context.getString(R.string.feed_event_added, name, title)
        "started"  -> context.getString(R.string.feed_event_started, name, title)
        "finished" -> context.getString(R.string.feed_event_finished, name, title)
        "review"   -> context.getString(R.string.feed_event_reviewed, name, title)
        else       -> context.getString(R.string.feed_event_unknown, name, title)
    }
}

/** Just the action part ("finished", "added to Want To", …) — the name is shown
 *  separately (bold) and the media title lives in the card's media box. */
fun ActivityEvent.actionPhrase(context: Context): String = when (activityType) {
    "added"    -> context.getString(R.string.feed_header_added)
    "started"  -> context.getString(R.string.feed_header_started)
    "finished" -> when (media?.mediaType) {
        "book" -> context.getString(R.string.feed_header_finished_reading)
        else   -> context.getString(R.string.feed_header_finished_watching)
    }
    "review"   -> context.getString(R.string.feed_header_reviewed)
    else       -> context.getString(R.string.feed_header_updated)
}
