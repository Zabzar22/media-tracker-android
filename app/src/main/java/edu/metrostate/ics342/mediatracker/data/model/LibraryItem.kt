package edu.metrostate.ics342.mediatracker.data.model

import androidx.annotation.StringRes
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LibraryItem(
    val userId: String,
    val mediaId: Int,
    val status: LibraryStatus,
    val addedAt: String,
    val updatedAt: String,
    val media: Media
)

// @Serializable + @SerialName so the JSON parser can match the server's strings
// ("want_to") to our enum values (WANT_TO). Without these the LibraryItem wouldn't parse.
@Serializable
enum class LibraryStatus(@param:StringRes val labelRes: Int) {
    @SerialName("want_to")     WANT_TO(R.string.status_want_to),
    @SerialName("in_progress") IN_PROGRESS(R.string.status_in_progress),
    @SerialName("finished")    FINISHED(R.string.status_finished);

    fun toApiString(): String = when (this) {
        WANT_TO     -> "want_to"
        IN_PROGRESS -> "in_progress"
        FINISHED    -> "finished"
    }

    companion object {
        fun fromString(value: String): LibraryStatus = when (value) {
            "want_to"     -> WANT_TO
            "in_progress" -> IN_PROGRESS
            "finished"    -> FINISHED
            else          -> WANT_TO
        }
    }
}
