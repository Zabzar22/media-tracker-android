package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.theme.MediaTrackerTheme
import edu.metrostate.ics342.mediatracker.theme.OnPrimaryContainer
import edu.metrostate.ics342.mediatracker.theme.PrimaryContainer

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var displayName     by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    fun attemptRegister() {
        focusManager.clearFocus()
        when {
            displayName.isBlank() || email.isBlank() || username.isBlank() ||
            password.isBlank()    || confirmPassword.isBlank() -> {
                errorMessage = "Please fill in all fields."
            }
            password != confirmPassword -> {
                errorMessage = "Passwords do not match."
            }
            else -> onRegisterSuccess()
        }
    }

    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.smart_display),
            contentDescription = "Application Icon",
            modifier = Modifier
                .size(64.dp)
                .background(
                    PrimaryContainer,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            colorFilter = ColorFilter.tint(OnPrimaryContainer)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.register_tagline),
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value         = displayName,
            onValueChange = { displayName = it; errorMessage = null },
            label         = { Text(stringResource(R.string.display_name_label)) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = username,
            onValueChange = { username = it; errorMessage = null },
            label         = { Text(stringResource(R.string.username_label)) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value         = email,
            onValueChange = { email = it; errorMessage = null },
            label         = { Text(stringResource(R.string.email_label)) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it; errorMessage = null },
            label                = { Text(stringResource(R.string.password_label)) },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value                = confirmPassword,
            onValueChange        = { confirmPassword = it; errorMessage = null },
            label                = { Text(stringResource(R.string.confirm_password_label)) },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { attemptRegister() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = { attemptRegister() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(R.string.sign_up_button))
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.login_prompt))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MediaTrackerTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onNavigateToLogin = {}
        )
    }
}
