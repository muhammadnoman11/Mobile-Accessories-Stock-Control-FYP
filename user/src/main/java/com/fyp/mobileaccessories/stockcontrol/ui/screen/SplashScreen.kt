package com.fyp.mobileaccessories.stockcontrol.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.navigation.navigateAndClear
import com.fyp.mobileaccessories.stockcontrol.ui.screen.auth.AuthViewModel
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.fyp.mobileaccessories.stockcontrol.R

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Collect the session from DataStore
    val session by viewModel.session.collectAsStateWithLifecycle()
    // Flag — wait at least 1.5 s so the splash doesn't flash
    var minDelayPassed by remember { mutableStateOf(false) }

    // Logo pulse animation
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.95f,
        targetValue  = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Minimum display time so it doesn't flash for fast devices
    LaunchedEffect(Unit) {
        delay(1500L)
        minDelayPassed = true
    }

    // Once both conditions are met, route to correct screen
    LaunchedEffect(session, minDelayPassed) {
        if (!minDelayPassed) return@LaunchedEffect
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val destination = when {
            session == null || firebaseUser == null -> Screen.Login.route
            session!!.role == UserRole.USER -> Screen.UserHome.route
            else -> {
                viewModel.logout { }
                Screen.Login.route
            }
        }
        navController.navigateAndClear(destination)
    }

    // ── Splash UI ─────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App icon inside a circle
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.user_icon),
                    contentDescription = "Stock Control Admin",
                    modifier = Modifier.size(100.dp)
                )
            }

            // App name and tagline
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "StockControl",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Mobile Accessories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(Modifier.height(32.dp))
            // Loading indicator
            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }

        // Version text at bottom
        Text(
            text = "v1.0  ·  FYP Project",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
