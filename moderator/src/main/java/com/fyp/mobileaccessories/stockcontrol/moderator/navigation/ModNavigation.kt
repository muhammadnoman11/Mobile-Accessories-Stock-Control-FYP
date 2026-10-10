package com.fyp.mobileaccessories.stockcontrol.moderator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.SplashScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth.LoginScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth.RegisterScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ApproveProductsScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModDashboardScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModProfileScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.TrackOrdersScreen

@Composable
fun ModNavigation(
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

        // ── MODERATOR ─────────────────────────────
        composable(Screen.ModDashboard.route) {
            ModDashboardScreen(navController = navController)
        }
        composable(Screen.ApproveProducts.route) {
            ApproveProductsScreen(navController = navController)
        }
        composable(Screen.TrackOrders.route) {
            TrackOrdersScreen(navController = navController)
        }
        composable(Screen.ModProfile.route) {
            ModProfileScreen(navController = navController)
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
