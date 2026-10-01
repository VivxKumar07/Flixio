package com.nuvio.app.features.library

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.downloads.DownloadStatus
import com.nuvio.app.features.downloads.DownloadsRepository
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_settings_root_downloads_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryDownloadsButton(onClick: () -> Unit) {
    val downloads by remember {
        DownloadsRepository.ensureLoaded()
        DownloadsRepository.uiState
    }.collectAsStateWithLifecycle()
    val hasCompleted = remember(downloads.items) {
        downloads.items.any { it.status == DownloadStatus.Completed }
    }

    IconButton(onClick = onClick) {
        when {
            downloads.items.any { it.status == DownloadStatus.Downloading } -> FlowingDownloadIcon()
            hasCompleted -> DownloadIcon(tint = MaterialTheme.colorScheme.primary)
            else -> DownloadIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DownloadIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Icon(
        imageVector = Icons.Rounded.CloudDownload,
        contentDescription = stringResource(Res.string.compose_settings_root_downloads_title),
        tint = tint,
        modifier = modifier.size(24.dp),
    )
}

@Composable
private fun FlowingDownloadIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "downloadFlow")
    val sweepFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweepFraction",
    )

    val flowColor = MaterialTheme.colorScheme.primary
    val baseTint = flowColor.copy(alpha = 0.45f)

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        DownloadIcon(
            tint = baseTint,
            modifier = Modifier.size(24.dp),
        )
        DownloadIcon(
            tint = flowColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    val bandWidth = size.width * 0.7f
                    val startX = (size.width + bandWidth * 2) * sweepFraction - bandWidth
                    val brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, flowColor, Color.Transparent),
                        start = Offset(startX, 0f),
                        end = Offset(startX + bandWidth, size.height),
                    )
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.SrcIn,
                    )
                },
        )
    }
}
