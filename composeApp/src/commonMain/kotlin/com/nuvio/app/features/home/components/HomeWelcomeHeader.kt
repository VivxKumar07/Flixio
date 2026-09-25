package com.nuvio.app.features.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import kotlinx.coroutines.delay
import kotlin.math.abs

private val MOVIE_TAGLINES = listOf(
    "Get lost in a story.",
    "Your next favorite film is waiting.",
    "Popcorn optional. Wonder mandatory.",
    "Where every frame tells a story.",
    "Dim the lights. Turn up the cinema.",
    "Escape into another universe tonight.",
    "Every scene holds a new adventure.",
    "Sit back, unwind, and let it roll.",
    "Great cinema is just a tap away.",
    "Stories made to keep you guessing.",
    "Tonight calls for something brilliant.",
    "Find your next binge-worthy obsession.",
    "Characters that stay with you forever.",
    "Pure cinema right at your fingertips.",
)

@Composable
fun HomeWelcomeHeader(
    profileName: String?,
    modifier: Modifier = Modifier,
) {
    val displayName = profileName?.takeIf { it.isNotBlank() } ?: "there"
    val todayIso = remember { CurrentDateProvider.todayIsoDate() }
    val initialSeed = remember(displayName, todayIso) {
        (displayName.hashCode() * 31) + todayIso.hashCode()
    }

    var messageIndex by remember(initialSeed) {
        mutableIntStateOf(abs(initialSeed) % MOVIE_TAGLINES.size)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000L)
            messageIndex = (messageIndex + 1) % MOVIE_TAGLINES.size
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        // Tagline - single-line with slide-up and fade transition
        AnimatedContent(
            targetState = messageIndex,
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn(tween(450)))
                    .togetherWith(slideOutVertically { -it / 3 } + fadeOut(tween(300)))
            },
            label = "RotatingTaglineTransition",
        ) { index ->
            Text(
                text = MOVIE_TAGLINES[index % MOVIE_TAGLINES.size],
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Profile Name / Greeting
        Text(
            text = "Welcome, $displayName",
            fontFamily = ClashDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = (-0.5).sp,
        )
    }
}
