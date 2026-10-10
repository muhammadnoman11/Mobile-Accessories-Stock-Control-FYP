package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.component.SearchBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// FILE: presentation/user/ProductListScreen.kt
// Full product catalogue — search by name/description,
// filter by category chip, tap a product to view details.
// ============================================================

@Composable
fun ProductListScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val query by viewModel.productQuery.collectAsStateWithLifecycle()
    val catFilter by viewModel.productCategory.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Browse Products",
                subtitle = "${products.size} items found",
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = { UserBottomNav(navController, Screen.ProductList.route) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
            // ── Search bar ────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SearchBar(
                    query = query,
                    onQueryChange = viewModel::onProductQueryChange,
                    placeholder = "Search accessories..."
                )
            }

            // ── Category filter chips ─────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = catFilter == -1,
                        onClick = { viewModel.onCategoryChange(-1) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = catFilter == cat.id,
                        onClick = { viewModel.onCategoryChange(cat.id) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.Divider)

            // ── Products list ─────────────────────────────────
            if (products.isEmpty()) {
                EmptyState(
                    message = "No products found",
                    icon = Icons.Default.SearchOff,
                    action = "Clear Search",
                    onAction = { viewModel.onProductQueryChange(""); viewModel.onCategoryChange(-1) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductListCard(
                            product = product,
                            onClick = {
                                navController.navigate(
                                    Screen.ProductDetail.createRoute(
                                        product.id
                                    )
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Horizontal product card for the list ─────────────────────
@Composable
private fun ProductListCard(product: ProductEntity, onClick: () -> Unit) {
    val isOutOfStock = product.stockQuantity == 0
    val isLowStock = product.stockQuantity in 1..product.lowStockThreshold

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 36.dp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Stock status badge
                    Surface(
                        color = when {
                            isOutOfStock -> AppColors.ErrorContainer
                            isLowStock -> AppColors.WarningContainer
                            else -> AppColors.SuccessContainer
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = when {
                                isOutOfStock -> "Out of Stock"
                                isLowStock -> "Low: ${product.stockQuantity}"
                                else -> "In Stock"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isOutOfStock -> AppColors.Error
                                isLowStock -> AppColors.Warning
                                else -> AppColors.Success
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = "Rs. ${"%,.0f".format(product.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.Primary
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextTertiary)
        }
    }
}

// ============================================================
// FILE: presentation/user/ProductDetailScreen.kt
// Full product detail view — description, price, stock,
// quantity selector, and "Place Order" button.
// ============================================================

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Int,
    viewModel: UserViewModel = hiltViewModel()
) {
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = product?.name ?: "Product Detail",
                onBack = { navController.navigateUp() }
            )
        }
    ) { padding ->
        if (product == null) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
            return@Scaffold
        }

        val p = product!!
        val isOutOfStock = p.stockQuantity == 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Product Image Header Banner ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    imageUrl = p.imageUrl,
                    contentDescription = p.name,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 80.dp
                )
                // Out of stock overlay
                if (isOutOfStock) {
                    Surface(
                        color = AppColors.ErrorContainer.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "OUT OF STOCK",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Error,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name + Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = p.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Rs. ${"%,.0f".format(p.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Primary
                    )
                }

                // Sub-category if available
                if (p.subCategory.isNotBlank()) {
                    Surface(color = AppColors.PrimaryContainer, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            p.subCategory, style = MaterialTheme.typography.labelMedium,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = AppColors.Divider)

                // Description
                Text(
                    "About this product",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                Text(
                    p.description,
                    style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary
                )

                HorizontalDivider(color = AppColors.Divider)

                // Stock info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailInfoItem(
                        "Availability",
                        if (isOutOfStock) "Out of Stock" else "${p.stockQuantity} units available",
                        if (isOutOfStock) AppColors.Error else AppColors.Success
                    )
                    DetailInfoItem(
                        "Price",
                        "Rs. ${"%,.0f".format(p.price)}", AppColors.Primary
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Action button ─────────────────────────────
                Button(
                    onClick = { navController.navigate(Screen.PlaceOrder.createRoute(p.id)) },
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOutOfStock) AppColors.TextTertiary else AppColors.Primary
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isOutOfStock) "Out of Stock" else "Place Order",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoItem(label: String, value: String, valueColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
        Text(
            value, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = valueColor
        )
    }
}
