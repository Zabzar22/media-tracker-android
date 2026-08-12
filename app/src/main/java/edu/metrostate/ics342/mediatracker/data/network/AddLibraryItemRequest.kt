package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import kotlinx.serialization.Serializable

// The body we send on POST /library. The spec wants { mediaId, status }.
// status serializes to "want_to" / "in_progress" / "finished" via @SerialName on the enum.
@Serializable
data class AddLibraryItemRequest(
    val mediaId: Int,
    val status: LibraryStatus
)
