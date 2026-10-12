package com.fyp.mobileaccessories.stockcontrol.ui.screen.auth

// ============================================================
// FILE: presentation/auth/RegisterScreen.kt
// User self-registration form — firstName, lastName, gender,
// address, contactNo, email, password, confirmPassword.
// Self-registration always creates USER role.
// ============================================================

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.navigation.navigateAndClear
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsStateWithLifecycle()

    // After success, auto login and navigate to home screen
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigateAndClear(Screen.UserHome.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        LaunchedEffect(state.error) {
            if (state.error != null) {
                scrollState.animateScrollTo(0)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header text
            Column {
                Text("Join StockControl",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnBackground)
                Text("Fill in your details to create an account",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary)
            }

            // Error Banner
            if (state.error != null) {
                Surface(
                    color = AppColors.ErrorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = AppColors.Error,
                            modifier = Modifier.size(16.dp))
                        Text(state.error!!, style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Error)
                    }
                }
            }

            // ── Section: Personal Info ────────────────────────
            SectionLabel("Personal Information")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = state.firstName,
                    onValueChange = viewModel::onRegFirstName,
                    label = "First Name",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = state.lastName,
                    onValueChange = viewModel::onRegLastName,
                    label = "Last Name",
                    modifier = Modifier.weight(1f)
                )
            }

            // Gender dropdown
            DropdownField(
                label = "Gender",
                selectedValue = state.gender,
                options = listOf("Male", "Female", "Other"),
                onSelect = viewModel::onRegGender
            )

            // ── Section: Contact Info ─────────────────────────
            SectionLabel("Contact Information")
            AppTextField(
                value = state.address,
                onValueChange = viewModel::onRegAddress,
                label = "Full Address",
                leadingIcon = Icons.Default.LocationOn,
                singleLine = false,
                maxLines = 2
            )
            AppTextField(
                value = state.contactNo,
                onValueChange = viewModel::onRegContactNo,
                label = "Contact Number",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )

            // ── Section: Account Credentials ─────────────────
            SectionLabel("Account Credentials")
            AppTextField(
                value = state.email,
                onValueChange = viewModel::onRegEmail,
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )
            AppTextField(
                value = state.password,
                onValueChange = viewModel::onRegPassword,
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )
            AppTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onRegConfirmPassword,
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            // Role notice
            Surface(
                color = AppColors.InfoContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = AppColors.Info,
                        modifier = Modifier.size(16.dp))
                    Text("Your account will be registered as a Customer (USER) role.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Info)
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Create Account",
                onClick = { viewModel.register { navController.navigateUp() } },
                isLoading = state.isLoading,
                icon = Icons.Default.PersonAdd
            )

            // Back to login link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary)
                TextButton(onClick = { navController.navigateUp() }) {
                    Text("Sign In", fontWeight = FontWeight.Bold, color = AppColors.Primary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// Small helper composable for section labels in the form
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.Primary
    )
    HorizontalDivider(color = AppColors.Divider)
}