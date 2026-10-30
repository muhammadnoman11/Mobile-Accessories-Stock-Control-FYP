package com.fyp.mobileaccessories.stockcontrol.ui.screen

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.R
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.navigation.navigateAndClear
import com.fyp.mobileaccessories.stockcontrol.ui.component.BaseSplashScreen
import com.fyp.mobileaccessories.stockcontrol.ui.screen.auth.AuthViewModel
import com.fyp.mobileaccessories.stockcontrol.util.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    var minDelayPassed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500L)
        minDelayPassed = true
    }

    LaunchedEffect(session, minDelayPassed) {
        if (!minDelayPassed) return@LaunchedEffect
        val destination = when {
            session == null -> Screen.Login.route
            session!!.role == UserRole.USER -> Screen.UserHome.route
            else -> {
                viewModel.logout { }
                Screen.Login.route
            }
        }
        navController.navigateAndClear(destination)
    }

    BaseSplashScreen(
        appTitle = "StockControl",
        subtitle = "Mobile Accessories",
        iconResId = R.drawable.user_icon
    )
}
