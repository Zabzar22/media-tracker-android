package edu.metrostate.ics342.mediatracker.data.model

import androidx.annotation.DrawableRes
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Was a plain String on Media ("book" / "movie" / "show"). Made it an enum so a typo
// is a build error instead of quietly falling into an else branch. @SerialName maps
// the JSON strings to the constants, same as LibraryStatus does.
@Serializable
enum class MediaType(val apiString: String) {
    @SerialName("book")  BOOK("book"),
    @SerialName("movie") MOVIE("movie"),
    @SerialName("show")  SHOW("show");

    /** "book" -> "Book", for the search card subtitle. */
    val displayName: String get() = apiString.replaceFirstChar { it.uppercase() }
}

/** Stand-in icon when there's no cover image. Nullable because the feed's media can be null. */
@DrawableRes
fun MediaType?.iconRes(): Int = when (this) {
    MediaType.BOOK  -> R.drawable.menu_book_24px
    MediaType.MOVIE -> R.drawable.movie_24px
    MediaType.SHOW  -> R.drawable.tv_24px
    null            -> R.drawable.tv_24px
}
