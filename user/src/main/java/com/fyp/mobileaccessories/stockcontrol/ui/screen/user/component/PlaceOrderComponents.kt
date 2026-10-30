package com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

@Composable
fun CourierOption(
    courier: CourierEntity,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.PrimaryContainer else Color.White
        ),
        border = if (isSelected)
            BorderStroke(1.5.dp, AppColors.Primary)
        else
            BorderStroke(1.dp, AppColors.Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    courier.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${courier.estimatedDays} day(s) • Rs. ${courier.deliveryCharges}",
                    style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                )
            }
            Text(
                "Rs. ${"%,.0f".format(courier.deliveryCharges)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AppColors.Primary else AppColors.TextSecondary
            )
        }
    }
}

@Composable
fun PaymentModeOption(
    mode: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.PrimaryContainer else Color.White
        ),
        border = if (isSelected)
            BorderStroke(1.5.dp, AppColors.Primary)
        else
            BorderStroke(1.dp, AppColors.Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected, onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = AppColors.Primary)
            )
            Icon(
                icon, null,
                tint = if (isSelected) AppColors.Primary else AppColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) AppColors.Primary else AppColors.OnBackground
            )
        }
    }
}

@Composable
fun PriceBreakdown(
    qty: Int,
    unitPrice: Double,
    deliveryCharge: Double,
    grandTotal: Double,
    courierName: String?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Order Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = AppColors.Divider)
            PriceRow("Items (×$qty)", "Rs. ${"%,.0f".format(unitPrice * qty)}")
            if (deliveryCharge > 0 && courierName != null) {
                PriceRow("Delivery ($courierName)", "Rs. ${"%,.0f".format(deliveryCharge)}")
            }
            HorizontalDivider(color = AppColors.Divider)
            PriceRow(
                label = "Total",
                value = "Rs. ${"%,.0f".format(grandTotal)}",
                isBold = true,
                valueColor = AppColors.Primary
            )
        }
    }
}

@Composable
fun PriceRow(
    label: String, value: String,
    isBold: Boolean = false,
    valueColor: Color = AppColors.OnBackground
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = AppColors.TextSecondary
        )
        Text(
            value, style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun FormSectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold, color = AppColors.Primary
    )
}
