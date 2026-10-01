package com.nuvio.app.features.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
internal fun AvatarPicker(
    avatars: List<AvatarCatalogItem>,
    selectedAvatarId: String?,
    onAvatarSelected: (AvatarCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (avatars.isEmpty()) return
    val spacing = 10.dp
    val minAvatarSize = 58.dp
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minAvatarSize),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(
            items = avatars,
            key = { it.id },
        ) { avatar ->
            AvatarChoiceItem(
                avatar = avatar,
                modifier = Modifier.aspectRatio(1f),
                isSelected = avatar.id == selectedAvatarId,
                onClick = { onAvatarSelected(avatar) },
            )
        }
    }
}

@Composable
private fun AvatarChoiceItem(
    avatar: AvatarCatalogItem,
    modifier: Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = androidx.compose.runtime.remember(avatar.id, avatar.storagePath) {
        profileAvatarShape(avatar.id, avatar.storagePath, cornerRadius = 10.dp)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                avatar.bgColor?.let(::parseHexColor)
                    ?: MaterialTheme.colorScheme.surfaceVariant,
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val imageUrl = avatarImageUrl(avatar)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = avatar.displayName,
                modifier = Modifier.fillMaxSize().clip(shape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = avatar.displayName,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                modifier = Modifier.size(30.dp),
            )
        }
    }
}
