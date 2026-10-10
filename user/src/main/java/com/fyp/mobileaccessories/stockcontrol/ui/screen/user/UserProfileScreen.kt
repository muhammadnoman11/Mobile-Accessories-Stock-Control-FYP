package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// FILE: presentation/user/UserProfileScreen.kt
// Customer profile — view and edit name, address, contact,
// change password. All saved to Room DB via UserRepository.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    val snackState = remember { SnackbarHostState() }
    var snack by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    // Editable fields
    var firstName by remember(profile) { mutableStateOf(profile?.firstName ?: "") }
    var lastName by remember(profile) { mutableStateOf(profile?.lastName ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var contactNo by remember(profile) { mutableStateOf(profile?.contactNo ?: "") }
    var newPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Profile",
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = { UserBottomNav(navController, Screen.UserProfile.route) },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Avatar + name header ──────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .run { background(AppColors.Primary, CircleShape) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "$firstName $lastName",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                )
                Text(
                    profile?.email ?: session?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary
                )
                Surface(color = AppColors.UserBadgeContainer, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        "CUSTOMER", style = MaterialTheme.typography.labelSmall,
                        color = AppColors.UserBadge, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = AppColors.Divider)

            // ── Edit toggle ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Account Information",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        null, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isEditing) "Cancel" else "Edit")
                }
            }

            if (isEditing) {
                // ── Edit mode ─────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = firstName, onValueChange = { firstName = it },
                        label = "First Name", modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = lastName, onValueChange = { lastName = it },
                        label = "Last Name", modifier = Modifier.weight(1f)
                    )
                }
                AppTextField(
                    value = address, onValueChange = { address = it },
                    label = "Address", leadingIcon = Icons.Default.LocationOn,
                    singleLine = false, maxLines = 2
                )
                AppTextField(
                    value = contactNo, onValueChange = { contactNo = it },
                    label = "Contact Number", leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                AppTextField(
                    value = newPassword, onValueChange = { newPassword = it },
                    label = "New Password (leave blank to keep)",
                    leadingIcon = Icons.Default.Lock, isPassword = true
                )

                Button(
                    onClick = {
                        viewModel.updateProfile(
                            firstName, lastName, address, contactNo, newPassword
                        ) { ok, msg ->
                            snack = msg
                            if (ok) {
                                isEditing = false; newPassword = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
                // ── View mode ─────────────────────────────────
                profile?.let { u ->
                    ProfileInfoRow(
                        Icons.Default.Person,
                        "Full Name",
                        "${u.firstName} ${u.lastName}"
                    )
                    ProfileInfoRow(Icons.Default.Email, "Email", u.email)
                    ProfileInfoRow(Icons.Default.Phone, "Contact No", u.contactNo)
                    ProfileInfoRow(Icons.Default.LocationOn, "Address", u.address)
                    ProfileInfoRow(Icons.Default.Male, "Gender", u.gender)
                    ProfileInfoRow(
                        Icons.Default.CalendarToday, "Member Since",
                        DateUtils.formatDate(u.dateOfRegistration)
                    )
                }
            }

            HorizontalDivider(color = AppColors.Divider)

            // ── Sign out button ───────────────────────────────
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
            Spacer(Modifier.height(16.dp))
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
        Icon(icon, null, tint = AppColors.Primary, modifier = Modifier
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


