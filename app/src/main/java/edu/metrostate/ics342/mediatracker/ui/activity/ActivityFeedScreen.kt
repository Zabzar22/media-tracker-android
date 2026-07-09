package edu.metrostate.ics342.mediatracker.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.ActivityEvent
import edu.metrostate.ics342.mediatracker.data.model.actionPhrase
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.theme.avatarColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityFeedScreen(
    onMediaClick: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ActivityFeedViewModel = viewModel()
) {
    val feedItems by viewModel.feedItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Compact grey header (surfaceVariant = the same tint behind the feed's book/movie
        // tiles). statusBarsPadding keeps the content clear of the status bar while the grey
        // fills behind it; the small vertical padding makes it a touch shorter than a TopAppBar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            // Placeholder avatar for the logged-in user (fake data = Alex Chen).
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "A",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            feedItems.forEach { event ->
                ActivityCard(
                    event        = event,
                    onMediaClick = { event.media?.id?.let(onMediaClick) },
                    onUserClick  = { event.user?.id?.let(onUserClick) }
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    event: ActivityEvent,
    onMediaClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
    val name    = event.user?.displayName ?: stringResource(R.string.feed_user_someone)
    val action  = event.actionPhrase(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Header — avatar + "Name action"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onUserClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (event.user?.avatarUrl != null) {
                        AsyncImage(
                            model              = event.user.avatarUrl,
                            contentDescription = event.user.displayName,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Surface(
                            color    = avatarColor(event.user?.id ?: name),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    name.trim().take(1).uppercase().ifEmpty { "?" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(name.substringBefore(" ")) }
                        append(" ")
                        append(action)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Nested media box — colored cover tile + title / rating / credit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onMediaClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val containerColor = when (event.media?.mediaType) {
                    "book"  -> MaterialTheme.colorScheme.primaryContainer
                    "movie" -> MaterialTheme.colorScheme.secondaryContainer
                    else    -> MaterialTheme.colorScheme.tertiaryContainer
                }
                val iconTint = when (event.media?.mediaType) {
                    "book"  -> MaterialTheme.colorScheme.onPrimaryContainer
                    "movie" -> MaterialTheme.colorScheme.onSecondaryContainer
                    else    -> MaterialTheme.colorScheme.tertiary
                }

                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (event.media?.coverUrl != null) {
                        AsyncImage(
                            model              = event.media.coverUrl,
                            contentDescription = event.media.title,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(
                                when (event.media?.mediaType) {
                                    "book"  -> R.drawable.menu_book_24px
                                    "movie" -> R.drawable.movie_24px
                                    else    -> R.drawable.tv_24px
                                }
                            ),
                            contentDescription = null,
                            tint     = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.media?.title ?: stringResource(R.string.feed_media_something),
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (event.activityType == "review" && event.rating != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "★".repeat(event.rating) + "☆".repeat(5 - event.rating),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!event.reviewText.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "\"${event.reviewText}\"",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    } else if (event.activityType == "finished" && event.media != null) {
                        // Finished items show the title's rating stars too — not just reviews.
                        val stars = event.media.averageRating.roundToInt().coerceIn(0, 5)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "★".repeat(stars) + "☆".repeat(5 - stars),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (event.media != null) {
                        Text(
                            event.media.creatorCredit(context),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                event.timeAgo ?: event.createdAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
