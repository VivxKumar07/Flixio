package com.nuvio.app.features.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import com.nuvio.app.core.ui.PlatformBackHandler
import com.nuvio.app.core.ui.FlixioLoadingIndicator
import com.nuvio.app.features.tmdb.TmdbService
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.auth.AuthRepository
import com.nuvio.app.core.auth.DeviceLinkAuthRepository
import com.nuvio.app.core.auth.DeviceLinkAuthState
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_auth_already_have_account
import nuvio.composeapp.generated.resources.compose_auth_continue_without_account
import nuvio.composeapp.generated.resources.compose_auth_create_account
import nuvio.composeapp.generated.resources.compose_auth_dont_have_account
import nuvio.composeapp.generated.resources.compose_auth_email
import nuvio.composeapp.generated.resources.compose_auth_or_separator
import nuvio.composeapp.generated.resources.compose_auth_password
import nuvio.composeapp.generated.resources.compose_auth_sign_in
import nuvio.composeapp.generated.resources.compose_auth_sign_in_subtitle
import nuvio.composeapp.generated.resources.compose_auth_sign_up
import nuvio.composeapp.generated.resources.compose_auth_sign_up_subtitle
import nuvio.composeapp.generated.resources.compose_auth_store_locally
import nuvio.composeapp.generated.resources.compose_auth_tagline
import nuvio.composeapp.generated.resources.compose_auth_terms_link
import nuvio.composeapp.generated.resources.compose_auth_terms_prefix
import nuvio.composeapp.generated.resources.compose_auth_welcome_back
import org.jetbrains.compose.resources.stringResource

internal val AuthTextPrimary = Color(0xFFF5F7F8)
internal val AuthTextSecondary = Color(0xFF969CA3)
private val AuthTextMuted = Color(0xFF6E7178)
private val AuthPrimaryButtonBackground = Color(0xFFF5F5F5)
private val AuthPrimaryButtonText = Color(0xFF111111)
internal val AuthFieldBackground = Color.White.copy(alpha = 0.04f)
private val AuthFieldBackgroundMobile = Color.White.copy(alpha = 0.035f)
internal val AuthFieldBorder = Color.White.copy(alpha = 0.08f)
private val AuthPaneBackground = Color.White.copy(alpha = 0.022f)
private val AuthPaneBorder = Color.White.copy(alpha = 0.07f)
private val AuthDividerColor = Color.White.copy(alpha = 0.10f)
private val AuthSecondaryButtonBackground = Color.White.copy(alpha = 0.05f)
private val AuthSecondaryButtonBorder = Color.White.copy(alpha = 0.09f)

private data class AuthFormMetrics(
    val fieldHeight: Dp,
    val fieldHorizontalPadding: Dp,
    val iconSize: Dp,
    val passwordIconSize: Dp,
    val primaryTop: Dp,
    val primaryHeight: Dp,
    val toggleTop: Dp,
    val dividerTop: Dp,
    val secondaryTop: Dp,
    val secondaryHeight: Dp,
    val fieldBackground: Color,
)

private val MobileAuthFormMetrics = AuthFormMetrics(
    fieldHeight = 56.dp,
    fieldHorizontalPadding = 16.dp,
    iconSize = 19.dp,
    passwordIconSize = 20.dp,
    primaryTop = 22.dp,
    primaryHeight = 54.dp,
    toggleTop = 18.dp,
    dividerTop = 28.dp,
    secondaryTop = 24.dp,
    secondaryHeight = 54.dp,
    fieldBackground = AuthFieldBackgroundMobile,
)

private val LargeAuthFormMetrics = AuthFormMetrics(
    fieldHeight = 58.dp,
    fieldHorizontalPadding = 18.dp,
    iconSize = 20.dp,
    passwordIconSize = 21.dp,
    primaryTop = 24.dp,
    primaryHeight = 56.dp,
    toggleTop = 18.dp,
    dividerTop = 30.dp,
    secondaryTop = 26.dp,
    secondaryHeight = 56.dp,
    fieldBackground = AuthFieldBackground,
)

private fun largeAuthScale(screenWidth: Dp): Float =
    (screenWidth.value / 1194f).coerceIn(1f, 1.32f)

