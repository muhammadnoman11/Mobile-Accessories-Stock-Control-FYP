package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.AdminOrderCard
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component.AdminUpdateStatusSheet
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.util.OrderStatus

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                        AdminOrderCard(
                            order = order,
                            viewModel = viewModel,
                            onUpdate = { actionTarget = order }
                        )
                    }
                }
            }
        }
    }

    actionTarget?.let { order ->
        AdminUpdateStatusSheet(
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
