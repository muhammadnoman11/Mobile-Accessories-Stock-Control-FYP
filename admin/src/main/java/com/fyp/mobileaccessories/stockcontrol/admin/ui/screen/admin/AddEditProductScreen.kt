package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.LoadingScreen
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// AddEditProductScreen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    productId: Int,                         // -1 = new product
    viewModel: AdminViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // State for each field
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf<CategoryEntity?>(null) }
    var subCategory by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("5") }
    var isApproved by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(productId > 0) }
    var error by remember { mutableStateOf("") }

    val snackState = remember { SnackbarHostState() }

    // Load existing product data when editing
    LaunchedEffect(productId) {
        if (productId > 0) {
            val product = viewModel.getProductById(productId)
            product?.let { p ->
                name = p.name
                description = p.description
                imageUrl = p.imageUrl
                subCategory = p.subCategory
                price = p.price.toString()
                stock = p.stockQuantity.toString()
                threshold = p.lowStockThreshold.toString()
                isApproved = p.isApproved
                selectedCat = categories.find { it.id == p.categoryId }
            }
            isLoading = false
        }
    }

    // Re-match category after categories load
    LaunchedEffect(categories) {
        if (productId > 0 && selectedCat == null) {
            val product = viewModel.getProductById(productId)
            selectedCat = categories.find { it.id == product?.categoryId }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (productId <= 0) "Add Product" else "Edit Product",
                onBack = { navController.navigateUp() }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        if (isLoading) {
            LoadingScreen()
        } else {
            val scrollState = rememberScrollState()
            LaunchedEffect(error) {
                if (error.isNotBlank()) {
                    scrollState.animateScrollTo(0)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
                // Error
                if (error.isNotBlank()) {
                    Surface(color = AppColors.ErrorContainer, shape = MaterialTheme.shapes.small) {
                        Text(
                            error, color = AppColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                SectionLabelProd("Product Details")
                AppTextField(
                    name, { name = it }, "Product Name",
                    leadingIcon = Icons.Default.Inventory2
                )
                AppTextField(
                    description, { description = it }, "Description",
                    singleLine = false, maxLines = 3
                )
                AppTextField(
                    subCategory,
                    { subCategory = it },
                    "Sub-Category (e.g. USB-C Chargers)"
                )

                SectionLabelProd("Product Image")
                AppTextField(
                    imageUrl, { imageUrl = it }, "Image URL (http://... or https://...)",
                    leadingIcon = Icons.Default.Image
                )
                if (imageUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.SurfaceVariant)
                        ) {
                            ProductImage(
                                imageUrl = imageUrl,
                                contentDescription = "Image preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            "Live Image Preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                SectionLabelProd("Category & Pricing")
                // Category picker
                DropdownField(
                    label = "Category",
                    selectedValue = selectedCat?.name ?: "Select Category",
                    options = categories.map { it.name },
                    onSelect = { name -> selectedCat = categories.find { it.name == name } }
                )
                AppTextField(
                    price, { price = it }, "Price (Rs.)",
                    leadingIcon = Icons.Default.AttachMoney,
                    keyboardType = KeyboardType.Decimal
                )

                SectionLabelProd("Stock Settings")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        stock, { stock = it }, "Stock Qty",
                        keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        threshold, { threshold = it }, "Low Stock Alert",
                        keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                    )
                }

                // Approved toggle (only admin can directly approve)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Mark as Approved", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Approved products are visible to users",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                    Switch(
                        checked = isApproved,
                        onCheckedChange = { isApproved = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppColors.Success
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = if (productId <= 0) "Add Product" else "Save Changes",
                    onClick = {
                        // Validate
                        error = when {
                            name.isBlank() -> "Product name is required"
                            price.toDoubleOrNull() == null || price.toDouble() <= 0
                                -> "Enter a valid price"

                            stock.toIntOrNull() == null -> "Enter valid stock quantity"
                            threshold.toIntOrNull() == null -> "Enter valid threshold"
                            else -> ""
                        }
                        if (error.isNotBlank()) return@PrimaryButton

                        val entity = ProductEntity(
                            id = if (productId > 0) productId else 0,
                            name = name.trim(),
                            description = description.trim(),
                            imageUrl = imageUrl.trim(),
                            categoryId = selectedCat?.id,
                            subCategory = subCategory.trim(),
                            price = price.toDouble(),
                            stockQuantity = stock.toInt(),
                            lowStockThreshold = threshold.toInt(),
                            addedByUserId = session?.userId,
                            isApproved = isApproved
                        )
                        viewModel.saveProduct(entity) { ok, msg ->
                            if (ok) navController.navigateUp()
                            else error = msg
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionLabelProd(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold, color = AppColors.Primary
    )
    HorizontalDivider(color = AppColors.Divider)
}
