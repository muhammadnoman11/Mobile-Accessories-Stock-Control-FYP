package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// FILE: presentation/user/AnnouncementsScreen.kt
// Displays all active announcements to customers.
// ============================================================

@Composable
fun AnnouncementsScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Announcements",
                subtitle = "${announcements.size} active",
                onBack = { navController.navigateUp() }
            )
        }
    ) { padding ->
        if (announcements.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                EmptyState("No announcements at this time", Icons.Default.Campaign)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    val (bg, accent) = when (ann.type) {
                        "SALE" -> AppColors.SuccessContainer to AppColors.Success
                        "ALERT" -> AppColors.ErrorContainer to AppColors.Error
                        "UPDATE" -> AppColors.InfoContainer to AppColors.Info
                        else -> AppColors.PrimaryContainer to AppColors.Primary
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Campaign, null, tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Surface(
                                    color = accent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        ann.type, style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold, color = accent,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 3.dp
                                        )
                                    )
                                }
                            }
                            Text(
                                ann.title, style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold, color = accent
                            )
                            Text(
                                ann.content, style = MaterialTheme.typography.bodyMedium,
                                color = accent.copy(alpha = 0.85f)
                            )
                            Text(
                                DateUtils.formatDateTime(ann.dateCreated),
                                style = MaterialTheme.typography.labelSmall,
                                color = accent.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}