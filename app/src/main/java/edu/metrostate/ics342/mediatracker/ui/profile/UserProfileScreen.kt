package edu.metrostate.ics342.mediatracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onMediaClick: (Int) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // Week 10: Replace this with a real GET /users/{id} call from the ViewModel
    val user: UserProfile? = remember(userId) { viewModel.loadUserById(userId) }
        ?: FakeMediaRepository.followers.find { it.id == userId }
        ?: FakeMediaRepository.following.find { it.id == userId }

    var isFollowing by remember { mutableStateOf(user?.isFollowing ?: false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(user?.displayName ?: stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(edu.metrostate.ics342.mediatracker.R.string.action_back))
                }
            }
        )

        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_user_not_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl != null) {
                    AsyncImage(model = user.avatarUrl, contentDescription = user.displayName,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.displayName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(user.displayName, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("@${user.username}", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (!user.bio.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(user.bio, style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(user.followerCount.toString(), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_followers), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(user.followingCount.toString(), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_following), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(user.trackedCount.toString(), style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_tracked), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Follow / Following button
            // Week 11: Wire to POST /followers/{userId} and DELETE /followers/{userId}
            if (isFollowing) {
                OutlinedButton(
                    onClick  = { isFollowing = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.action_following)) }
            } else {
                Button(
                    onClick  = { isFollowing = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.action_follow)) }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_recently_tracked), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start))

            Spacer(Modifier.height(8.dp))

            // Placeholder — Week 10: replace with GET /users/{id}/library
            FakeMediaRepository.libraryItems.take(3).forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp, 56.dp).clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxSize()) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(when (item.media.mediaType) {
                                        "book"  -> edu.metrostate.ics342.mediatracker.R.drawable.menu_book_24px
                                        "movie" -> edu.metrostate.ics342.mediatracker.R.drawable.movie_24px
                                        else    -> edu.metrostate.ics342.mediatracker.R.drawable.tv_24px
                                    }),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item.media.title, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
