package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import kotlinx.serialization.Serializable

// the body we send on PUT /library/{mediaId}. just the new status ; the mediaId is
// already in the url so it doesn't go in here.
@Serializable
data class UpdateLibraryStatusRequest(
    val status: LibraryStatus
)
