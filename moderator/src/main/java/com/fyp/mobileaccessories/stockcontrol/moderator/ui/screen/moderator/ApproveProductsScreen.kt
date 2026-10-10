package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// ApproveProductsScreen
// ============================================================

@Composable
fun ApproveProductsScreen(
    navController: NavController,
    viewModel: ModViewModel = hiltViewModel()
) {
    val products    by viewModel.pendingProducts.collectAsStateWithLifecycle()
    val isOnline    by viewModel.isOnline.collectAsStateWithLifecycle()
    var snack       by remember { mutableStateOf("") }
    val snackState  = remember { SnackbarHostState() }
    var rejectTarget by remember { mutableStateOf<ProductEntity?>(null) }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) { snackState.showSnackbar(snack); snack = "" }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title    = "Product Approvals",
                subtitle = "${products.size} pending",
                onBack   = { navController.navigateUp() }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OfflineBanner(isOffline = !isOnline)
            if (products.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    EmptyState(
                        message = "No products awaiting approval",
                        icon    = Icons.Default.CheckCircle
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        PendingProductCard(
                            product   = product,
                            onApprove = {
                                viewModel.approveProduct(product.id) { ok, msg ->
                                    snack = if (ok) "✅ ${product.name} approved" else msg
                                }
                            },
                            onReject  = { rejectTarget = product }
                        )
                    }
                }
            }
        }
    }

    // Rejection reason dialog (TC-08-03)
    rejectTarget?.let { target ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("Reject '${target.name}'?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide a reason for rejection (TC-08-03):", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("e.g. Image quality too low, price missing") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectProduct(target.id) { ok, msg ->
                            snack = if (ok) "❌ ${target.name} rejected" else msg
                        }
                        rejectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                ) { Text("Confirm Rejection") }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectTarget = null }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PendingProductCard(
    product: ProductEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    ProductImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 28.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(product.description, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = AppColors.InfoContainer, shape = RoundedCornerShape(20.dp)) {
                    Text("Rs. ${"%,.0f".format(product.price)}", color = AppColors.Info,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(color = AppColors.PrimaryContainer, shape = RoundedCornerShape(20.dp)) {
                    Text("Stock: ${product.stockQuantity}", color = AppColors.Primary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                if (product.subCategory.isNotBlank()) {
                    Surface(color = AppColors.SurfaceVariant, shape = RoundedCornerShape(20.dp)) {
                        Text(product.subCategory, color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp)); Text("Approve")
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)) {
                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp)); Text("Reject")
                }
            }
        }
    }
}
