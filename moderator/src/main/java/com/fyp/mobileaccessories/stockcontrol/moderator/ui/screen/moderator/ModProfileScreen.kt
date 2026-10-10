package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// ModProfileScreen
// ============================================================

@Composable
fun ModProfileScreen(
    navController: NavController,
    viewModel: ModViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Profile", onBack = { navController.navigateUp() })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFFD97706),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    session?.name?.take(1)?.uppercase() ?: "M",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Text(
                session?.name ?: "", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(color = AppColors.ModBadgeContainer, shape = RoundedCornerShape(20.dp)) {
                Text(
                    "MODERATOR",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.ModBadge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Text(
                session?.email ?: "", style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    viewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(
                                0
                            )
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Sign Out")
            }
        }
    }
}