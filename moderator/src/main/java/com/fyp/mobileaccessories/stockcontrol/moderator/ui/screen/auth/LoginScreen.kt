package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.moderator.navigation.navigateAndClear
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// FILE: presentation/auth/LoginScreen.kt
// Login screen — email + password, role-based navigation on success
// ============================================================

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.loginState.collectAsStateWithLifecycle()

    // After successful login, navigate to the correct role dashboard
    fun onLoginSuccess(role: String) {
        navController.navigateAndClear(Screen.ModDashboard.route)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.Primary, AppColors.CardGradientEnd)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // App Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inventory2, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "StockControl", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = Color.White
            )
            Text(
                "Moderator Control Panel", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = Color.White
            )
            Text(
                "Sign in to approve products and track deliveries", style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(40.dp))

            // ── Login Card ────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnBackground
                    )
                    Text(
                        "Enter your credentials to access your account",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    // Error message
                    if (state.error != null) {
                        Surface(
                            color = AppColors.ErrorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error, contentDescription = null,
                                    tint = AppColors.Error, modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    state.error!!, style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.Error
                                )
                            }
                        }
                    }

                    // Email field
                    AppTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = state.error != null
                    )

                    // Password field
                    AppTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isError = state.error != null
                    )

                    // Login button
                    PrimaryButton(
                        text = "Sign In",
                        onClick = { viewModel.login(::onLoginSuccess) },
                        isLoading = state.isLoading,
                        icon = Icons.Default.Login
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Default credential hint
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.InfoContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Demo Credentials",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = AppColors.Info
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Moderator: mod@stock.com / Mod@123",
                        style = MaterialTheme.typography.bodySmall, color = AppColors.Info
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
