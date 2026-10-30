package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.component.ModOrderCard
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.component.ModStatusSheet
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner

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
