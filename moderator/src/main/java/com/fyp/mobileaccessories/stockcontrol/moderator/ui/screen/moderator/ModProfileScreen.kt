package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// ModProfileScreen
// ============================================================

@Composable
fun ModProfileScreen(
    navController: NavController,
    viewModel: ModViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val modProfile by viewModel.moderatorProfile.collectAsStateWithLifecycle()

    val firstName = modProfile?.firstName ?: session?.name?.substringBefore(" ") ?: "Moderator"
    val lastName = modProfile?.lastName ?: session?.name?.substringAfter(" ", "") ?: ""

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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
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
                    text = "${firstName.firstOrNull() ?: 'M'}${lastName.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$firstName $lastName".trim(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(color = AppColors.ModBadgeContainer, shape = RoundedCornerShape(20.dp)) {
                Text(
                    text = "MODERATOR${modProfile?.domain?.let { " • $it" } ?: ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.ModBadge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Text(
                text = modProfile?.email ?: session?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.Divider)

            // Profile details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoRow(Icons.Default.Phone, "Contact No", modProfile?.contactNo ?: "—")
                ProfileInfoRow(Icons.Default.LocationOn, "Address", modProfile?.address ?: "—")
                ProfileInfoRow(Icons.Default.Category, "Product Domain", modProfile?.domain ?: "—")
                ProfileInfoRow(Icons.Default.Male, "Gender", modProfile?.gender ?: "—")
                ProfileInfoRow(
                    Icons.Default.CalendarToday, "Member Since",
                    DateUtils.formatDate(modProfile?.dateOfRegistration ?: System.currentTimeMillis())
                )
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
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

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String, value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = Color(0xFFD97706), modifier = Modifier
            .size(18.dp)
            .offset(y = 2.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary
            )
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
