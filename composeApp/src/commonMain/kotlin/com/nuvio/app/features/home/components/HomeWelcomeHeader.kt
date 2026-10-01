package com.nuvio.app.features.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.core.ui.nuvio
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
    val timeGreeting = remember(todayIso) {
        when (CurrentDateProvider.currentHour()) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
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
        modifier = modifier.fillMaxWidth(),
    ) {
            // Line 1 - time-based greeting only
            Text(
                text = timeGreeting,
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.4.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Line 2 - the profile name, large in the brand font
            Text(
                text = displayName,
                fontFamily = ClashDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.5).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Line 3 - rotating random message
            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = {
                    (fadeIn(tween(450)) + slideInVertically { it / 4 })
                        .togetherWith(fadeOut(tween(250)))
                },
                label = "TaglineSubtitleTransition",
            ) { index ->
                Text(
                    text = MOVIE_TAGLINES[index % MOVIE_TAGLINES.size],
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
