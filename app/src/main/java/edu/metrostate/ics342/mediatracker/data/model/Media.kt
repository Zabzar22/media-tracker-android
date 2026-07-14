package edu.metrostate.ics342.mediatracker.data.model

import android.content.Context
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.Serializable

// GET /media (search) leaves the bottom fields empty - only GET /media/{id} fills them
// in. That's why the detail screen re-fetches by id instead of reusing the search result.
@Serializable
data class Media(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val author: String? = null,       // books
    val director: String? = null,     // movies
    val creator: String? = null,      // shows
    val network: String? = null,      // shows (streaming / broadcast platform)
    val coverUrl: String? = null,
    val publishedYear: Int? = null,
    val averageRating: Float = 0f,
    val ratingCount: Int = 0,
    val genres: List<String> = emptyList(),
    // ---- only filled in by GET /media/{id} ----
    val description: String? = null,
    val pageCount: Int? = null,       // books
    val runtimeMinutes: Int? = null,  // movies
    val seasonCount: Int? = null,     // shows
    val episodeCount: Int? = null,    // shows
    val isbn: String? = null,         // books
    val reviewCount: Int = 0
)

/** Returns a human-readable credit line appropriate for the media type. */
fun Media.creatorCredit(context: Context): String = when (mediaType) {
    MediaType.BOOK  -> author   ?: context.getString(R.string.media_unknown_author)
    MediaType.MOVIE -> director ?: context.getString(R.string.media_unknown_director)
    MediaType.SHOW  -> creator  ?: context.getString(R.string.media_unknown_creator)
}

/**
 * The middle stat box on the detail screen changes per media type:
 * books show page count, movies show runtime, shows show episode count.
 * Returns an (uppercase label, value) pair; value is "—" when unknown.
 */
fun Media.typeStat(context: Context): Pair<String, String> = when (mediaType) {
    MediaType.BOOK  -> context.getString(R.string.detail_stat_pages) to
        (pageCount?.toString() ?: context.getString(R.string.detail_stat_unknown))
    MediaType.MOVIE -> context.getString(R.string.detail_stat_runtime) to
        (runtimeMinutes?.let { context.getString(R.string.detail_stat_runtime_value, it) }
            ?: context.getString(R.string.detail_stat_unknown))
    MediaType.SHOW  -> context.getString(R.string.detail_stat_seasons_eps) to
        (if (seasonCount != null && episodeCount != null)
            context.getString(R.string.detail_stat_seasons_eps_value, seasonCount, episodeCount)
        else context.getString(R.string.detail_stat_unknown))
}
