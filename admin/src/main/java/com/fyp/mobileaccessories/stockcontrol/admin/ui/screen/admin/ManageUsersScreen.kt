package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.component.SearchBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.UserCard
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import com.fyp.mobileaccessories.stockcontrol.util.UserRole

// ============================================================
// FILE: presentation/admin/ManageUsersScreen.kt
// Reused for both "Manage Users" (roleFilter="USER") and
// "Manage Moderators" (roleFilter="MODERATOR").
// Features: search by name/email/contact, add/edit via bottom
// sheet dialog, deactivate (soft delete), hard delete.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    navController: NavController,
    roleFilter: String,                     // "USER" or "MODERATOR"
    viewModel: AdminViewModel = hiltViewModel()
) {
    val query by viewModel.userSearchQuery.collectAsStateWithLifecycle()
    val allUsers by viewModel.users.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // Filter locally to show only the target role
    val users = remember(allUsers, roleFilter) {
        allUsers.filter { it.role == roleFilter }
    }

    // Dialog state
    var showAddEdit by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<UserEntity?>(null) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarState = remember { SnackbarHostState() }

    // Show snackbar on message
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotBlank()) {
            snackbarState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

    // Set role filter in ViewModel so search scopes correctly
    LaunchedEffect(roleFilter) { viewModel.onUserRoleFilter(roleFilter) }

    val screenTitle = when (roleFilter) {
        UserRole.MODERATOR -> "Manage Moderators"
        UserRole.EMPLOYEE -> "Manage Employees"
        else -> "Manage Users"
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = screenTitle,
                subtitle = "${users.size} record(s)",
                onBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingUser = null; showAddEdit = true },
                containerColor = AppColors.Primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
            Spacer(Modifier.height(12.dp))

            // Search bar
            SearchBar(
                query = query,
                onQueryChange = viewModel::onUserSearchChange,
                placeholder = "Search by name, email or contact..."
            )
            Spacer(Modifier.height(12.dp))

            // User list
            if (users.isEmpty()) {
                EmptyState(
                    message = "No ${if (roleFilter == UserRole.MODERATOR) "moderators" else "users"} found",
                    icon = Icons.Default.PeopleAlt,
                    action = "Add New",
                    onAction = { editingUser = null; showAddEdit = true }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(users, key = { it.id }) { user ->
                        UserCard(
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            contactNo = user.contactNo,
                            role = user.role,
                            isActive = user.isActive,
                            onEdit = { editingUser = user; showAddEdit = true },
                            onDelete = { deleteTarget = user }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── Add / Edit Bottom Sheet ───────────────────────────────
    if (showAddEdit) {
        AddEditUserSheet(
            user = editingUser,
            role = roleFilter,
            onDismiss = { showAddEdit = false },
            onSave = { entity, plainPass ->
                viewModel.saveUser(entity, plainPass) { ok, msg ->
                    snackbarMessage = msg
                    if (ok) showAddEdit = false
                }
            }
        )
    }

    // ── Delete Confirm Dialog ─────────────────────────────────
    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete ${target.firstName}?",
            message = "This will permanently remove the account. Deactivate instead to keep data.",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteUser(target) { snackbarMessage = "User deleted" }
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

// ── Add / Edit User — ModalBottomSheet ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditUserSheet(
    user: UserEntity?,         // null = add new
    role: String,              // target role
    onDismiss: () -> Unit,
    onSave: (UserEntity, String) -> Unit
) {
    // Pre-fill if editing
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "Male") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var contactNo by remember { mutableStateOf(user?.contactNo ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }   // blank = no change on edit
    var domain by remember { mutableStateOf(user?.domain ?: "") }
    var error by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val roleLabel = when (role) {
                UserRole.MODERATOR -> "Moderator"
                UserRole.EMPLOYEE -> "Employee"
                else -> "User"
            }
            Text(
                if (user == null) "Add New $roleLabel"
                else "Edit ${user.firstName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (error.isNotBlank()) {
                Text(error, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    firstName,
                    { firstName = it },
                    "First Name",
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    lastName,
                    { lastName = it },
                    "Last Name",
                    modifier = Modifier.weight(1f)
                )
            }
            DropdownField("Gender", gender, listOf("Male", "Female", "Other"), { gender = it })
            AppTextField(address, { address = it }, "Address", singleLine = false, maxLines = 2)
            AppTextField(
                contactNo, { contactNo = it }, "Contact No",
                leadingIcon = Icons.Default.Phone, keyboardType = KeyboardType.Phone
            )
            AppTextField(
                email, { email = it }, "Email",
                leadingIcon = Icons.Default.Email, keyboardType = KeyboardType.Email
            )
            AppTextField(
                password,
                { password = it },
                if (user == null) "Password" else "New Password (leave blank to keep)",
                leadingIcon = Icons.Default.Lock,
                isPassword = true
            )

            // Domain field only for Moderators
            if (role == UserRole.MODERATOR) {
                AppTextField(
                    domain, { domain = it }, "Product Domain (e.g. Mobile Accessories)",
                    leadingIcon = Icons.Default.Category
                )
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (user == null) "Add $roleLabel" else "Save Changes",
                onClick = {
                    // Validation
                    error = when {
                        firstName.isBlank() -> "First name required"
                        lastName.isBlank() -> "Last name required"
                        email.isBlank() -> "Email required"
                        user == null && password.length < 6 -> "Password must be 6+ chars"
                        else -> ""
                    }
                    if (error.isNotBlank()) return@PrimaryButton
                    // Build entity
                    val hash = if (password.isNotBlank()) PasswordHasher.hash(password)
                    else user?.passwordHash ?: ""
                    onSave(
                        UserEntity(
                            id = user?.id ?: 0,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            gender = gender,
                            address = address.trim(),
                            contactNo = contactNo.trim(),
                            email = email.trim(),
                            passwordHash = hash,
                            role = role,
                            domain = domain.ifBlank { null },
                            isActive = user?.isActive ?: true,
                            dateOfRegistration = user?.dateOfRegistration
                                ?: System.currentTimeMillis()
                        ),
                        password
                    )
                }
            )
        }
    }
}