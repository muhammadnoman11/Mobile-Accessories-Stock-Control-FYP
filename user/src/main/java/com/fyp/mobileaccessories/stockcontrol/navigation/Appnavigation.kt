package com.fyp.mobileaccessories.stockcontrol.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.SplashScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.auth.LoginScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.auth.RegisterScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.AnnouncementsScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.MyOrdersScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.PlaceOrderScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.ProductDetailScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.ProductListScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.UserHomeScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.UserProfileScreen

@Composable
fun AppNavigation(
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

        // ── USER (CUSTOMER) ───────────────────────
        composable(Screen.UserHome.route) {
            UserHomeScreen(navController = navController)
        }
        composable(Screen.ProductList.route) {
            ProductListScreen(navController = navController)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductDetailScreen(navController = navController, productId = productId)
        }
        composable(
            route = Screen.PlaceOrder.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            PlaceOrderScreen(navController = navController, productId = productId)
        }
        composable(Screen.MyOrders.route) {
            MyOrdersScreen(navController = navController)
        }
        composable(Screen.UserProfile.route) {
            UserProfileScreen(navController = navController)
        }
        composable(Screen.Announcements.route) {
            AnnouncementsScreen(navController = navController)
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