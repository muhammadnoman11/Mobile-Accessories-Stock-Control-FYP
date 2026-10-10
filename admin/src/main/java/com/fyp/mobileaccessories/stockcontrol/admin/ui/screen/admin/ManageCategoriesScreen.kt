package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// ManageCategoriesScreen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Categories",
                subtitle = "${categories.size} categories", onBack = { navController.navigateUp() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showSheet = true },
                containerColor = AppColors.Primary
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(cat.name, fontWeight = FontWeight.Bold) },
                        leadingContent = {
                            Icon(Icons.Default.Category, null, tint = AppColors.Primary)
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editing = cat; showSheet = true }) {
                                    Icon(Icons.Default.Edit, null, tint = AppColors.Primary)
                                }
                                IconButton(onClick = { deleteTarget = cat }) {
                                    Icon(Icons.Default.Delete, null, tint = AppColors.Error)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showSheet) {
        CategorySheet(
            category = editing, onDismiss = { showSheet = false },
            onSave = { c ->
                viewModel.saveCategory(c) { ok, msg -> snack = msg; if (ok) showSheet = false }
            })
    }
    deleteTarget?.let { c ->
        ConfirmDialog(
            "Delete ${c.name}?", "Products in this category will be unlinked.",
            onConfirm = { viewModel.deleteCategory(c) { snack = "Deleted" }; deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (category == null) "Add Category" else "Edit Category",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            AppTextField(name, { name = it }, "Category Name")
            PrimaryButton("Save", onClick = {
                if (name.isBlank()) return@PrimaryButton
                onSave(
                    CategoryEntity(
                        id = category?.id ?: 0, name = name.trim()
                    )
                )
            })
            Spacer(Modifier.height(16.dp))
        }
    }
}
