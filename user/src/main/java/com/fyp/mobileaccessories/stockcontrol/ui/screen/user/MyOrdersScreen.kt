package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.OrderStatus
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

// ============================================================
// FILE: presentation/user/MyOrdersScreen.kt
// Shows all orders the logged-in customer has placed.
// Displays order status, payment status, totals, and timeline.
// ============================================================

@Composable
fun MyOrdersScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val orders by viewModel.myOrders.collectAsStateWithLifecycle()
    val transactions by viewModel.myTransactions.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // Local filter state
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
            com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
            // ── Status filter chips ───────────────────────────
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
                        // Match transaction for payment status
                        val tx = transactions.find { it.orderId == order.id }
                        MyOrderCard(order = order, transaction = tx, viewModel = viewModel)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MyOrderCard(
    order: OrderEntity,
    transaction: TransactionEntity?,
    viewModel: UserViewModel
) {
    val product by produceState<com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity?>(initialValue = null, order.productId) {
        value = viewModel.getProductById(order.productId)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Header row ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${order.id}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                StatusBadge(order.status)
            }

            HorizontalDivider(color = AppColors.Divider)

            // ── Product Info Row ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage(
                        imageUrl = product?.imageUrl ?: "",
                        contentDescription = product?.name,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 28.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product?.name ?: "Product #${order.productId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Qty: ${order.quantity} · Total: Rs. ${"%,.0f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }

            HorizontalDivider(color = AppColors.Divider)

            // ── Order details ─────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                OrderDetailItem("Quantity", "${order.quantity} unit(s)")
                OrderDetailItem("Total", "Rs. ${"%,.0f".format(order.totalAmount)}")
                OrderDetailItem("Payment", PaymentMode.displayName(order.paymentMode))
            }

            // ── Delivery address ──────────────────────────────
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn, null,
                    tint = AppColors.TextTertiary, modifier = Modifier
                        .size(14.dp)
                        .offset(y = 2.dp)
                )
                Text(
                    order.deliveryAddress,
                    style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                )
            }

            // ── Notes ─────────────────────────────────────────
            if (order.notes.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Notes,
                        null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier
                            .size(14.dp)
                            .offset(y = 2.dp)
                    )
                    Text(
                        order.notes, style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }

            HorizontalDivider(color = AppColors.Divider)

            // ── Payment / Transaction status ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Payment Status", style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary
                )
                if (transaction != null) {
                    StatusBadge(transaction.status)
                } else {
                    Text(
                        "—", style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextTertiary
                    )
                }
            }

            // ── Transaction reference ─────────────────────────
            transaction?.let { tx ->
                Text(
                    "Ref: ${tx.referenceNo}",
                    style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary
                )
                if (tx.remarks.isNotBlank()) {
                    Text(
                        "Note: ${tx.remarks}",
                        style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary
                    )
                }
            }

            // ── Order date ────────────────────────────────────
            Text(
                DateUtils.formatDateTime(order.orderDate),
                style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary
            )

            // ── Order progress tracker ────────────────────────
            OrderProgressBar(status = order.status)
        }
    }
}

// ── Compact detail item ───────────────────────────────────────
@Composable
private fun OrderDetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ── Visual order progress stepper ────────────────────────────
@Composable
private fun OrderProgressBar(status: String) {
    val steps = listOf("PENDING", "CONFIRMED", "DISPATCHED", "DELIVERED")
    val currentStep = steps.indexOf(status).coerceAtLeast(0)
    val isCancelled = status == "CANCELLED"

    if (isCancelled) {
        Surface(color = AppColors.ErrorContainer, shape = RoundedCornerShape(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Cancel,
                    null,
                    tint = AppColors.Error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Order Cancelled",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Error
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = index <= currentStep
            val isActive = index == currentStep
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .run {
                            if (isDone) background(AppColors.Primary, CircleShape)
                            else background(AppColors.Divider, CircleShape)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) Icon(
                        Icons.Default.Check, null, tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    else Text(
                        "${index + 1}", style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextTertiary
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    step.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) AppColors.Primary else AppColors.TextTertiary
                )
            }
            // Connector line between steps
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) AppColors.Primary else AppColors.Divider
                        )
                )
            }
        }
    }
}

