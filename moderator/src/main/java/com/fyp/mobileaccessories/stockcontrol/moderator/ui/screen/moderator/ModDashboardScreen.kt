package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.RequestNotificationPermissionEffect
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatCard
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// FILE: presentation/moderator/ModDashboardScreen.kt
// ============================================================

@Composable
fun ModDashboardScreen(
    navController: NavController,
    viewModel: ModViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val pendingProds by viewModel.pendingProductCount.collectAsStateWithLifecycle()
    val pendingOrds by viewModel.pendingOrderCount.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    RequestNotificationPermissionEffect()

    LaunchedEffect(Unit) { viewModel.loadDashboardStats() }

    Scaffold(
        topBar = {}
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            OfflineBanner(isOffline = !isOnline)
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD97706), Color(0xFFEA580C))
                        )
                    )
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Moderator Panel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "Welcome, ${session?.name ?: "Moderator"} 👋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                    Text(
                        "Domain: ${session?.name ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "  Tasks Overview",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Products to Review",
                    value = pendingProds.toString(),
                    icon = Icons.Default.Pending,
                    containerColor = AppColors.WarningContainer,
                    contentColor = AppColors.Warning,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.ApproveProducts.route) }
                )
                StatCard(
                    title = "Orders to Track",
                    value = pendingOrds.toString(),
                    icon = Icons.Default.LocalShipping,
                    containerColor = AppColors.InfoContainer,
                    contentColor = AppColors.Info,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.TrackOrders.route) }
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "  Quick Actions",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))

            // Action cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModActionCard(
                    title = "Approve / Reject Products",
                    subtitle = "$pendingProds products awaiting your review",
                    icon = Icons.Default.CheckCircle,
                    color = AppColors.Warning,
                    onClick = { navController.navigate(Screen.ApproveProducts.route) }
                )
                ModActionCard(
                    title = "Track & Update Orders",
                    subtitle = "Confirm dispatch and delivery status",
                    icon = Icons.Default.LocalShipping,
                    color = AppColors.Info,
                    onClick = { navController.navigate(Screen.TrackOrders.route) }
                )
                ModActionCard(
                    title = "My Profile",
                    subtitle = "View and edit your account details",
                    icon = Icons.Default.AccountCircle,
                    color = AppColors.Primary,
                    onClick = { navController.navigate(Screen.ModProfile.route) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModActionCard(
    title: String, subtitle: String,
    icon: ImageVector,
    color: Color, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = color, modifier = Modifier.size(28.dp)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle, style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextTertiary)
        }
    }
}
