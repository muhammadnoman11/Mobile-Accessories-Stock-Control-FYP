package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import java.util.Calendar

data class AdminMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val bgColor: Color,
    val iconColor: Color
)

val adminMenuItems = listOf(
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
    )
)

@Composable
fun QuickActionButton(item: AdminMenuItem, onClick: () -> Unit) {
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
                textAlign = TextAlign.Center
            )
        }
    }
}

fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
