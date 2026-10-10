package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// AdminProfileScreen
// ============================================================

@Composable
fun AdminProfileScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "My Profile", onBack = { navController.navigateUp() })
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
            Spacer(Modifier.height(16.dp))
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        AppColors.Primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session?.name?.take(1)?.uppercase() ?: "A",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Text(
                session?.name ?: "", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(color = AppColors.AdminBadgeContainer, shape = RoundedCornerShape(20.dp)) {
                Text(
                    "ADMIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.AdminBadge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Text(
                session?.email ?: "", style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )

            Spacer(Modifier.height(16.dp))
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
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}