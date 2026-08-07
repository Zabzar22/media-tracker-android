package edu.metrostate.ics342.mediatracker.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.creatorCredit
import edu.metrostate.ics342.mediatracker.data.model.iconRes
import edu.metrostate.ics342.mediatracker.ui.components.StarRatingRow

// The review form. Rate the thing, optionally say why, and POST /reviews. The rating is
// required and the text isn't, so the Save button stays disabled until a star is picked.
//
// Week 12: this is also the edit screen. If the view model finds a review you already
// wrote, the form opens with it filled in, the wording changes ("Edit Your Review" /
// "Save Changes"), a Delete button appears, and saving sends a PUT instead of a POST.
// The Edit button on Media Detail navigates here exactly the same way + Write Review does
// - there is nothing to pass along, because the screen works out for itself which one
// this is.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    mediaId: Int,
    onNavigateBack: () -> Unit,
    viewModel: WriteReviewViewModel = viewModel()
) {
    val media       by viewModel.media.collectAsState()
    val rating      by viewModel.rating.collectAsState()
    val reviewText  by viewModel.reviewText.collectAsState()
    val shareToFeed by viewModel.shareToFeed.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val editingReviewId by viewModel.editingReviewId.collectAsState()

    // One flag the whole screen reads off. An id to edit = we're editing; null = new review.
    val isEditing = editingReviewId != null

    // Same reason as the detail screen: in the body this would re-fetch on every
    // recomposition. Keyed on mediaId so it runs once per item.
    LaunchedEffect(mediaId) { viewModel.load(mediaId) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Whether the "are you sure?" dialog is up. It's remembered by the screen, not the view
    // model, because it's only ever about what's drawn - nothing is sent until it's confirmed.
    var showDeleteDialog by remember { mutableStateOf(false) }

    // The error text is looked up out here rather than inside the effect below. Pulling a
    // string out of LocalContext works, but it won't re-read if the device language changes
    // - stringResource does, which is why Compose flags the LocalContext version.
    val errorState   = submitState as? WriteReviewViewModel.SubmitState.Error
    val errorMessage = if (errorState != null) stringResource(errorState.messageRes) else null

    // Saving is async, so leaving the screen is driven by the *result*, not by the tap.
    // Doing this in the button's onClick would navigate back before the server had
    // answered, and a failed save would look like it worked. Deleting lands here too - once
    // the review is gone there's nothing left on this form to look at.
    LaunchedEffect(submitState) {
        if (submitState is WriteReviewViewModel.SubmitState.Success) onNavigateBack()
    }

    // Separate effect because it's keyed on the message instead of the whole state.
    // clearError() puts us back to Idle once it's shown, so the snackbar appears once and
    // the Save button becomes usable again for a retry.
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    val isSubmitting = submitState is WriteReviewViewModel.SubmitState.Submitting

    // Deleting can't be undone, so it asks first. Same AlertDialog the library screen uses
    // for changing status, just with the destructive action in red.
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.review_delete_confirm_title)) },
            text  = { Text(stringResource(R.string.review_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.delete() }) {
                    Text(stringResource(R.string.review_delete_confirm_button),
                        color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(stringResource(
                        if (isEditing) R.string.review_title_edit else R.string.review_title
                    ))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack,
                            stringResource(R.string.action_back))
                    }
                }
            )

            // Wait for the "have I already reviewed this?" answer before drawing anything.
            // Without this the stars show up empty for a moment and then fill themselves in,
            // which reads as the app losing the review you're trying to edit.
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Only drawn once GET /media/{id} lands. If that call failed we skip the
                // summary rather than showing an empty box — you came here to rate a thing
                // you were just looking at, so the form is still usable without it.
                media?.let { MediaSummary(it) }

                Spacer(Modifier.height(24.dp))
                SectionLabel(stringResource(R.string.review_rating_label))
                Spacer(Modifier.height(4.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StarRatingRow(
                        rating         = rating,
                        onRatingChange = viewModel::onRatingChange,
                        enabled        = !isSubmitting
                    )
                    Text(
                        text = if (rating == 0) stringResource(R.string.review_rating_hint)
                               else stringResource(R.string.review_rating_value, rating,
                                   WriteReviewViewModel.MAX_RATING),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel(stringResource(R.string.review_text_label))
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = reviewText,
                    onValueChange = viewModel::onReviewTextChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text(stringResource(R.string.review_text_placeholder)) },
                    minLines      = 5,
                    enabled       = !isSubmitting,
                    shape         = RoundedCornerShape(12.dp)
                )

                // The view model drops anything past the cap, so this can't be the number
                // that's wrong — it's reading the same string that would get sent.
                Text(
                    text = stringResource(R.string.review_text_counter,
                        reviewText.length, WriteReviewViewModel.MAX_REVIEW_LENGTH),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reviewText.length >= WriteReviewViewModel.MAX_REVIEW_LENGTH)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier  = Modifier.fillMaxWidth().padding(top = 4.dp)
                )

                // The server already defaults this to true, but the checkbox means the
                // request says what the user chose instead of what we happened to omit.
                //
                // Hidden while editing: PUT /reviews/{id} doesn't take shareToFeed. The feed
                // post was made when the review was first written, and an edit doesn't
                // re-share it - so a checkbox here would be a promise we can't keep.
                if (!isEditing) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = shareToFeed,
                            onCheckedChange = viewModel::onShareToFeedChange,
                            enabled         = !isSubmitting
                        )
                        Text(stringResource(R.string.review_share_to_feed),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick  = { viewModel.submit(mediaId) },
                    // Rating is the one required field, so no star means nothing to save.
                    enabled  = rating >= WriteReviewViewModel.MIN_RATING && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(
                        if (isEditing) R.string.review_save_button
                        else           R.string.review_post_button
                    ))
                }

                // Only there when there's something to delete. Text button in red rather
                // than a second filled button, so it doesn't compete with Save.
                if (isEditing) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick  = { showDeleteDialog = true },
                        enabled  = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.review_delete_button),
                            color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Cover thumbnail + title + credit, so you can see what you're rating. */
@Composable
private fun MediaSummary(media: Media) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (media.coverUrl != null) {
                AsyncImage(
                    model              = media.coverUrl,
                    contentDescription = media.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(media.mediaType.iconRes()),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(media.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Spacer(Modifier.height(2.dp))
            Text(media.creatorCredit(LocalContext.current),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Small uppercase section label — matches the ones on the detail screen. */
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
