package edu.metrostate.ics342.mediatracker.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlin.math.roundToInt
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.data.model.iconRes
import edu.metrostate.ics342.mediatracker.data.model.typeStat

// Week 8 - hooked this up to the real API. The view model fetches the item, its library
// status and its reviews instead of reading the fake repo. I kept the top bar outside the
// when() so the back button still works while it's loading / erroring.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    onWriteReview: (Int) -> Unit,
    viewModel: MediaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Only want to load once when the screen opens (or if the id changes). Putting the
    // call in LaunchedEffect instead of straight in the body stops it from re-running
    // GET /media/{id} on every recomposition, which was making it loop.
    LaunchedEffect(mediaId) { viewModel.load(mediaId) }

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

        when (val state = uiState) {
            is MediaDetailUiState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            // No such id — nothing to retry, so this one just explains itself.
            is MediaDetailUiState.NotFound ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.detail_not_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            is MediaDetailUiState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Show what the server said if it said anything useful; my own
                        // wording covers the cases where it didn't (e.g. no connection).
                        Text(state.message.ifBlank { stringResource(R.string.detail_error) },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.load(mediaId) }) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }

            is MediaDetailUiState.Success ->
                MediaDetailContent(
                    media = state.media,
                    mediaId = mediaId,
                    libraryStatus = state.libraryStatus,
                    reviews = state.reviews,
                    isUpdatingLibrary = state.isUpdatingLibrary,
                    isFavorite = state.isFavorite,
                    isUpdatingFavorite = state.isUpdatingFavorite,
                    onAddToLibrary = { viewModel.addToWantTo(mediaId) },
                    onToggleFavorite = { viewModel.toggleFavorite(mediaId) },
                    onWriteReview = onWriteReview
                )
        }
    }
}

// Pulled the whole success layout out into its own composable so the when() branch
// above stays readable. It's the same layout from Week 7, just given the real Media now.
@Composable
private fun MediaDetailContent(
    media: Media,
    mediaId: Int,
    libraryStatus: LibraryStatus?,
    reviews: List<Review>,
    isUpdatingLibrary: Boolean,
    isFavorite: Boolean,
    isUpdatingFavorite: Boolean,
    onAddToLibrary: () -> Unit,
    onToggleFavorite: () -> Unit,
    onWriteReview: (Int) -> Unit
) {
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
            textAlign = TextAlign.Center)
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
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.detail_rating_count,
                "%,d".format(media.ratingCount)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // "+ Want To" adds the item (POST /library). two different buttons depending on
        // whether it's in the library yet, the same way the follow button works over on
        // the people screen ; changing status once it's in there is next week.
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (libraryStatus == null) {
                // not added yet ; filled purple button that does the adding.
                // enabled is off while the request runs so it can't be tapped twice.
                Button(onClick = onAddToLibrary,
                    enabled = !isUpdatingLibrary,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)) {
                    Text(stringResource(R.string.detail_want_to))
                }
            } else {
                // already in the library ; matches the saved button next to it, white
                // with purple text, and there's nothing left to tap.
                OutlinedButton(onClick = { },
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        disabledContentColor = MaterialTheme.colorScheme.primary
                    ),
                    // a disabled button fades its own border until you can't see it,
                    // so we hand it one ; BorderStroke is just thickness and color.
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Text(stringResource(libraryStatus.labelRes))
                }
            }
            // the "Save" button toggles the item in and out of favorites. the heart fills
            // in once it's saved, and tapping again takes it back out ; it's the only
            // place in the app that can un-save something.
            OutlinedButton(onClick = onToggleFavorite,
                enabled = !isUpdatingFavorite,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                // keeps the text and heart purple ; the default disabled color would
                // grey them out once it's saved.
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor         = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.primary
                ),
                // grey outline while it can still be tapped, like the wireframe shows,
                // then purple once it's saved so it matches the button beside it.
                border = BorderStroke(
                    1.dp,
                    if (isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isFavorite) stringResource(R.string.detail_saved)
                    else stringResource(R.string.detail_save)
                )
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

        // Reviews header. Counts with the server's reviewCount, not reviews.size — the
        // list only holds the first 20, so the size would under-report a popular item.
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.detail_reviews, media.reviewCount),
                style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { onWriteReview(mediaId) }) {
                Text(stringResource(R.string.detail_write_review))
            }
        }

        Spacer(Modifier.height(8.dp))
        if (reviews.isEmpty()) {
            Text(stringResource(R.string.detail_no_reviews),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        } else {
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
                painter = painterResource(media.mediaType.iconRes()),
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
    // Round to the nearest half-star so 4.5 shows four full stars + one half,
    // instead of being floored to four. Working in half-steps (0..10) also absorbs
    // float noise like 4.4999. Each position is full, half, or empty.
    val halves = (rating * 2).roundToInt().coerceIn(0, 10)
    Row {
        for (index in 0 until 5) {
            val star = when {
                halves >= (index + 1) * 2 -> Icons.Filled.Star          // full star
                halves >= index * 2 + 1   -> Icons.Filled.StarHalf      // half star
                else                      -> Icons.Outlined.StarBorder  // empty star
            }
            Icon(
                imageVector = star,
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
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** A single review: avatar, username, timestamp, stars, text. */
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
                        style = MaterialTheme.typography.titleSmall)
                    // createdAt is a full ISO timestamp now that it's real data
                    // ("2026-07-14T18:22:05Z") - take(10) keeps just the date part.
                    Text(review.createdAt.take(10), style = MaterialTheme.typography.labelSmall,
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
