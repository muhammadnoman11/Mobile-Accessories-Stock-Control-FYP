package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.OrderStatus
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

// ============================================================
// FILE: presentation/admin/AdminOrdersScreen.kt
// View all orders, filter by status, update order status.
// ============================================================

@Composable
fun AdminOrdersScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var statusFilter by remember { mutableStateOf("") }
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    var actionTarget by remember { mutableStateOf<OrderEntity?>(null) }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    val statusOptions = listOf(
        "", OrderStatus.PENDING, OrderStatus.CONFIRMED,
        OrderStatus.DISPATCHED, OrderStatus.DELIVERED, OrderStatus.CANCELLED
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Orders", subtitle = "${orders.size} orders",
                onBack = { navController.navigateUp() })
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            // Status filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusOptions) { s ->
                    FilterChip(
                        selected = statusFilter == s,
                        onClick = { statusFilter = s; viewModel.onOrderStatusFilter(s) },
                        label = { Text(if (s.isEmpty()) "All" else s.replace("_", " ")) }
                    )
                }
            }

            if (orders.isEmpty()) {
                EmptyState("No orders found", Icons.Default.ShoppingCart)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            viewModel = viewModel,
                            onUpdate = { actionTarget = order }
                        )
                    }
                }
            }
        }
    }

    // Status update bottom sheet
    actionTarget?.let { order ->
        UpdateStatusSheet(
            currentStatus = order.status,
            onDismiss = { actionTarget = null },
            onSelect = { newStatus ->
                viewModel.updateOrderStatus(order.id, newStatus) {
                    snack = "Order #${order.id} → $newStatus"
                }
                actionTarget = null
            }
        )
    }
}

@Composable
private fun OrderCard(order: OrderEntity, viewModel: AdminViewModel, onUpdate: () -> Unit) {
    val product by produceState<com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity?>(initialValue = null, order.productId) {
        value = viewModel.getProductById(order.productId)
    }
    val user by produceState<com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity?>(initialValue = null, order.userId) {
        value = viewModel.getUserById(order.userId)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                StatusBadge(order.status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage(
                        imageUrl = product?.imageUrl ?: "",
                        contentDescription = product?.name,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 24.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product?.name ?: "Product #${order.productId}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        "Qty: ${order.quantity} • Rs. ${"%,.0f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                    )
                }
            }
            Text(
                "Customer: ${user?.firstName ?: ""} ${user?.lastName ?: "User #${order.userId}"}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text(
                "Payment: ${PaymentMode.displayName(order.paymentMode)}",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
            )
            Text(
                "Contact No: ${user?.contactNo ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Date: ${DateUtils.formatDateTime(order.orderDate)}",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = onUpdate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Update Status")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateStatusSheet(
    currentStatus: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        OrderStatus.PENDING, OrderStatus.CONFIRMED,
        OrderStatus.DISPATCHED, OrderStatus.DELIVERED, OrderStatus.CANCELLED
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Update Order Status", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            options.forEach { s ->
                OutlinedButton(
                    onClick = { onSelect(s) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (s == currentStatus)
                        ButtonDefaults.outlinedButtonColors(containerColor = AppColors.PrimaryContainer)
                    else ButtonDefaults.outlinedButtonColors()
                ) { Text(s.replace("_", " ")) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
