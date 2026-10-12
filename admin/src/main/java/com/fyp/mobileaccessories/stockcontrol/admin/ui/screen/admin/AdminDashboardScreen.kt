package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.UserRole
import java.util.Calendar

// ============================================================
// FILE: presentation/admin/AdminDashboardScreen.kt
// Admin home — shows key metrics (users, products, orders, revenue)
// and quick-access buttons to every admin section.
// ============================================================

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // Load fresh stats whenever screen is composed
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
            // ── Unified Gradient Header ───────────────────────
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

            // ── Stats Grid ────────────────────────────────────
            Text(
                "  Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)  // Inside scrollable Column
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                item {
                    StatCard(
                        title = "Total Users",
                        value = stats.totalUsers.toString(),
                        icon = Icons.Default.People,
                        containerColor = AppColors.PrimaryContainer,
                        contentColor = AppColors.Primary,
                        onClick = { if (!isEmployee) navController.navigate(Screen.ManageUsers.route) }
                    )
                }
                item {
                    StatCard(
                        title = "Moderators",
                        value = stats.totalModerators.toString(),
                        icon = Icons.Default.SupervisorAccount,
                        containerColor = AppColors.WarningContainer,
                        contentColor = AppColors.Warning,
                        onClick = { if (!isEmployee) navController.navigate(Screen.ManageModerators.route) }
                    )
                }
                item {
                    StatCard(
                        title = "Products",
                        value = stats.totalProducts.toString(),
                        icon = Icons.Default.Inventory2,
                        containerColor = AppColors.SuccessContainer,
                        contentColor = AppColors.Success,
                        onClick = { navController.navigate(Screen.ManageProducts.route) }
                    )
                }
                item {
                    StatCard(
                        title = "Low Stock",
                        value = stats.lowStockCount.toString(),
                        icon = Icons.Default.Warning,
                        containerColor = AppColors.ErrorContainer,
                        contentColor = AppColors.Error,
                        onClick = { navController.navigate(Screen.StockReport.route) }
                    )
                }
                item {
                    StatCard(
                        title = "Pending Orders",
                        value = stats.pendingOrders.toString(),
                        icon = Icons.Default.ShoppingCart,
                        containerColor = AppColors.InfoContainer,
                        contentColor = AppColors.Info,
                        onClick = { if (!isEmployee) navController.navigate(Screen.AdminOrders.route) }
                    )
                }
                item {
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

            Spacer(Modifier.height(24.dp))

            // ── Quick Actions ─────────────────────────────────
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

            // Grid of navigation buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 800.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(filteredMenuItems) { item ->
                    QuickActionButton(item = item, onClick = { navController.navigate(item.route) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Quick Action Button ───────────────────────────────────────
@Composable
private fun QuickActionButton(item: AdminMenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = item.bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                item.icon, contentDescription = item.label,
                tint = item.iconColor, modifier = Modifier.size(28.dp)
            )
            Text(
                item.label, style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold, color = item.iconColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Menu item data class
private data class AdminMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val bgColor: Color,
    val iconColor: Color
)
private val adminMenuItems = listOf(
    AdminMenuItem(
        "Manage Users",
        Icons.Default.People,
        Screen.ManageUsers.route,
        AppColors.PrimaryContainer,
        AppColors.Primary
    ),
    AdminMenuItem(
        "Moderators",
        Icons.Default.SupervisorAccount,
        Screen.ManageModerators.route,
        AppColors.WarningContainer,
        AppColors.Warning
    ),
    AdminMenuItem(
        "Employees",
        Icons.Default.AssignmentInd,
        Screen.ManageEmployees.route,
        AppColors.SuccessContainer,
        AppColors.Success
    ),
    AdminMenuItem(
        "Products",
        Icons.Default.Inventory2,
        Screen.ManageProducts.route,
        AppColors.SuccessContainer,
        AppColors.Success
    ),
    AdminMenuItem(
        "Categories",
        Icons.Default.Category,
        Screen.ManageCategories.route,
        AppColors.InfoContainer,
        AppColors.Info
    ),
    AdminMenuItem(
        "Orders",
        Icons.Default.ShoppingCart,
        Screen.AdminOrders.route,
        AppColors.PrimaryContainer,
        AppColors.Primary
    ),
    AdminMenuItem(
        "Payments",
        Icons.Default.Payments,
        Screen.ManagePayments.route,
        AppColors.SuccessContainer,
        AppColors.Success
    ),
    AdminMenuItem(
        "Couriers",
        Icons.Default.LocalShipping,
        Screen.ManageCouriers.route,
        AppColors.WarningContainer,
        AppColors.Warning
    ),
    AdminMenuItem(
        "Announcements",
        Icons.Default.Campaign,
        Screen.ManageAnnouncements.route,
        AppColors.InfoContainer,
        AppColors.Info
    ),
    AdminMenuItem(
        "Stock Report",
        Icons.Default.BarChart,
        Screen.StockReport.route,
        AppColors.ErrorContainer,
        AppColors.Error
    ),
    AdminMenuItem(
        "My Profile",
        Icons.Default.AccountCircle,
        Screen.AdminProfile.route,
        AppColors.SurfaceVariant,
        AppColors.TextSecondary
    ),
)

// Helper: greeting based on current time of day
private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
