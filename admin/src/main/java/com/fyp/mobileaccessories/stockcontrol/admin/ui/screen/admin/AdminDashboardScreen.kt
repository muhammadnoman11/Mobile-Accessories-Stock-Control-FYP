package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.QuickActionButton
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.adminMenuItems
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.greeting
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatCard
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.UserRole

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadDashboardStats() }

    val isEmployee = session?.role == UserRole.EMPLOYEE

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.Primary, AppColors.CardGradientEnd)
                        )
                    )
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (isEmployee) "Employee Console" else "Admin Dashboard",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        "Good ${greeting()}! ${session?.name ?: "User"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        DateUtils.formatDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "  Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Total Users",
                            value = stats.totalUsers.toString(),
                            icon = Icons.Default.People,
                            containerColor = AppColors.PrimaryContainer,
                            contentColor = AppColors.Primary,
                            onClick = { if (!isEmployee) navController.navigate(Screen.ManageUsers.route) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Moderators",
                            value = stats.totalModerators.toString(),
                            icon = Icons.Default.SupervisorAccount,
                            containerColor = AppColors.WarningContainer,
                            contentColor = AppColors.Warning,
                            onClick = { if (!isEmployee) navController.navigate(Screen.ManageModerators.route) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Products",
                            value = stats.totalProducts.toString(),
                            icon = Icons.Default.Inventory2,
                            containerColor = AppColors.SuccessContainer,
                            contentColor = AppColors.Success,
                            onClick = { navController.navigate(Screen.ManageProducts.route) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Low Stock",
                            value = stats.lowStockCount.toString(),
                            icon = Icons.Default.Warning,
                            containerColor = AppColors.ErrorContainer,
                            contentColor = AppColors.Error,
                            onClick = { navController.navigate(Screen.StockReport.route) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Pending Orders",
                            value = stats.pendingOrders.toString(),
                            icon = Icons.Default.ShoppingCart,
                            containerColor = AppColors.InfoContainer,
                            contentColor = AppColors.Info,
                            onClick = { if (!isEmployee) navController.navigate(Screen.AdminOrders.route) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatCard(
                            title = "Revenue (Rs.)",
                            value = "%,.0f".format(stats.totalRevenue),
                            icon = Icons.Default.AttachMoney,
                            containerColor = AppColors.SuccessContainer,
                            contentColor = AppColors.Success,
                            onClick = { if (!isEmployee) navController.navigate(Screen.ManagePayments.route) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "  Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            val filteredMenuItems = remember(session) {
                if (isEmployee) {
                    adminMenuItems.filter {
                        it.route == Screen.ManageProducts.route ||
                        it.route == Screen.ManageCategories.route ||
                        it.route == Screen.StockReport.route ||
                        it.route == Screen.AdminProfile.route
                    }
                } else {
                    adminMenuItems
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredMenuItems.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                QuickActionButton(item = item, onClick = { navController.navigate(item.route) })
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
