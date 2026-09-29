package com.nuvio.app.features.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.ott_apple_tv
import nuvio.composeapp.generated.resources.ott_crunchyroll
import nuvio.composeapp.generated.resources.ott_disney_plus
import nuvio.composeapp.generated.resources.ott_hulu
import nuvio.composeapp.generated.resources.ott_max
import nuvio.composeapp.generated.resources.ott_netflix
import nuvio.composeapp.generated.resources.ott_paramount_plus
import nuvio.composeapp.generated.resources.ott_prime_video
import org.jetbrains.compose.resources.painterResource

data class OttPlatform(
    val id: String,
    val name: String,
    val watchProviderId: String,
    val tag: String,
    val brandColor: Color,
    val gradientColors: List<Color>,
)

@Composable
fun ottPlatformLogo(id: String): Painter? = when (id) {
    "netflix" -> painterResource(Res.drawable.ott_netflix)
    "prime_video" -> painterResource(Res.drawable.ott_prime_video)
    "disney_plus" -> painterResource(Res.drawable.ott_disney_plus)
    "apple_tv" -> painterResource(Res.drawable.ott_apple_tv)
    "max" -> painterResource(Res.drawable.ott_max)
    "hulu" -> painterResource(Res.drawable.ott_hulu)
    "paramount_plus" -> painterResource(Res.drawable.ott_paramount_plus)
    "crunchyroll" -> painterResource(Res.drawable.ott_crunchyroll)
    else -> null
}

val OTT_PLATFORMS: List<OttPlatform> = listOf(
    OttPlatform(
        id = "netflix",
        name = "Netflix",
        watchProviderId = "8",
        tag = "Originals",
        brandColor = Color(0xFFE50914),
        gradientColors = listOf(Color(0xFFE50914).copy(alpha = 0.22f), Color(0xFF141414)),
    ),
    OttPlatform(
        id = "prime_video",
        name = "Prime Video",
        watchProviderId = "9",
        tag = "Amazon",
        brandColor = Color(0xFF00A8E1),
        gradientColors = listOf(Color(0xFF00A8E1).copy(alpha = 0.22f), Color(0xFF0F172A)),
    ),
    OttPlatform(
        id = "disney_plus",
        name = "Disney+",
        watchProviderId = "337",
        tag = "Disney",
        brandColor = Color(0xFF113CCF),
        gradientColors = listOf(Color(0xFF113CCF).copy(alpha = 0.22f), Color(0xFF0C1635)),
    ),
    OttPlatform(
        id = "apple_tv",
        name = "Apple TV+",
        watchProviderId = "350",
        tag = "Apple Original",
        brandColor = Color(0xFFE4E4E7),
        gradientColors = listOf(Color(0xFF71717A).copy(alpha = 0.22f), Color(0xFF18181B)),
    ),
    OttPlatform(
        id = "max",
        name = "Max",
        watchProviderId = "1899",
        tag = "HBO & Warner",
        brandColor = Color(0xFF002BE7),
        gradientColors = listOf(Color(0xFF002BE7).copy(alpha = 0.22f), Color(0xFF0D1226)),
    ),
    OttPlatform(
        id = "hulu",
        name = "Hulu",
        watchProviderId = "15",
        tag = "FX & Hulu",
        brandColor = Color(0xFF1CE783),
        gradientColors = listOf(Color(0xFF1CE783).copy(alpha = 0.20f), Color(0xFF0B1F16)),
    ),
    OttPlatform(
        id = "paramount_plus",
        name = "Paramount+",
        watchProviderId = "531",
        tag = "Paramount",
        brandColor = Color(0xFF0064FF),
        gradientColors = listOf(Color(0xFF0064FF).copy(alpha = 0.22f), Color(0xFF071B3B)),
    ),
    OttPlatform(
        id = "crunchyroll",
        name = "Crunchyroll",
        watchProviderId = "283",
        tag = "Anime",
        brandColor = Color(0xFFF47521),
        gradientColors = listOf(Color(0xFFF47521).copy(alpha = 0.22f), Color(0xFF26160A)),
    ),
)

@Composable
fun HomeOttPlatformsSection(
    modifier: Modifier = Modifier,
    sectionPadding: Dp = 16.dp,
    onPlatformClick: (providerName: String, watchProviderId: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sectionPadding, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "OTT Platforms",
                fontFamily = ClashDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.nuvio.colors.accent.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "Streaming",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MaterialTheme.nuvio.colors.accent,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = sectionPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = OTT_PLATFORMS,
                key = { it.id },
            ) { platform ->
                OttPlatformTile(
                    platform = platform,
                    onClick = { onPlatformClick(platform.name, platform.watchProviderId) },
                )
            }
        }
    }
}

@Composable
private fun OttPlatformTile(
    platform: OttPlatform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor = if (isPressed) {
        platform.brandColor.copy(alpha = 0.45f)
    } else {
        tokens.colors.borderSubtle
    }

    Box(
        modifier = modifier
            .width(120.dp)
            .height(58.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .clip(shape)
            .background(tokens.colors.surfaceCard)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        val logo = ottPlatformLogo(platform.id)
        if (logo != null) {
            Image(
                painter = logo,
                contentDescription = platform.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .graphicsLayer {
                        alpha = if (isPressed) 1f else 0.82f
                    },
            )
        } else {
            Text(
                text = platform.name,
                fontFamily = ClashDisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isPressed) tokens.colors.textPrimary else tokens.colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

