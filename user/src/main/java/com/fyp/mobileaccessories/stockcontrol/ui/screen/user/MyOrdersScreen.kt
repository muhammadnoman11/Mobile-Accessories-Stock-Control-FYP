package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.MyOrderCard
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.OrderStatus

@Composable
fun MyOrdersScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val orders by viewModel.myOrders.collectAsStateWithLifecycle()
    val transactions by viewModel.myTransactions.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf("") }
    val filteredOrders = remember(orders, statusFilter) {
        if (statusFilter.isBlank()) orders
        else orders.filter { it.status == statusFilter }
    }

    val statusOptions = listOf(
        "", OrderStatus.PENDING, OrderStatus.CONFIRMED,
        OrderStatus.DISPATCHED, OrderStatus.DELIVERED, OrderStatus.CANCELLED
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Orders",
                subtitle = "${filteredOrders.size} order(s)",
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = { UserBottomNav(navController, Screen.MyOrders.route) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OfflineBanner(isOffline = !isOnline)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusOptions) { s ->
                    FilterChip(
                        selected = statusFilter == s,
                        onClick = { statusFilter = s },
                        label = { Text(if (s.isEmpty()) "All" else s.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                EmptyState(
                    message = if (statusFilter.isBlank()) "You have no orders yet"
                    else "No $statusFilter orders",
                    icon = Icons.Default.ShoppingBag,
                    action = if (statusFilter.isBlank()) "Browse Products" else "Clear Filter",
                    onAction = {
                        if (statusFilter.isBlank()) navController.navigate(Screen.ProductList.route)
                        else statusFilter = ""
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        val tx = transactions.find { it.orderId == order.id }
                        MyOrderCard(order = order, transaction = tx, viewModel = viewModel)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
