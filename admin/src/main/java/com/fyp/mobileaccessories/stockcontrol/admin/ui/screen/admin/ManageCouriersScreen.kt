package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ============================================================
// ManageCouriersScreen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCouriersScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val couriers by viewModel.couriers.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember {
        mutableStateOf<CourierEntity?>(
            null
        )
    }
    var deleteTarget by remember {
        mutableStateOf<CourierEntity?>(
            null
        )
    }
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Couriers",
                subtitle = "${couriers.size} couriers", onBack = { navController.navigateUp() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showSheet = true },
                containerColor = AppColors.Primary
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        if (couriers.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding)) {
                EmptyState(
                    "No couriers added", Icons.Default.LocalShipping,
                    "Add Courier"
                ) { editing = null; showSheet = true }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(couriers, key = { it.id }) { c ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(c.name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Column {
                                    Text("Contact: ${c.contactNo}")
                                    Text("Charges: Rs. ${c.deliveryCharges} • Est. ${c.estimatedDays} days")
                                }
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    null, tint = AppColors.Primary
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editing = c; showSheet = true }) {
                                        Icon(Icons.Default.Edit, null, tint = AppColors.Primary)
                                    }
                                    IconButton(onClick = { deleteTarget = c }) {
                                        Icon(Icons.Default.Delete, null, tint = AppColors.Error)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        CourierSheet(
            courier = editing,
            courierList = couriers,
            onDismiss = { showSheet = false },
            onSave = { c ->
                viewModel.saveCourier(c) { ok, msg -> snack = msg; if (ok) showSheet = false }
            }
        )
    }
    deleteTarget?.let { c ->
        ConfirmDialog(
            "Delete ${c.name}?", "This courier will be removed.",
            onConfirm = { viewModel.deleteCourier(c) { snack = "Deleted" }; deleteTarget = null },
            onDismiss = { deleteTarget = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourierSheet(
    courier: CourierEntity?,
    courierList: List<CourierEntity>,
    onDismiss: () -> Unit, onSave: (CourierEntity) -> Unit
) {
    var name by remember { mutableStateOf(courier?.name ?: "") }
    var contact by remember { mutableStateOf(courier?.contactNo ?: "") }
    var email by remember { mutableStateOf(courier?.email ?: "") }
    var address by remember { mutableStateOf(courier?.address ?: "") }
    var charges by remember { mutableStateOf(courier?.deliveryCharges?.toString() ?: "") }
    var days by remember { mutableStateOf(courier?.estimatedDays?.toString() ?: "3") }
    var error by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (courier == null) "Add Courier" else "Edit Courier",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            if (error.isNotBlank()) {
                Text(error, color = AppColors.Error, style = MaterialTheme.typography.bodySmall)
            }
            AppTextField(name, { name = it }, "Courier Name")
            AppTextField(
                contact,
                { contact = it },
                "Contact Number",
                keyboardType = KeyboardType.Phone
            )
            AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
            AppTextField(address, { address = it }, "Address")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    charges, { charges = it }, "Delivery Charges",
                    keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                )
                AppTextField(
                    days, { days = it }, "Est. Days",
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)
                )
            }
            PrimaryButton("Save Courier", onClick = {
                if (name.isBlank() || contact.isBlank()) {
                    error = "Name and contact number are required"
                    return@PrimaryButton
                }
                if (courierList.any { it.name.equals(name.trim(), ignoreCase = true) && it.id != courier?.id }) {
                    error = "A courier named '${name.trim()}' already exists (TC-03-03)."
                    return@PrimaryButton
                }
                onSave(
                    CourierEntity(
                        id = courier?.id ?: 0, name = name.trim(), contactNo = contact.trim(),
                        email = email.trim(), address = address.trim(),
                        deliveryCharges = charges.toDoubleOrNull() ?: 0.0,
                        estimatedDays = days.toIntOrNull() ?: 3
                    )
                )
            })
            Spacer(Modifier.height(16.dp))
        }
    }
}
