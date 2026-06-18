package edu.metrostate.ics342.mediatracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onEditProfile: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user    by viewModel.currentUser.collectAsStateWithLifecycle()
    val library by viewModel.libraryPreview.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_title)) },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Outlined.Settings, stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_settings))
                }
            }
        )

        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val u = user!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (u.avatarUrl != null) {
                    AsyncImage(
                        model             = u.avatarUrl,
                        contentDescription = u.displayName,
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                u.displayName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(u.displayName, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("@${u.username}", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (!u.bio.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(u.bio, style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(u.followerCount.toString(),  edu.metrostate.ics342.mediatracker.R.string.profile_followers)
                StatItem(u.followingCount.toString(), edu.metrostate.ics342.mediatracker.R.string.profile_following)
                StatItem(u.trackedCount.toString(),   edu.metrostate.ics342.mediatracker.R.string.profile_tracked)
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick  = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_edit_button)) }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_recently_tracked), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start))

            Spacer(Modifier.height(8.dp))

            if (library.isEmpty()) {
                Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.profile_nothing_tracked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                library.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp, 56.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxSize()) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(when (item.media.mediaType) {
                                        "book" -> "📖"; "movie" -> "🎬"; "show" -> "📺"
                                        else -> "?"
                                    })
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(item.media.title, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(stringResource(item.status.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, labelRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