private fun largeAuthFormMetrics(scale: Float): AuthFormMetrics = LargeAuthFormMetrics.copy(
    fieldHeight = 58.dp * scale,
    fieldHorizontalPadding = 18.dp * scale,
    iconSize = 20.dp * scale,
    passwordIconSize = 21.dp * scale,
    primaryTop = 24.dp * scale,
    primaryHeight = 56.dp * scale,
    toggleTop = 18.dp * scale,
    dividerTop = 30.dp * scale,
    secondaryTop = 26.dp * scale,
    secondaryHeight = 56.dp * scale,
)

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
) {
    val authError by AuthRepository.error.collectAsStateWithLifecycle()
    val deviceLinkAuthState by DeviceLinkAuthRepository.state.collectAsStateWithLifecycle()
    val serverConnectionState by ServerConnectionController.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showWelcomeScreen by rememberSaveable { mutableStateOf(true) }
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var emailFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var passwordFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var showServerSheet by rememberSaveable { mutableStateOf(false) }
    var showOfficialServerDialog by rememberSaveable { mutableStateOf(false) }

    fun submitAuth() {
        if (isLoading) return
        if (email.isBlank()) {
            AuthRepository.setError("Please enter your email address.")
            return
        }
        if (!email.contains('@') || !email.contains('.')) {
            AuthRepository.setError("Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            AuthRepository.setError("Password must be at least 6 characters long.")
            return
        }
        DeviceLinkAuthRepository.cancel()
        isLoading = true
        focusManager.clearFocus(force = true)
        scope.launch {
            if (isSignUp) AuthRepository.signUpWithEmail(email.trim(), password)
            else AuthRepository.signInWithEmail(email.trim(), password)
            isLoading = false
        }
    }

    fun toggleAuthMode() {
        DeviceLinkAuthRepository.cancel()
        isSignUp = !isSignUp
        AuthRepository.clearError()
    }

    fun startDeviceLink() {
        if (isLoading) return
        focusManager.clearFocus(force = true)
        AuthRepository.clearError()
        DeviceLinkAuthRepository.start()
    }

    fun signInWithGoogle() {
        if (isLoading) return
        focusManager.clearFocus(force = true)
        DeviceLinkAuthRepository.cancel()
        AuthRepository.clearError()
        isLoading = true
        scope.launch {
            AuthRepository.signInWithGoogle()
            isLoading = false
        }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(serverConnectionState.activeServer.backendUrl) {
        DeviceLinkAuthRepository.cancel()
        if (!serverConnectionState.activeServer.isCustom) {
            showOfficialServerDialog = false
        }
    }

    DisposableEffect(Unit) {
        onDispose(DeviceLinkAuthRepository::cancel)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(emailFieldBounds, passwordFieldBounds) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial,
                    )
                    val tappedTextField = listOfNotNull(emailFieldBounds, passwordFieldBounds)
                        .any { bounds -> bounds.contains(down.position) }
                    if (!tappedTextField) {
                        focusManager.clearFocus(force = true)
                    }
                }
            },
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val screenWidth = maxWidth
            val largeScreen = screenWidth >= 900.dp
            val compactLargeScreen = screenWidth < 1100.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .authGradientBackground(largeScreen = largeScreen),
            ) {
                if (largeScreen) {
                    val largeScale = largeAuthScale(screenWidth)
                    val formPaneWidth = (screenWidth * 0.30f).coerceIn(460.dp * largeScale, 720.dp * largeScale)
                    val brandHorizontalPadding = (screenWidth * 0.055f).coerceIn(56.dp * largeScale, 128.dp * largeScale)
                    val formHorizontalPadding = (formPaneWidth * 0.14f).coerceIn(48.dp, 96.dp)
                    val formMetrics = largeAuthFormMetrics(largeScale)

                    AuthLargeLayout(
                        modifier = Modifier.fillMaxSize(),
                        isSignUp = isSignUp,
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        isLoading = isLoading,
                        authError = authError,
                        deviceLinkAuthState = deviceLinkAuthState,
                        deviceLinkEnabled = serverConnectionState.activeServer.capabilities.tvLogin,
                        formPaneWidth = if (compactLargeScreen) 460.dp else formPaneWidth,
                        brandHorizontalPadding = brandHorizontalPadding,
                        formHorizontalPadding = formHorizontalPadding,
                        formMetrics = formMetrics,
                        scale = largeScale,
                        onEmailChange = {
                            email = it
                            AuthRepository.clearError()
                        },
                        onPasswordChange = {
                            password = it
                            AuthRepository.clearError()
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onSubmit = ::submitAuth,
                        onToggleAuthMode = ::toggleAuthMode,
                        onSignInWithGoogle = ::signInWithGoogle,
                        onContinueWithoutAccount = {
                            focusManager.clearFocus(force = true)
                            DeviceLinkAuthRepository.cancel()
                            AuthRepository.signInAnonymously()
                        },
                        onStartDeviceLink = ::startDeviceLink,
                        onCancelDeviceLink = DeviceLinkAuthRepository::cancel,
                        onEmailBoundsChange = { emailFieldBounds = it },
                        onPasswordBoundsChange = { passwordFieldBounds = it },
                    )
                } else if (showWelcomeScreen) {
                    FlixioGetStartedScreen(
                        statusBarTop = statusBarTop,
                        onGetStarted = {
                            AuthRepository.clearError()
                            isSignUp = true
                            showWelcomeScreen = false
                        },
                        onSignIn = {
                            AuthRepository.clearError()
                            isSignUp = false
                            showWelcomeScreen = false
                        },
                        onContinueWithoutAccount = {
                            focusManager.clearFocus(force = true)
                            DeviceLinkAuthRepository.cancel()
                            AuthRepository.signInAnonymously()
                        },
                    )
                } else {
                    PlatformBackHandler(enabled = true) {
                        showWelcomeScreen = true
                        AuthRepository.clearError()
                    }
                    AuthMobileLayout(
                        isSignUp = isSignUp,
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        isLoading = isLoading,
                        authError = authError,
                        deviceLinkAuthState = deviceLinkAuthState,
                        deviceLinkEnabled = serverConnectionState.activeServer.capabilities.tvLogin,
                        statusBarTop = statusBarTop,
                        onBack = {
                            showWelcomeScreen = true
                            AuthRepository.clearError()
                        },
                        onEmailChange = {
                            email = it
                            AuthRepository.clearError()
                        },
                        onPasswordChange = {
                            password = it
                            AuthRepository.clearError()
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onSubmit = ::submitAuth,
                        onToggleAuthMode = ::toggleAuthMode,
                        onSignInWithGoogle = ::signInWithGoogle,
                        onContinueWithoutAccount = {
                            focusManager.clearFocus(force = true)
                            DeviceLinkAuthRepository.cancel()
                            AuthRepository.signInAnonymously()
                        },
                        onStartDeviceLink = ::startDeviceLink,
                        onCancelDeviceLink = DeviceLinkAuthRepository::cancel,
                        onEmailBoundsChange = { emailFieldBounds = it },
                        onPasswordBoundsChange = { passwordFieldBounds = it },
                    )
                }
            }
        }

        if (AppFeaturePolicy.customServerConnectionsEnabled) {
            ServerConnectionMenu(
                activeServer = serverConnectionState.activeServer,
                onUseOfficial = {
                    ServerConnectionController.resetDiscovery()
                    showOfficialServerDialog = true
                },
                onConnectCustom = {
                    ServerConnectionController.resetDiscovery()
                    showServerSheet = true
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = statusBarTop + 4.dp),
            )
        }
    }

    if (
        AppFeaturePolicy.customServerConnectionsEnabled &&
        showServerSheet &&
        serverConnectionState.discoveredServer == null
    ) {
        ServerConnectionSheet(
            state = serverConnectionState,
            onDiscover = ServerConnectionController::discover,
            onDismiss = {
                showServerSheet = false
                ServerConnectionController.resetDiscovery()
            },
        )
    }

    serverConnectionState.discoveredServer?.let { server ->
        if (AppFeaturePolicy.customServerConnectionsEnabled) {
            ServerTrustDialog(
                server = server,
                isSwitching = serverConnectionState.isSwitching,
                switchFailure = serverConnectionState.switchFailure,
                onConfirm = ServerConnectionController::connectDiscovered,
                onDismiss = {
                    showServerSheet = false
                    ServerConnectionController.resetDiscovery()
                },
            )
        }
    }

    if (AppFeaturePolicy.customServerConnectionsEnabled && showOfficialServerDialog) {
        OfficialServerDialog(
            isSwitching = serverConnectionState.isSwitching,
            switchFailure = serverConnectionState.switchFailure,
            onConfirm = ServerConnectionController::useOfficial,
            onDismiss = {
                showOfficialServerDialog = false
                ServerConnectionController.resetDiscovery()
            },
        )
    }
}

