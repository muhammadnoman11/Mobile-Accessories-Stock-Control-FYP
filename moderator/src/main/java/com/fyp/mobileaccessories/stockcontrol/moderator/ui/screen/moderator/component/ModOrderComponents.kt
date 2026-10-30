package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator.ModViewModel
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

@Composable
fun ModOrderCard(
    order: OrderEntity,
    viewModel: ModViewModel,
    onUpdateStatus: () -> Unit
) {
    val product by produceState<ProductEntity?>(initialValue = null, order.productId) {
        value = viewModel.getProductById(order.productId)
    }
    val user by produceState<UserEntity?>(initialValue = null, order.userId) {
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
fun ModStatusSheet(
    currentStatus: String, onDismiss: () -> Unit, onSelect: (String) -> Unit
) {
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
