package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.nuvio.app.features.telegram.TelegramAuthorizationMode
import com.nuvio.app.features.telegram.TelegramRepository
import com.nuvio.app.features.telegram.TelegramUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.telegram_cache_clear
import nuvio.composeapp.generated.resources.telegram_cache_title
import nuvio.composeapp.generated.resources.telegram_code_label
import nuvio.composeapp.generated.resources.telegram_connect
import nuvio.composeapp.generated.resources.telegram_connected_as
import nuvio.composeapp.generated.resources.telegram_disconnect
import nuvio.composeapp.generated.resources.telegram_email_code_label
import nuvio.composeapp.generated.resources.telegram_email_label
import nuvio.composeapp.generated.resources.telegram_missing_credentials
import nuvio.composeapp.generated.resources.telegram_password_label
import nuvio.composeapp.generated.resources.telegram_phone_label
import nuvio.composeapp.generated.resources.telegram_section_account
import nuvio.composeapp.generated.resources.telegram_section_storage
import nuvio.composeapp.generated.resources.telegram_starting
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nuvio.composeapp.generated.resources.telegram_unsupported
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import coil3.compose.AsyncImage
import io.ktor.http.encodeURLParameter
import nuvio.composeapp.generated.resources.telegram_change_number
import nuvio.composeapp.generated.resources.telegram_code_verify_button
import nuvio.composeapp.generated.resources.telegram_login_phone_button
import nuvio.composeapp.generated.resources.telegram_login_qr_button
import nuvio.composeapp.generated.resources.telegram_or_divider
import nuvio.composeapp.generated.resources.telegram_phone_helper
import nuvio.composeapp.generated.resources.telegram_qr_open_app
import nuvio.composeapp.generated.resources.telegram_qr_step_1
import nuvio.composeapp.generated.resources.telegram_qr_step_2
import nuvio.composeapp.generated.resources.telegram_qr_step_3
import nuvio.composeapp.generated.resources.telegram_qr_title
import nuvio.composeapp.generated.resources.telegram_resend_code
import nuvio.composeapp.generated.resources.telegram_start_over

internal fun LazyListScope.telegramSettingsContent(
    isTablet: Boolean,
) {
    item {
        val uiState by remember {
            TelegramRepository.ensureLoaded()
            TelegramRepository.uiState
        }.collectAsStateWithLifecycle()

        SettingsSection(
            title = stringResource(Res.string.telegram_section_account),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                TelegramAccountCard(isTablet = isTablet, uiState = uiState)
            }
        }
    }

    item {
        val uiState by remember {
            TelegramRepository.ensureLoaded()
            TelegramRepository.uiState
        }.collectAsStateWithLifecycle()

        if (uiState.isConnected) {
            SettingsSection(
                title = stringResource(Res.string.telegram_section_storage),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    TelegramCacheCard(isTablet = isTablet, bytes = uiState.cacheSizeBytes)
                }
            }
        }
    }
}

@Composable
private fun TelegramAccountCard(
    isTablet: Boolean,
    uiState: TelegramUiState,
) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val verticalPadding = if (isTablet) 16.dp else 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        when (uiState.mode) {
            TelegramAuthorizationMode.Ready -> {
                Text(
                    text = stringResource(
                        Res.string.telegram_connected_as,
                        uiState.displayName ?: uiState.username ?: "Telegram",
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(onClick = TelegramRepository::logOut, enabled = !uiState.isBusy) {
                    Text(stringResource(Res.string.telegram_disconnect))
                }
            }
            TelegramAuthorizationMode.PhoneNumber -> {
                TelegramPhoneNumberView(uiState = uiState)
            }
            TelegramAuthorizationMode.Code -> {
                TelegramCodeView(uiState = uiState)
            }
            TelegramAuthorizationMode.QrCode -> {
                TelegramQrCodeView(uiState = uiState)
            }
            TelegramAuthorizationMode.Password -> {
                TelegramPasswordView(uiState = uiState)
            }
            TelegramAuthorizationMode.EmailAddress,
            TelegramAuthorizationMode.EmailCode -> {
                TelegramEmailView(uiState = uiState)
            }
            TelegramAuthorizationMode.MissingCredentials -> TelegramInfoText(
                stringResource(Res.string.telegram_missing_credentials),
            )
            TelegramAuthorizationMode.Unsupported -> TelegramInfoText(
                stringResource(Res.string.telegram_unsupported),
            )
            else -> TelegramInfoText(
                uiState.errorMessage ?: stringResource(Res.string.telegram_starting),
            )
        }
    }
}