/**
 * Dribbble Image 1 Phone 1:
 * Dramatic First Launch / Welcome Screen:
 * - 3D angled cascading grid of movie posters fading smoothly into deep obsidian background
 * - Flixio brand wordmark and logo
 * - Pagination indicator dots
 * - Hero headline ("Your Ultimate Destination for Movies & TV Shows")
 * - Rounded pill CTA buttons ("Get Started" and "Sign In")
 * - "Continue as Guest" option
 */
@Composable
private fun FlixioGetStartedScreen(
    statusBarTop: Dp,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
) {
    var dynamicPosters by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        val trending = runCatching { TmdbService.fetchTrendingAll() }.getOrNull()
        val urls = trending?.mapNotNull { it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w500$p" } }
        if (!urls.isNullOrEmpty()) {
            dynamicPosters = urls
        }
    }
    val posters = dynamicPosters.ifEmpty { DEFAULT_GET_STARTED_POSTERS }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top 3D tilted cascade poster grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .graphicsLayer {
                    rotationZ = -10f
                    scaleX = 1.25f
                    scaleY = 1.25f
                    translationY = -40f
                    translationX = -10f
                },
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Column 1 (offset up)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { translationY = -50f },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(4) { idx ->
                        GetStartedPosterCard(
                            color = Color(0xFF1E2230),
                            posterUrl = posters[idx % posters.size],
                        )
                    }
                }
                // Column 2 (offset down)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { translationY = 20f },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(4) { idx ->
                        GetStartedPosterCard(
                            color = Color(0xFF1A1C24),
                            posterUrl = posters[(idx + 4) % posters.size],
                        )
                    }
                }
                // Column 3 (offset up)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { translationY = -30f },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(4) { idx ->
                        GetStartedPosterCard(
                            color = Color(0xFF232736),
                            posterUrl = posters[(idx + 8) % posters.size],
                        )
                    }
                }
            }
        }

        // Heavy dark gradient overlay that smoothly dissolves the posters into obsidian black
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x8008090C),
                            Color(0xEE08090C),
                            Color(0xFF08090C),
                            Color(0xFF08090C),
                        ),
                        startY = 180f,
                    ),
                ),
        )

        // Top ambient aurora lighting beam matching Dribbble Image 3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E599).copy(alpha = 0.28f),
                            Color(0xFF0BBF9A).copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        center = Offset(Float.POSITIVE_INFINITY / 2f, 0f),
                        radius = 600f,
                    ),
                ),
        )

        // Bottom content panel with logo, headline, pagination dots, and pill buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand Logo & Wordmark
            AppBrandWordmark(
                contentDescription = null,
                modifier = Modifier.height(34.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main headline
            Text(
                text = "Your Ultimate Destination\nfor Movies & TV Shows",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Explore endless entertainment with personalized tracking, cloud library, and addons.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.65f),
                ),
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // "Get Started" Primary Pill Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF111111),
                ),
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111111),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Sign In" Secondary Frosted Pill Button
            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Color.White,
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Continue without account"
            Text(
                text = stringResource(Res.string.compose_auth_continue_without_account),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onContinueWithoutAccount)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

