package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.LoadingScreen
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

// ============================================================
// FILE: presentation/user/PlaceOrderScreen.kt
// Full order form:
// · Shows product summary (name, price per unit)
// · Quantity stepper (1 … stock limit)
// · Courier dropdown (loaded from DB) with delivery charge shown
// · Payment mode selector (COD / Bank Transfer / Card)
// · Delivery address (pre-filled from user profile)
// · Order notes (optional)
// · Total price calculation in real-time
// · On success → navigate to MyOrders with confirmation snackbar
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOrderScreen(
    navController: NavController,
    productId: Int,
    viewModel: UserViewModel = hiltViewModel()
) {
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val couriers by viewModel.activeCouriers.collectAsStateWithLifecycle()
    val state by viewModel.orderState.collectAsStateWithLifecycle()
    val snackState = remember { SnackbarHostState() }

    // Load product and prefill address on first composition
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
        viewModel.prefillAddress()
    }

    // Navigate away on success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackState.showSnackbar("✅ Order #${state.orderId} placed successfully!")
            viewModel.resetOrderState()
            navController.navigate(Screen.MyOrders.route) {
                popUpTo(Screen.UserHome.route)
            }
        }
    }

    // Show error in snackbar at bottom
    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackState.showSnackbar("❌ $err")
        }
    }

    // Selected courier object (for calculating delivery charges)
    val selectedCourier = couriers.find { it.id == state.selectedCourierId }

    // Real-time total calculation
    val productPrice = product?.price ?: 0.0
    val deliveryCharge = selectedCourier?.deliveryCharges ?: 0.0
    val lineTotal = productPrice * state.quantity
    val grandTotal = lineTotal + deliveryCharge

    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "Place Order", onBack = { navController.navigateUp() })
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        if (product == null) {
            LoadingScreen(); return@Scaffold
        }

        val p = product!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner(isOffline = !isOnline)
            Spacer(Modifier.height(8.dp))

            // ── Product Summary Card ──────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.PrimaryContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage(
                            imageUrl = p.imageUrl,
                            contentDescription = p.name,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 28.dp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            p.name, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = AppColors.Primary
                        )
                        Text(
                            "Rs. ${"%,.0f".format(p.price)} per unit",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Primary.copy(0.7f)
                        )
                        Text(
                            "${p.stockQuantity} units available",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Primary.copy(0.7f)
                        )
                    }
                }
            }



            // ── Section: Quantity ─────────────────────────────
            FormSectionLabel("Quantity")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Decrease button
                FilledIconButton(
                    onClick = { viewModel.onQuantityChange(state.quantity - 1) },
                    enabled = state.quantity > 1,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AppColors.PrimaryContainer
                    )
                ) { Icon(Icons.Default.Remove, null, tint = AppColors.Primary) }

                // Quantity display
                Text(
                    text = state.quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )

                // Increase button
                FilledIconButton(
                    onClick = { viewModel.onQuantityChange(state.quantity + 1) },
                    enabled = state.quantity < p.stockQuantity,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AppColors.PrimaryContainer
                    )
                ) { Icon(Icons.Default.Add, null, tint = AppColors.Primary) }

                Spacer(Modifier.weight(1f))
                Text(
                    "Max: ${p.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                )
            }

            // ── Section: Select Courier ───────────────────────
            FormSectionLabel("Delivery Courier")
            if (couriers.isEmpty()) {
                Surface(color = AppColors.WarningContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "No couriers available. Proceed without courier selection.",
                        style = MaterialTheme.typography.bodySmall, color = AppColors.Warning,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    couriers.forEach { courier ->
                        CourierOption(
                            courier = courier,
                            isSelected = state.selectedCourierId == courier.id,
                            onSelect = { viewModel.onCourierSelect(courier.id) }
                        )
                    }
                }
            }

            // ── Section: Payment Mode ─────────────────────────
            FormSectionLabel("Payment Method")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PaymentMode.CASH_ON_DELIVERY to Icons.Default.Money,
                    PaymentMode.BANK_TRANSFER to Icons.Default.AccountBalance,
                    PaymentMode.CARD to Icons.Default.CreditCard
                ).forEach { (mode, icon) ->
                    PaymentModeOption(
                        mode = mode,
                        label = PaymentMode.displayName(mode),
                        icon = icon,
                        isSelected = state.paymentMode == mode,
                        onSelect = { viewModel.onPaymentModeChange(mode) }
                    )
                }
            }

            // ── Section: Delivery Address ─────────────────────
            FormSectionLabel("Delivery Address")
            AppTextField(
                value = state.deliveryAddress,
                onValueChange = viewModel::onAddressChange,
                label = "Full Delivery Address",
                leadingIcon = Icons.Default.LocationOn,
                singleLine = false,
                maxLines = 3,
                isError = state.error != null && state.deliveryAddress.isBlank()
            )

            // ── Section: Order Notes (optional) ──────────────
            FormSectionLabel("Notes (Optional)")
            AppTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Special instructions or notes",
                singleLine = false,
                maxLines = 2
            )

            // ── Price Breakdown ───────────────────────────────
            HorizontalDivider(color = AppColors.Divider)
            PriceBreakdown(
                qty = state.quantity,
                unitPrice = productPrice,
                deliveryCharge = deliveryCharge,
                grandTotal = grandTotal,
                courierName = selectedCourier?.name
            )

            Spacer(Modifier.height(8.dp))

            // ── Place Order button ────────────────────────────
            PrimaryButton(
                text = "Confirm Order • Rs. ${"%,.0f".format(grandTotal)}",
                onClick = { viewModel.placeOrder(p, selectedCourier) { /* handled via state */ } },
                isLoading = state.isLoading,
                icon = Icons.Default.CheckCircle
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Courier selection row ─────────────────────────────────────
@Composable
private fun CourierOption(
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
            androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider)
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

// ── Payment mode selection row ────────────────────────────────
@Composable
private fun PaymentModeOption(
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

// ── Price breakdown summary ───────────────────────────────────
@Composable
private fun PriceBreakdown(
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
private fun PriceRow(
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

// ── Form section label helper ─────────────────────────────────
@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold, color = AppColors.Primary
    )
}
