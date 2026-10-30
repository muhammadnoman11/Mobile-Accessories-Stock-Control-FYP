package com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnnouncementsCarousel(announcements: List<AnnouncementEntity>) {
    val pagerState = rememberPagerState { announcements.size }
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp
        ) { page ->
            val ann = announcements[page]
            val (bg, accent) = when (ann.type) {
                "SALE" -> AppColors.SuccessContainer to AppColors.Success
                "ALERT" -> AppColors.ErrorContainer to AppColors.Error
                "UPDATE" -> AppColors.InfoContainer to AppColors.Info
                else -> AppColors.PrimaryContainer to AppColors.Primary
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (ann.type == "WELCOME") Icons.Default.Celebration else Icons.Default.Campaign,
                            null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            ann.type, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = accent
                        )
                    }
                    Text(
                        ann.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = accent
                    )
                    Text(
                        ann.content, style = MaterialTheme.typography.bodySmall,
                        color = accent.copy(alpha = 0.8f), maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        DateUtils.formatDateTime(ann.dateCreated),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(announcements.size) { i ->
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .background(
                            if (isSelected) AppColors.Primary
                            else AppColors.Divider,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