private val DEFAULT_GET_STARTED_POSTERS = listOf(
    "https://image.tmdb.org/t/p/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg", // Fast X
    "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg", // Shawshank Redemption
    "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", // Oppenheimer
    "https://image.tmdb.org/t/p/w500/fiVW06jE7z9YnO4trhaMEdclSiC.jpg", // Spider-Man
    "https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg", // Barbie
    "https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg", // Kung Fu Panda
    "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg", // Dune
    "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg", // The Godfather
    "https://image.tmdb.org/t/p/w500/velWPhVMQeQKcxggNEU8YmIo52R.jpg", // Gladiator
    "https://image.tmdb.org/t/p/w500/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg", // Avengers: Infinity War
    "https://image.tmdb.org/t/p/w500/ctMserH8g2SeOAnCw5gFjdQF8mo.jpg", // Deadpool & Wolverine
    "https://image.tmdb.org/t/p/w500/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg", // Inside Out 2
)

@Composable
private fun GetStartedPosterCard(
    color: Color,
    posterUrl: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Subtle gradient overlay for depth and aesthetic consistency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x50000000),
                            Color(0x9005070B),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun AuthMobileLayout(
    isSignUp: Boolean,
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    authError: String?,
    deviceLinkAuthState: DeviceLinkAuthState,
    deviceLinkEnabled: Boolean,
    statusBarTop: Dp,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    onStartDeviceLink: () -> Unit,
    onCancelDeviceLink: () -> Unit,
    onEmailBoundsChange: (Rect) -> Unit,
    onPasswordBoundsChange: (Rect) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                top = statusBarTop + 16.dp,
                end = 24.dp,
                bottom = 40.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 342.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            AuthBrandLockup(logoHeight = 38.dp)

            Spacer(modifier = Modifier.height(44.dp))

            AuthHeading(
                isSignUp = isSignUp,
                headingStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = AuthTextPrimary,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                subtitleStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = AuthTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthForm(
                isSignUp = isSignUp,
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                isLoading = isLoading,
                authError = authError,
                deviceLinkAuthState = deviceLinkAuthState,
                deviceLinkEnabled = deviceLinkEnabled,
                metrics = MobileAuthFormMetrics,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                onSubmit = onSubmit,
                onToggleAuthMode = onToggleAuthMode,
                onSignInWithGoogle = onSignInWithGoogle,
                onContinueWithoutAccount = onContinueWithoutAccount,
                onStartDeviceLink = onStartDeviceLink,
                onCancelDeviceLink = onCancelDeviceLink,
                onEmailBoundsChange = onEmailBoundsChange,
                onPasswordBoundsChange = onPasswordBoundsChange,
            )
        }
    }
}

