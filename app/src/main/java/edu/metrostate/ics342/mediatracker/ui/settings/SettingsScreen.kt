package edu.metrostate.ics342.mediatracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.data.FakeMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.TokenStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignOut: () -> Unit
) {
    var signOutDialogVisible by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    if (signOutDialogVisible) {
        AlertDialog(
            onDismissRequest = { signOutDialogVisible = false },
            title            = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_sign_out_confirm_title)) },
            text             = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_sign_out_confirm_message)) },
            confirmButton    = {
                // Forget the token and the profile before leaving, not just navigate away.
                // Signing out used to only send you back to the login screen while the old
                // session sat in memory - which matters to us, because we each test with a
                // different account and the next login would otherwise inherit this one.
                TextButton(onClick = {
                    signOutDialogVisible = false
                    TokenStore.clear()
                    onSignOut()
                }) {
                    Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_sign_out_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { signOutDialogVisible = false }) { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_cancel_button)) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_title)) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            // ── Account section ────────────────────────────────────────────────
            SectionHeader(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_section_account))

            ListItem(
                headlineContent   = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_email_label)) },
                supportingContent = { Text(FakeMediaRepository.currentUser.email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_change_password)) },
                trailingContent = {
                    Icon(Icons.Outlined.Lock, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            Spacer(Modifier.height(8.dp))

            // ── App section ────────────────────────────────────────────────────
            SectionHeader(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_section_app))

            ListItem(
                headlineContent  = { Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_dark_mode)) },
                trailingContent  = {
                    Switch(
                        checked         = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                },
                leadingContent = {
                    Icon(Icons.Outlined.DarkMode, contentDescription = null)
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Sign out ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick  = { signOutDialogVisible = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(edu.metrostate.ics342.mediatracker.R.string.settings_sign_out_button))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
