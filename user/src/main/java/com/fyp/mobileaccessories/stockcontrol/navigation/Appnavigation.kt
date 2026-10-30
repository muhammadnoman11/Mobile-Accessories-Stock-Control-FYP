package com.fyp.mobileaccessories.stockcontrol.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.UserViewModel

@Composable
fun AppNavigation(
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

@Composable
private fun RealtimeSessionObserver(navController: NavHostController) {
    val viewModel: UserViewModel = hiltViewModel()
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