@Composable
private fun AuthLargeLayout(
    modifier: Modifier = Modifier,
    isSignUp: Boolean,
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    authError: String?,
    deviceLinkAuthState: DeviceLinkAuthState,
    deviceLinkEnabled: Boolean,
    formPaneWidth: Dp,
    brandHorizontalPadding: Dp,
    formHorizontalPadding: Dp,
    formMetrics: AuthFormMetrics,
    scale: Float,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    onStartDeviceLink: () -> Unit,
    onCancelDeviceLink: () -> Unit,
    onEmailBoundsChange: (Rect) -> Unit,
    onPasswordBoundsChange: (Rect) -> Unit,
) {
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = brandHorizontalPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            AppBrandWordmark(
                contentDescription = null,
                modifier = Modifier.height(60.dp * scale),
            )
            Spacer(modifier = Modifier.height(32.dp * scale))
            Text(
                text = stringResource(Res.string.compose_auth_tagline),
                modifier = Modifier.widthIn(max = 440.dp * scale),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = AuthTextPrimary,
                    fontSize = (40f * scale).sp,
                    lineHeight = (45f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(18.dp * scale))
            Text(
                text = stringResource(Res.string.compose_auth_sign_up_subtitle),
                modifier = Modifier.widthIn(max = 400.dp * scale),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = AuthTextSecondary,
                    fontSize = (17f * scale).sp,
                    lineHeight = (26f * scale).sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }

        Box(
            modifier = Modifier
                .width(formPaneWidth)
                .fillMaxHeight()
                .background(AuthPaneBackground)
                .drawBehind {
                    drawLine(
                        color = AuthPaneBorder,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = formHorizontalPadding),
            ) {
                AuthHeading(
                    isSignUp = isSignUp,
                    headingStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = AuthTextPrimary,
                        fontSize = (30f * scale).sp,
                        lineHeight = (33f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    subtitleStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = AuthTextSecondary,
                        fontSize = (15f * scale).sp,
                        lineHeight = (21f * scale).sp,
                        fontWeight = FontWeight.Normal,
                    ),
                )

                Spacer(modifier = Modifier.height(32.dp * scale))

                AuthForm(
                    isSignUp = isSignUp,
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    authError = authError,
                    deviceLinkAuthState = deviceLinkAuthState,
                    deviceLinkEnabled = deviceLinkEnabled,
                    metrics = formMetrics,
                    scale = scale,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    onSubmit = onSubmit,
                    onToggleAuthMode = onToggleAuthMode,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onContinueWithoutAccount = onContinueWithoutAccount,
                    onStartDeviceLink = onStartDeviceLink,
                    onCancelDeviceLink = onCancelDeviceLink,
                    onEmailBoundsChange = onEmailBoundsChange,
                    onPasswordBoundsChange = onPasswordBoundsChange,
                )
            }
        }
    }
}

