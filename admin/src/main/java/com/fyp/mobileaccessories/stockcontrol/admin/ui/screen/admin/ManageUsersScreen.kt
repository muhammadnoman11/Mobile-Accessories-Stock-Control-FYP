package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.AddEditUserSheet
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.SearchBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.UserCard
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    navController: NavController,
    roleFilter: String,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val query by viewModel.userSearchQuery.collectAsStateWithLifecycle()
    val allUsers by viewModel.users.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val users = remember(allUsers, roleFilter) {
        allUsers.filter { it.role == roleFilter }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<UserEntity?>(null) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotBlank()) {
            snackbarState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

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
            OfflineBanner(isOffline = !isOnline)
            Spacer(Modifier.height(12.dp))

            SearchBar(
                query = query,
                onQueryChange = viewModel::onUserSearchChange,
                placeholder = "Search by name, email or contact..."
            )
            Spacer(Modifier.height(12.dp))

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
