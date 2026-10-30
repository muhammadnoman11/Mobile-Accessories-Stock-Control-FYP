package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin.AdminViewModel
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.OrderStatus
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

@Composable
fun AdminOrderCard(order: OrderEntity, viewModel: AdminViewModel, onUpdate: () -> Unit) {
    val product by produceState<ProductEntity?>(initialValue = null, order.productId) {
        value = viewModel.getProductById(order.productId)
    }
    val user by produceState<UserEntity?>(initialValue = null, order.userId) {
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
                    ProductImage(
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
                        overflow = TextOverflow.Ellipsis
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
fun AdminUpdateStatusSheet(
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