@Composable
private fun TelegramPhoneNumberView(
    uiState: TelegramUiState,
) {
    var phoneInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Connect with Phone Number",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(Res.string.telegram_phone_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
            ),
            label = { Text(stringResource(Res.string.telegram_phone_label)) },
            placeholder = { Text("+1234567890") },
        )

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneInput.isNotBlank() && !uiState.isBusy,
            onClick = { TelegramRepository.submitPhoneNumber(phoneInput) },
        ) {
            if (uiState.isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sending code…")
            } else {
                Text(stringResource(Res.string.telegram_connect))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.telegram_or_divider),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isBusy,
            onClick = { TelegramRepository.requestQrCode() },
        ) {
            Text(stringResource(Res.string.telegram_login_qr_button))
        }
        Text(
            text = "Scan a QR code using your Telegram mobile app without waiting for SMS.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TelegramCodeView(
    uiState: TelegramUiState,
) {
    var codeInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Verification Code",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        val hintText = uiState.codeDeliveryHint
            ?: "Telegram has sent a verification code to your account or phone number."

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp),
            )
        }

        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            label = { Text(stringResource(Res.string.telegram_code_label)) },
            placeholder = { Text("12345") },
        )

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = codeInput.isNotBlank() && !uiState.isBusy,
                onClick = { TelegramRepository.submitCode(codeInput) },
            ) {
                if (uiState.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying…")
                } else {
                    Text(stringResource(Res.string.telegram_code_verify_button))
                }
            }

            OutlinedButton(
                enabled = !uiState.isBusy,
                onClick = { TelegramRepository.resendCode() },
            ) {
                Text(stringResource(Res.string.telegram_resend_code))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                enabled = !uiState.isBusy,
                onClick = { TelegramRepository.switchToPhoneNumber() },
            ) {
                Text(stringResource(Res.string.telegram_change_number))
            }

            TextButton(
                enabled = !uiState.isBusy,
                onClick = { TelegramRepository.restartAuthentication() },
            ) {
                Text(stringResource(Res.string.telegram_start_over))
            }
        }
    }
}

@Composable
private fun TelegramQrCodeView(
    uiState: TelegramUiState,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.telegram_qr_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.telegram_qr_step_1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.telegram_qr_step_2),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.telegram_qr_step_3),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val qrLink = uiState.qrCodeLink
        if (!qrLink.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${qrLink.encodeURLParameter()}",
                    contentDescription = "Telegram QR Code",
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    runCatching { uriHandler.openUri(qrLink) }
                },
            ) {
                Text(stringResource(Res.string.telegram_qr_open_app))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(
                onClick = { TelegramRepository.switchToPhoneNumber() },
            ) {
                Text(stringResource(Res.string.telegram_login_phone_button))
            }

            TextButton(
                onClick = { TelegramRepository.restartAuthentication() },
            ) {
                Text(stringResource(Res.string.telegram_start_over))
            }
        }
    }
}

@Composable
private fun TelegramPasswordView(
    uiState: TelegramUiState,
) {
    var passwordInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Two-Step Verification",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Your Telegram account is protected by an additional cloud password.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsSecretTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = stringResource(Res.string.telegram_password_label),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null,
        )

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = passwordInput.isNotBlank() && !uiState.isBusy,
                onClick = { TelegramRepository.submitPassword(passwordInput) },
            ) {
                if (uiState.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying…")
                } else {
                    Text(stringResource(Res.string.telegram_connect))
                }
            }

            OutlinedButton(
                enabled = !uiState.isBusy,
                onClick = { TelegramRepository.restartAuthentication() },
            ) {
                Text(stringResource(Res.string.telegram_start_over))
            }
        }
    }
}

@Composable
private fun TelegramEmailView(
    uiState: TelegramUiState,
) {
    var input by rememberSaveable(uiState.mode) { mutableStateOf("") }
    val isCode = uiState.mode == TelegramAuthorizationMode.EmailCode

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (isCode) "Email Verification Code" else "Email Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isCode) KeyboardType.Number else KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            label = {
                Text(
                    stringResource(
                        if (isCode) Res.string.telegram_email_code_label else Res.string.telegram_email_label,
                    ),
                )
            },
        )

        uiState.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = input.isNotBlank() && !uiState.isBusy,
                onClick = {
                    if (isCode) TelegramRepository.submitEmailCode(input)
                    else TelegramRepository.submitEmailAddress(input)
                },
            ) {
                Text(stringResource(Res.string.telegram_connect))
            }

            OutlinedButton(
                onClick = { TelegramRepository.restartAuthentication() },
            ) {
                Text(stringResource(Res.string.telegram_start_over))
            }
        }
    }
}

@Composable
private fun TelegramCacheCard(isTablet: Boolean, bytes: Long) {
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val verticalPadding = if (isTablet) 16.dp else 14.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(stringResource(Res.string.telegram_cache_title), fontWeight = FontWeight.Medium)
            Text(
                text = formatBytes(bytes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(onClick = TelegramRepository::clearCache) {
            Text(stringResource(Res.string.telegram_cache_clear))
        }
    }
}

@Composable
private fun TelegramInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "${oneDecimal(bytes / 1_073_741_824.0)} GB"
    bytes >= 1_048_576L -> "${oneDecimal(bytes / 1_048_576.0)} MB"
    bytes >= 1_024L -> "${oneDecimal(bytes / 1_024.0)} KB"
    else -> "$bytes B"
}

private fun oneDecimal(value: Double): String {
    val tenths = (value * 10.0).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
