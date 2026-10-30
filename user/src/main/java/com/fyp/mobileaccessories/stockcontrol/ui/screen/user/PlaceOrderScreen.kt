package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.LoadingScreen
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.CourierOption
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.FormSectionLabel
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.PaymentModeOption
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.PriceBreakdown
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

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

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
        viewModel.prefillAddress()
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackState.showSnackbar("✅ Order #${state.orderId} placed successfully!")
            viewModel.resetOrderState()
            navController.navigate(Screen.MyOrders.route) {
                popUpTo(Screen.UserHome.route)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackState.showSnackbar("❌ $err")
        }
    }

    val selectedCourier = couriers.find { it.id == state.selectedCourierId }

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
            OfflineBanner(isOffline = !isOnline)
            Spacer(Modifier.height(8.dp))

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
                        ProductImage(
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

            FormSectionLabel("Quantity")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = { viewModel.onQuantityChange(state.quantity - 1) },
                    enabled = state.quantity > 1,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AppColors.PrimaryContainer
                    )
                ) { Icon(Icons.Default.Remove, null, tint = AppColors.Primary) }

                Text(
                    text = state.quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )

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

            FormSectionLabel("Notes (Optional)")
            AppTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Special instructions or notes",
                singleLine = false,
                maxLines = 2
            )

            HorizontalDivider(color = AppColors.Divider)
            PriceBreakdown(
                qty = state.quantity,
                unitPrice = productPrice,
                deliveryCharge = deliveryCharge,
                grandTotal = grandTotal,
                courierName = selectedCourier?.name
            )

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text = "Confirm Order • Rs. ${"%,.0f".format(grandTotal)}",
                onClick = { viewModel.placeOrder(p, selectedCourier) { } },
                isLoading = state.isLoading,
                icon = Icons.Default.CheckCircle
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
