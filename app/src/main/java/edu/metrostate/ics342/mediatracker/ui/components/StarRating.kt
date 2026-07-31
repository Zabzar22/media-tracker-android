package edu.metrostate.ics342.mediatracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.R
import kotlin.math.roundToInt

// Both star rows live here so there's one place to change how a star looks. The tappable
// one is new for the review form ; the read-only one moved out of MediaDetailScreen, where
// it was private and therefore no use to anything else.

/**
 * Five tappable stars for picking a rating.
 *
 * Tapping star N sets the rating to N. It's not five checkboxes, and it's not something
 * that only goes up — tapping star 2 after star 5 leaves you on 2. That's why [onRatingChange]
 * is just handed the star's number, with nothing compared against the current rating first.
 *
 * @param rating what's picked now; 0 means nothing picked yet.
 * @param onRatingChange gets the number of the star that was tapped, 1..[starCount].
 */
@Composable
fun StarRatingRow(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starSize: Dp = 36.dp,
    enabled: Boolean = true
) {
    Row(modifier = modifier) {
        for (star in 1..starCount) {
            val filled = star <= rating
            IconButton(
                onClick = { onRatingChange(star) },
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    // Reads as "3 star, button" to TalkBack, so you can tell which one
                    // you're on without seeing it.
                    contentDescription = stringResource(R.string.review_star_description, star),
                    tint = if (filled) MaterialTheme.colorScheme.tertiary
                           else        MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

/**
 * Read-only stars for an average or a posted rating. A star fills at .6 and above
 * (4.6 -> 5, 4.5/4.4 -> 4).
 */
@Composable
fun StarRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    starSize: Dp = 16.dp
) {
    // Round to the nearest half-star so 4.5 shows four full stars + one half, instead of
    // being floored to four. Working in half-steps (0..10) also absorbs float noise like
    // 4.4999. Each position is full, half, or empty.
    val halves = (rating * 2).roundToInt().coerceIn(0, starCount * 2)
    Row(modifier = modifier) {
        for (index in 0 until starCount) {
            val star = when {
                halves >= (index + 1) * 2 -> Icons.Filled.Star          // full star
                // AutoMirrored so the half fills from the correct side in a right-to-left
                // language ; the plain Icons.Filled.StarHalf is deprecated for that reason.
                halves >= index * 2 + 1   -> Icons.AutoMirrored.Filled.StarHalf
                else                      -> Icons.Outlined.StarBorder  // empty star
            }
            Icon(
                imageVector = star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(starSize)
            )
        }
    }
}
