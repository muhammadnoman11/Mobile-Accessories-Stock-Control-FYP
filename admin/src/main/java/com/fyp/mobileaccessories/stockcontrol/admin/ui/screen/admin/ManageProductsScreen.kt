package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.LoadingScreen
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductCard
import com.fyp.mobileaccessories.stockcontrol.ui.component.SearchBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// FILE: presentation/admin/ManageProductsScreen.kt
// Full product management: search by name/description/subcategory,
// filter by category, approve/reject, add/edit/delete.
// ============================================================

@Composable
fun ManageProductsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val query by viewModel.productQuery.collectAsStateWithLifecycle()
    val catFilter by viewModel.productCategory.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isEmployee = session?.role == com.fyp.mobileaccessories.stockcontrol.util.UserRole.EMPLOYEE

    var deleteTarget by remember { mutableStateOf<ProductEntity?>(null) }
    var snackMessage by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        if (snackMessage.isNotBlank()) {
            snackState.showSnackbar(snackMessage)
            snackMessage = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Products",
                subtitle = "${products.size} products",
                onBack = { navController.navigateUp() }
            )
        },
        floatingActionButton = {
            if (!isEmployee) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEditProduct.createRoute(-1)) },
                    containerColor = AppColors.Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                // Search
                SearchBar(
                    query = query,
                    onQueryChange = viewModel::onProductQueryChange,
                    placeholder = "Search products..."
                )
                Spacer(Modifier.height(8.dp))
            }

            // Category filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = catFilter == -1,
                        onClick = { viewModel.onProductCategoryChange(-1) },
                        label = { Text("All") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = catFilter == cat.id,
                        onClick = { viewModel.onProductCategoryChange(cat.id) },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (products.isEmpty()) {
                EmptyState(
                    message = "No products found",
                    icon = Icons.Default.Inventory2,
                    action = "Add Product",
                    onAction = { navController.navigate(Screen.AddEditProduct.createRoute(-1)) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        // Find category name
                        val catName = categories.find { it.id == product.categoryId }?.name ?: "—"
                        ProductCard(
                            name = product.name,
                            description = "${product.description} • $catName",
                            price = product.price,
                            stockQuantity = product.stockQuantity,
                            lowStockThreshold = product.lowStockThreshold,
                            imageUrl = product.imageUrl,
                            isApproved = product.isApproved,
                            onEdit = if (isEmployee) null else {
                                {
                                    navController.navigate(
                                        Screen.AddEditProduct.createRoute(
                                            product.id
                                        )
                                    )
                                }
                            },
                            onDelete = if (isEmployee) null else { { deleteTarget = product } }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Delete confirmation
    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete \"${target.name}\"?",
            message = "All order data linked to this product will also be affected.",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteProduct(target) { snackMessage = "Product deleted" }
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
