package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// StockReportScreen — Shows all products with stock levels
// ============================================================

@Composable
fun StockReportScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val lowStock by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val movements by viewModel.recentStockMovements.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    var adjustTarget by remember {
        mutableStateOf<ProductEntity?>(
            null
        )
    }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Stock Report",
                subtitle = "${lowStock.size} low-stock alerts",
                onBack = { navController.navigateUp() })
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            // ── Low Stock Alerts ───────────────────────────
            if (lowStock.isNotEmpty()) {
                item {
                    Text(
                        "⚠️  Low Stock Alerts (${lowStock.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = AppColors.Error
                    )
                }
                items(lowStock, key = { it.id }) { p ->
                    StockProductRow(
                        product = p,
                        onAdjust = { adjustTarget = p })
                }
            }

            // ── All Products stock overview ─────────────────
            item {
                Text(
                    "All Products Stock",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
            }
            items(allProducts, key = { "all_${it.id}" }) { p ->
                StockProductRow(product = p, onAdjust = { adjustTarget = p })
            }

            // ── Recent Stock Movements ──────────────────────
            if (movements.isNotEmpty()) {
                item {
                    Text(
                        "Recent Movements",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                    )
                }
                items(movements.take(20), key = { "mv_${it.id}" }) { mv ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        ListItem(
                            headlineContent = { Text("Product #${mv.productId} — ${mv.movementType}") },
                            supportingContent = { Text("${mv.previousStock} → ${mv.newStock} • ${mv.reason}") },
                            trailingContent = {
                                Text(
                                    DateUtils.formatDate(mv.date),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Stock adjustment dialog
    adjustTarget?.let { product ->
        StockAdjustDialog(
            product = product,
            onDismiss = { adjustTarget = null },
            onAdjust = { newQty, reason ->
                viewModel.adjustStock(product, newQty, reason, session?.userId ?: 1) {
                    snack = "Stock updated for ${product.name}"
                }
                adjustTarget = null
            }
        )
    }
}

@Composable
private fun StockProductRow(
    product: ProductEntity,
    onAdjust: () -> Unit
) {
    val isLow = product.stockQuantity <= product.lowStockThreshold
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            if (isLow) AppColors.ErrorContainer else Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        ListItem(
            headlineContent = { Text(product.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Text(
                    "Stock: ${product.stockQuantity} / Threshold: ${product.lowStockThreshold}",
                    color = if (isLow) AppColors.Error else AppColors.TextSecondary
                )
            },
            trailingContent = {
                OutlinedButton(onClick = onAdjust) { Text("Adjust") }
            }
        )
    }
}

@Composable
private fun StockAdjustDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAdjust: (Int, String) -> Unit
) {
    var qty by remember { mutableStateOf(product.stockQuantity.toString()) }
    var reason by remember { mutableStateOf("Manual Adjustment") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock — ${product.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current: ${product.stockQuantity} units")
                OutlinedTextField(
                    value = qty, onValueChange = { qty = it },
                    label = { Text("New Quantity") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("Reason") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newQty = qty.toIntOrNull() ?: return@Button
                onAdjust(newQty, reason)
            }) { Text("Adjust") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(16.dp)
    )
}

