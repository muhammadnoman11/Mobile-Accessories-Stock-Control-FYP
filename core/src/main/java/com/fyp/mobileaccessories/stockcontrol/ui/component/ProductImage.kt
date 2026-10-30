package com.fyp.mobileaccessories.stockcontrol.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

@Composable
fun ProductImage(
    imageUrl: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    iconSize: Dp = 36.dp
) {
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(AppColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
