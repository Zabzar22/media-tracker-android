package edu.metrostate.ics342.mediatracker.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit

@Composable
fun MediaTypeFilterChips(
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        "" to R.string.filter_all,
        "book" to R.string.filter_books,
        "movie" to R.string.filter_movies,
        "show" to R.string.filter_shows,
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, labelRes) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelect(type) },
                label = { Text(stringResource(labelRes)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun MediaResultCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val containerColor = when (media.mediaType) {
                "book"  -> MaterialTheme.colorScheme.primaryContainer
                "movie" -> MaterialTheme.colorScheme.secondaryContainer
                else    -> MaterialTheme.colorScheme.tertiaryContainer
            }
            val iconTint = when (media.mediaType) {
                "book"  -> MaterialTheme.colorScheme.onPrimaryContainer
                "movie" -> MaterialTheme.colorScheme.onSecondaryContainer
                else    -> MaterialTheme.colorScheme.tertiary
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(when (media.mediaType) {
                        "book"  -> R.drawable.menu_book_24px
                        "movie" -> R.drawable.movie_24px
                        else    -> R.drawable.tv_24px
                    }),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconTint
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = media.creatorCredit(LocalContext.current),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Only the star + rating gets the amber accent — and only when the
                    // item actually has a rating (the live catalog is unrated so far).
                    if (media.averageRating > 0f) {
                        Text(
                            text       = "★ ${"%.1f".format(media.averageRating)}",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Text(
                        text = buildString {
                            if (media.averageRating > 0f) append(" · ")
                            append(media.mediaType.replaceFirstChar { it.uppercase() })
                            media.publishedYear?.let { append(" · $it") }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