@Composable
private fun AuthBrandLockup(
    logoHeight: Dp,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBrandWordmark(
            contentDescription = null,
            modifier = Modifier.height(logoHeight),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(Res.string.compose_auth_tagline),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun AuthHeading(
    isSignUp: Boolean,
    headingStyle: TextStyle,
    subtitleStyle: TextStyle,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedContent(
            targetState = isSignUp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "authHeading",
        ) { signUp ->
            Text(
                text = if (signUp) stringResource(Res.string.compose_auth_create_account)
                else stringResource(Res.string.compose_auth_welcome_back),
                style = headingStyle,
                color = AuthTextPrimary,
            )
        }
        AnimatedContent(
            targetState = isSignUp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "authSubtitle",
        ) { signUp ->
            Text(
                text = if (signUp) stringResource(Res.string.compose_auth_sign_up_subtitle)
                else stringResource(Res.string.compose_auth_sign_in_subtitle),
                style = subtitleStyle,
                color = AuthTextSecondary,
            )
        }
    }
}

@Composable
private fun AuthForm(
    isSignUp: Boolean,
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    authError: String?,
    deviceLinkAuthState: DeviceLinkAuthState,
    deviceLinkEnabled: Boolean,
    metrics: AuthFormMetrics,
    scale: Float = 1f,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    onStartDeviceLink: () -> Unit,
    onCancelDeviceLink: () -> Unit,
    onEmailBoundsChange: (Rect) -> Unit,
    onPasswordBoundsChange: (Rect) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = stringResource(Res.string.compose_auth_email),
            icon = Icons.Rounded.Email,
            metrics = metrics,
            scale = scale,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                onEmailBoundsChange(coordinates.boundsInRoot())
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        AuthTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(Res.string.compose_auth_password),
            icon = Icons.Rounded.Lock,
            metrics = metrics,
            scale = scale,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityToggle = onPasswordVisibilityToggle,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                onPasswordBoundsChange(coordinates.boundsInRoot())
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() },
            ),
        )

        authError?.let { errorText ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.error,
                    fontSize = (13f * scale).sp,
                    lineHeight = (18f * scale).sp,
                ),
            )
        }

        if (isSignUp) {
            Spacer(modifier = Modifier.height(14.dp * scale))
            AuthTermsAcknowledgement(
                scale = scale,
                onTermsClick = { uriHandler.openUri("https://github.com/VivxKumar07/Flixio#disclaimer") },
            )
        }

        Spacer(modifier = Modifier.height(metrics.primaryTop))

        AuthPrimaryButton(
            text = if (isSignUp) stringResource(Res.string.compose_auth_create_account)
            else stringResource(Res.string.compose_auth_sign_in),
            isLoading = isLoading,
            enabled = !isLoading,
            height = metrics.primaryHeight,
            scale = scale,
            onClick = onSubmit,
        )

        Spacer(modifier = Modifier.height(metrics.toggleTop))

        AuthModeToggle(
            isSignUp = isSignUp,
            scale = scale,
            onToggleAuthMode = onToggleAuthMode,
        )

        Spacer(modifier = Modifier.height(metrics.dividerTop))

        AuthDivider(scale = scale)

        Spacer(modifier = Modifier.height(metrics.secondaryTop))

        AuthSecondaryButton(
            text = stringResource(Res.string.compose_auth_continue_without_account),
            enabled = !isLoading,
            height = metrics.secondaryHeight,
            scale = scale,
            onClick = onContinueWithoutAccount,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(Res.string.compose_auth_store_locally),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthTextMuted,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale).sp,
                fontWeight = FontWeight.Normal,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AuthTermsAcknowledgement(
    scale: Float,
    onTermsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.compose_auth_terms_prefix),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthTextSecondary,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale).sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp * scale))
        Text(
            text = stringResource(Res.string.compose_auth_terms_link),
            modifier = Modifier.clickable(onClick = onTermsClick),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthTextPrimary,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale).sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    metrics: AuthFormMetrics,
    scale: Float,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(metrics.fieldHeight)
            .background(metrics.fieldBackground, shape)
            .border(1.dp, AuthFieldBorder, shape)
            .padding(horizontal = metrics.fieldHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(metrics.iconSize),
            tint = AuthTextMuted,
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = AuthTextPrimary,
                fontSize = (16f * scale).sp,
                lineHeight = (22f * scale).sp,
                fontWeight = FontWeight.Normal,
            ),
            cursorBrush = SolidColor(AuthTextPrimary),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = AuthTextMuted,
                                fontSize = (16f * scale).sp,
                                lineHeight = (22f * scale).sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (isPassword) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable(onClick = onPasswordVisibilityToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff
                    else Icons.Rounded.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(metrics.passwordIconSize),
                    tint = AuthTextSecondary,
                )
            }
        }
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    height: Dp,
    scale: Float,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimaryButtonBackground,
            contentColor = AuthPrimaryButtonText,
            disabledContainerColor = if (isLoading) {
                AuthPrimaryButtonBackground
            } else {
                AuthPrimaryButtonBackground.copy(alpha = 0.45f)
            },
            disabledContentColor = AuthPrimaryButtonText.copy(alpha = 0.55f),
        ),
    ) {
        if (isLoading) {
            FlixioLoadingIndicator(
                modifier = Modifier.size(20.dp * scale),
                color = AuthPrimaryButtonText,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (16f * scale).sp,
                    lineHeight = (22f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun AuthModeToggle(
    isSignUp: Boolean,
    scale: Float,
    onToggleAuthMode: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggleAuthMode)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = isSignUp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "authTogglePrompt",
        ) { signUp ->
            Text(
                text = if (signUp) {
                    stringResource(Res.string.compose_auth_already_have_account).trimEnd()
                } else {
                    stringResource(Res.string.compose_auth_dont_have_account).trimEnd()
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AuthTextSecondary,
                    fontSize = (14f * scale).sp,
                    lineHeight = (20f * scale).sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        AnimatedContent(
            targetState = isSignUp,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "authToggleAction",
        ) { signUp ->
            Text(
                text = if (signUp) stringResource(Res.string.compose_auth_sign_in)
                else stringResource(Res.string.compose_auth_sign_up),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AuthTextPrimary,
                    fontSize = (14f * scale).sp,
                    lineHeight = (20f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

@Composable
private fun AuthDivider(scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthDividerColor),
        )
        Text(
            text = stringResource(Res.string.compose_auth_or_separator).trim(),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AuthTextMuted,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale).sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthDividerColor),
        )
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    enabled: Boolean,
    height: Dp,
    scale: Float,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AuthSecondaryButtonBorder),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthSecondaryButtonBackground,
            contentColor = AuthTextPrimary,
            disabledContainerColor = AuthSecondaryButtonBackground.copy(alpha = 0.45f),
            disabledContentColor = AuthTextPrimary.copy(alpha = 0.55f),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (15f * scale).sp,
                lineHeight = (21f * scale).sp,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.authGradientBackground(largeScreen: Boolean): Modifier = drawWithCache {
    val angleDegrees = if (largeScreen) 122.0 else 148.0
    val angleRadians = angleDegrees * PI / 180.0
    val directionX = sin(angleRadians).toFloat()
    val directionY = (-cos(angleRadians)).toFloat()
    val halfLength = (abs(size.width * directionX) + abs(size.height * directionY)) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val start = Offset(
        x = center.x - directionX * halfLength,
        y = center.y - directionY * halfLength,
    )
    val end = Offset(
        x = center.x + directionX * halfLength,
        y = center.y + directionY * halfLength,
    )
    val colorStops = if (largeScreen) {
        arrayOf(
            0f to Color(0xFF21113B),
            0.14f to Color(0xFF21113B),
            0.26f to Color(0xFF1A0E2F),
            0.36f to Color(0xFF130A23),
            0.48f to Color(0xFF0A060F),
            0.60f to Color(0xFF050408),
            0.70f to Color.Black,
            1f to Color.Black,
        )
    } else {
        arrayOf(
            0f to Color(0xFF21113B),
            0.12f to Color(0xFF21113B),
            0.24f to Color(0xFF1A0E2F),
            0.34f to Color(0xFF130A23),
            0.44f to Color(0xFF0A060F),
            0.58f to Color(0xFF050408),
            0.64f to Color.Black,
            1f to Color.Black,
        )
    }
    val brush = Brush.linearGradient(
        colorStops = colorStops,
        start = start,
        end = end,
    )
    onDrawBehind {
        drawRect(brush = brush)
    }
}

private fun LayoutCoordinates.boundsInRoot(): Rect {
    val position = positionInRoot()
    return Rect(
        left = position.x,
        top = position.y,
        right = position.x + size.width,
        bottom = position.y + size.height,
    )
}
