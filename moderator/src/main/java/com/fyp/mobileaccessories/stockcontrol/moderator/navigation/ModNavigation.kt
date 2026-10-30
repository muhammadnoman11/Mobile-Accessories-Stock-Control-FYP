package com.fyp.mobileaccessories.stockcontrol.moderator.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.SplashScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth.LoginScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth.RegisterScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ApproveProductsScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModDashboardScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModProfileScreen
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModViewModel
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.TrackOrdersScreen

@Composable
fun ModNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    RealtimeSessionObserver(navController = navController)

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

@Composable
private fun RealtimeSessionObserver(navController: NavHostController) {
    val viewModel: ModViewModel = hiltViewModel()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(session, currentRoute) {
        if (currentRoute == Screen.Login.route ||
            currentRoute == Screen.Register.route ||
            currentRoute == Screen.Splash.route ||
            currentRoute == null
        ) {
            return@LaunchedEffect
        }

        if (session == null) {
            Toast.makeText(context, "Your account has been deleted by admin", Toast.LENGTH_LONG).show()
            navController.navigateAndClear(Screen.Login.route)
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
