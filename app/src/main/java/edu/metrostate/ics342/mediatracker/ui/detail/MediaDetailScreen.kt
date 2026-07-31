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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.data.model.iconRes
import edu.metrostate.ics342.mediatracker.data.model.typeStat
import edu.metrostate.ics342.mediatracker.ui.components.StarRatingDisplay

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
    val actionError by viewModel.actionError.collectAsState()

    // Only want to load once when the screen opens (or if the id changes). Putting the
    // call in LaunchedEffect instead of straight in the body stops it from re-running
    // GET /media/{id} on every recomposition, which was making it loop.
    LaunchedEffect(mediaId) { viewModel.load(mediaId) }

    // Coming back from Write Review lands us on this screen again, but the LaunchedEffect
    // above does NOT run a second time - this screen never left the back stack, so as far
    // as Compose is concerned mediaId never changed. Without this line a review you just
    // posted wouldn't show up until you left the screen and came back.
    // ON_RESUME fires every time the screen comes to the front, and refresh() quietly does
    // nothing if the page hasn't loaded yet, so it can't double up on the way in.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh(mediaId) }

    // a button that flipped back gets a snackbar down here, so the page stays up.
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    currentUserId = state.currentUserId,
                    isFavorite = state.isFavorite,
                    onAddToLibrary = { viewModel.addToWantTo(mediaId) },
                    onToggleFavorite = { viewModel.toggleFavorite(mediaId) },
                    onWriteReview = onWriteReview
                )
        }
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
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
    currentUserId: String?,
    isFavorite: Boolean,
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
            StarRatingDisplay(media.averageRating)
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
        // the people screen.
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (libraryStatus == null) {
                // not added yet ; filled purple button that does the adding. one tap swaps
                // it for the outlined one below, so there's nothing to disable.
                Button(onClick = onAddToLibrary,
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
                // the view model already moved ours to the front ; this just says which
                // one it is. next week this is where the Edit button goes.
                ReviewCard(review, isMine = review.userId == currentUserId)
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
private fun ReviewCard(review: Review, isMine: Boolean = false) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("@${review.user?.username ?: "user"}",
                            style = MaterialTheme.typography.titleSmall)
                        if (isMine) {
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.detail_your_review),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    // createdAt is a full ISO timestamp now that it's real data
                    // ("2026-07-14T18:22:05Z") - take(10) keeps just the date part.
                    Text(review.createdAt.take(10), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                StarRatingDisplay(review.rating.toFloat())
                if (!review.reviewText.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(review.reviewText, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
