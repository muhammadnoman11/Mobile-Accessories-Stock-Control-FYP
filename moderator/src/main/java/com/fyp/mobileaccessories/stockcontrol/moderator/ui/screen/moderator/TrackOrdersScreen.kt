package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// TrackOrdersScreen — Moderator confirms and tracks orders
// ============================================================

@Composable
fun TrackOrdersScreen(
    navController: NavController,
    viewModel: ModViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    var actionTarget by remember { mutableStateOf<OrderEntity?>(null) }

    // Only show active orders (exclude delivered/cancelled)
    val activeOrders = remember(orders) {
        orders.filter { it.status !in listOf("DELIVERED", "CANCELLED") }
    }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Track Orders",
                subtitle = "${activeOrders.size} active orders",
                onBack = { navController.navigateUp() }
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OfflineBanner(isOffline = !isOnline)
            if (activeOrders.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    EmptyState(
                        "No active orders to track", Icons.Default.LocalShipping
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeOrders, key = { it.id }) { order ->
                        ModOrderCard(
                            order = order,
                            viewModel = viewModel,
                            onUpdateStatus = { actionTarget = order }
                        )
                    }
                }
            }
        }
    }

    // Status update sheet
    actionTarget?.let { order ->
        ModStatusSheet(
            currentStatus = order.status,
            onDismiss = { actionTarget = null },
            onSelect = { newStatus ->
                viewModel.updateOrderStatus(order.id, newStatus) { ok, msg ->
                    snack = if (ok) "Order #${order.id} → $newStatus" else msg
                }
                actionTarget = null
            }
        )
    }
}

@Composable
private fun ModOrderCard(
    order: OrderEntity,
    viewModel: ModViewModel,
    onUpdateStatus: () -> Unit
) {
    val product by produceState<com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity?>(initialValue = null, order.productId) {
        value = viewModel.getProductById(order.productId)
    }
    val user by produceState<com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity?>(initialValue = null, order.userId) {
        value = viewModel.getUserById(order.userId)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
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
                "Delivery: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
            )
            Text(
                "Contact No: ${user?.contactNo ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text(
                DateUtils.formatDateTime(order.orderDate),
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextTertiary
            )
            Button(
                onClick = onUpdateStatus, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) { Text("Update Status") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModStatusSheet(
    currentStatus: String, onDismiss: () -> Unit, onSelect: (String) -> Unit
) {
    // Moderator can move forward in the pipeline only
    val options = listOf("CONFIRMED", "DISPATCHED", "DELIVERED")
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Update Order Status", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Current: $currentStatus", style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            options.forEach { s ->
                Button(
                    onClick = { onSelect(s) }, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (s) {
                            "CONFIRMED" -> AppColors.Info
                            "DISPATCHED" -> AppColors.Primary
                            "DELIVERED" -> AppColors.Success
                            else -> AppColors.Primary
                        }
                    )
                ) { Text(s) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}