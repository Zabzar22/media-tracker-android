package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.data.model.typeStat

// Week 7: Media Detail layout. No network yet — one item looked up from the fake
// repository by mediaId. GET /media/{id} and GET /reviews come in a later week.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    onWriteReview: (Int) -> Unit
) {
    val media = remember(mediaId) { FakeMediaRepository.mediaList.find { it.id == mediaId } }

    // A couple of fake reviews so the list has something to show tonight.
    val reviews = remember(mediaId) { sampleReviews(mediaId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack,
                        stringResource(R.string.action_back))
                }
            },
            actions = {
                IconButton(onClick = { /* Week 8+: overflow menu actions */ }) {
                    Icon(Icons.Outlined.MoreVert,
                        stringResource(R.string.action_more_options))
                }
            }
        )

        if (media == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.detail_not_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cover — placeholder icon box unless a real coverUrl exists.
            CoverArt(media)

            Spacer(Modifier.height(16.dp))
            Text(media.title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(media.creatorCredit(LocalContext.current),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Rating row: stars + numeric average + count.
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRow(media.averageRating)
                Spacer(Modifier.width(6.dp))
                Text("%.1f".format(media.averageRating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.detail_rating_count,
                    "%,d".format(media.ratingCount)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Two action buttons — no-ops tonight.
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { /* Week 9: POST /library want_to */ },
                    modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.detail_want_to))
                }
                OutlinedButton(onClick = { /* Week 9: save */ },
                    modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.detail_save))
                }
            }

            // About
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.detail_about))
            Spacer(Modifier.height(8.dp))
            Text(media.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth())

            // 3-box stat grid — middle box changes per media type.
            Spacer(Modifier.height(16.dp))
            val (statLabel, statValue) = media.typeStat(LocalContext.current)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(stringResource(R.string.detail_stat_year),
                    media.publishedYear?.toString()
                        ?: stringResource(R.string.detail_stat_unknown),
                    Modifier.weight(1f))
                StatBox(statLabel, statValue, Modifier.weight(1f))
                StatBox(stringResource(R.string.detail_stat_genre),
                    media.genres.firstOrNull()
                        ?: stringResource(R.string.detail_stat_unknown),
                    Modifier.weight(1f))
            }

            // Reviews header
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.detail_reviews, reviews.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                TextButton(onClick = { onWriteReview(mediaId) }) {
                    Text(stringResource(R.string.detail_write_review))
                }
            }

            Spacer(Modifier.height(8.dp))
            reviews.forEach { review ->
                ReviewCard(review)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Large centered cover: real image if we have one, otherwise the type-icon box. */
@Composable
private fun CoverArt(media: Media) {
    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 190.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (media.coverUrl != null) {
            AsyncImage(model = media.coverUrl, contentDescription = media.title,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(
                painter = painterResource(when (media.mediaType) {
                    "book"  -> R.drawable.menu_book_24px
                    "movie" -> R.drawable.movie_24px
                    else    -> R.drawable.tv_24px
                }),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** Row of five amber stars. A star fills at .6 and above (4.6 -> 5, 4.5/4.4 -> 4). */
@Composable
private fun StarRow(rating: Float) {
    // Work in whole tenths first so float rounding (4.6 is really 4.5999…) can't
    // knock a rating down a star. Then fill the next star only when the tenths
    // digit is 6 or higher.
    val tenths = (rating * 10).roundToInt()
    val filled = (tenths / 10 + if (tenths % 10 >= 6) 1 else 0).coerceIn(0, 5)
    Row {
        for (index in 0 until 5) {
            Icon(
                imageVector = if (index < filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** Small uppercase section label, left aligned. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

/** One outlined box in the 3-box stat grid: uppercase label over a bold value. */
@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
    }
}

/** A single hardcoded review: avatar, username, timestamp, stars, text. */
@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            val name = review.user?.displayName ?: review.user?.username ?: "?"
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("@${review.user?.username ?: "user"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold)
                    Text(review.createdAt, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                StarRow(review.rating.toFloat())
                if (!review.reviewText.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(review.reviewText, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Fake reviews for tonight. Week 8+: replace with GET /reviews?mediaId={id}. */
private fun sampleReviews(mediaId: Int): List<Review> {
    val alice = UserProfile("user-101", "alice@example.com", "alice_reads", "Alice")
    val marco = UserProfile("user-102", "marco@example.com", "marco_m", "Marco")
    return listOf(
        Review(userId = "user-101", mediaId = mediaId, rating = 5,
            reviewText = "A timeless classic. Fresh every time.",
            createdAt = "2d ago", user = alice),
        Review(userId = "user-102", mediaId = mediaId, rating = 4,
            reviewText = "Slow to start but absolutely worth it by the end.",
            createdAt = "1w ago", user = marco),
    )
}
