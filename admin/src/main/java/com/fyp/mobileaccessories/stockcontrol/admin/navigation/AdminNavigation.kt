package com.fyp.mobileaccessories.stockcontrol.admin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.SplashScreen
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.AddEditProductScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.AdminDashboardScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.AdminOrdersScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.AdminProfileScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManageAnnouncementsScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManageCategoriesScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManageCouriersScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManagePaymentsScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManageProductsScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.ManageUsersScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.StockReportScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.auth.LoginScreen
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.auth.RegisterScreen
import com.fyp.mobileaccessories.stockcontrol.util.UserRole

@Composable
fun AdminNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── AUTH ──────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // ── ADMIN ─────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(navController = navController, roleFilter = UserRole.USER)
        }
        composable(Screen.ManageModerators.route) {
            ManageUsersScreen(navController = navController, roleFilter = UserRole.MODERATOR)
        }
        composable(Screen.ManageEmployees.route) {
            ManageUsersScreen(navController = navController, roleFilter = UserRole.EMPLOYEE)
        }
        composable(Screen.ManageProducts.route) {
            ManageProductsScreen(navController = navController)
        }
        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            AddEditProductScreen(navController = navController, productId = productId)
        }
        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(navController = navController)
        }
        composable(Screen.ManagePayments.route) {
            ManagePaymentsScreen(navController = navController)
        }
        composable(Screen.ManageCouriers.route) {
            ManageCouriersScreen(navController = navController)
        }
        composable(Screen.ManageAnnouncements.route) {
            ManageAnnouncementsScreen(navController = navController)
        }
        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(navController = navController)
        }
        composable(Screen.StockReport.route) {
            StockReportScreen(navController = navController)
        }
        composable(Screen.AdminProfile.route) {
            AdminProfileScreen(navController = navController)
        }
    }
}

// Helper extension to navigate and clear back stack
fun NavController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
