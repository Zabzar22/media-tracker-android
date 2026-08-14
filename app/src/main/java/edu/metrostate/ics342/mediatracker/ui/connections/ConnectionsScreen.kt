package edu.metrostate.ics342.mediatracker.ui.connections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    onUserClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        edu.metrostate.ics342.mediatracker.R.string.profile_followers,
        edu.metrostate.ics342.mediatracker.R.string.profile_following
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.connections_title)) })

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = { Text(stringResource(titleRes)) }
                )
            }
        }

        val users = if (selectedTab == 0)
            FakeMediaRepository.followers
        else
            FakeMediaRepository.following

        if (users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (selectedTab == 0) stringResource(edu.metrostate.ics342.mediatracker.R.string.connections_followers_empty)
                    else stringResource(edu.metrostate.ics342.mediatracker.R.string.connections_following_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(users, key = { it.id }) { user ->
                    UserRow(
                        user       = user,
                        onClick    = { onUserClick(user.id) },
                        isFollowing = selectedTab == 0 // simplified — real state comes from API
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: UserProfile,
    onClick: () -> Unit,
    isFollowing: Boolean
) {
    var following by remember { mutableStateOf(isFollowing) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape),
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
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, style = MaterialTheme.typography.titleSmall)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Follow / Following toggle
        // Week 11: Wire to POST /followers/{userId} and DELETE /followers/{userId}
        if (following) {
            OutlinedButton(
                onClick  = { following = false },
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.action_following), style = MaterialTheme.typography.labelMedium) }
        } else {
            FilledTonalButton(
                onClick  = { following = true },
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.action_follow), style = MaterialTheme.typography.labelMedium) }
        }
    }
}
