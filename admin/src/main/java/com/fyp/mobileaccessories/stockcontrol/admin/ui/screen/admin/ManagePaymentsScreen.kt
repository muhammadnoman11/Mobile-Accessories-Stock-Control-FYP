package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.StatusBadge
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils
import com.fyp.mobileaccessories.stockcontrol.util.PaymentMode

// ============================================================
// FILE: ManagePaymentsScreen.kt — Transactions / mock payments & refunds
// ============================================================

@Composable
fun ManagePaymentsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    var remarksTarget by remember { mutableStateOf<Pair<Int, Boolean>?>(null) } // id, isApprove
    var refundTarget by remember { mutableStateOf<Int?>(null) } // txId for refund

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    val statusOptions = listOf("", "PENDING", "APPROVED", "REJECTED", "REFUND_PENDING", "REFUNDED")

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payments & Transactions",
                subtitle = "${transactions.size} records",
                onBack = { navController.navigateUp() })
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusOptions) { s ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.onTxStatusFilter(s) },
                        label = { Text(if (s.isEmpty()) "All" else s.replace("_", " ")) }
                    )
                }
            }
            if (transactions.isEmpty()) {
                EmptyState("No transactions found", Icons.Default.Payments)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        val order = orders.find { it.id == tx.orderId }
                        val isOrderCancelled = order?.status == "CANCELLED"

                        TransactionCard(
                            txId = tx.id,
                            orderId = tx.orderId,
                            amount = tx.amount,
                            mode = tx.paymentMode,
                            status = tx.status,
                            refNo = tx.referenceNo,
                            date = tx.transactionDate,
                            remarks = tx.remarks,
                            isOrderCancelled = isOrderCancelled,
                            onApprove = { remarksTarget = Pair(tx.id, true) },
                            onReject = { remarksTarget = Pair(tx.id, false) },
                            onRefund = { refundTarget = tx.id }
                        )
                    }
                }
            }
        }
    }

    // Remarks dialog for approve/reject
    remarksTarget?.let { (txId, isApprove) ->
        RemarksDialog(
            title = if (isApprove) "Approve Transaction" else "Reject Transaction",
            confirmLabel = if (isApprove) "Approve" else "Reject",
            confirmColor = if (isApprove) AppColors.Success else AppColors.Error,
            onDismiss = { remarksTarget = null },
            onConfirm = { remarks ->
                if (isApprove)
                    viewModel.approveTransaction(txId, remarks) { snack = "Transaction APPROVED" }
                else
                    viewModel.rejectTransaction(txId, remarks) { snack = "Transaction REJECTED" }
                remarksTarget = null
            }
        )
    }

    // Refund Dialog
    refundTarget?.let { txId ->
        RemarksDialog(
            title = "Process Refund",
            confirmLabel = "Refund",
            confirmColor = AppColors.Warning,
            onDismiss = { refundTarget = null },
            onConfirm = { remarks ->
                viewModel.refundTransaction(txId, remarks) { snack = "Refund processed successfully" }
                refundTarget = null
            }
        )
    }
}

@Composable
private fun TransactionCard(
    txId: Int, orderId: Int, amount: Double, mode: String,
    status: String, refNo: String, date: Long, remarks: String,
    isOrderCancelled: Boolean,
    onApprove: () -> Unit, onReject: () -> Unit, onRefund: () -> Unit
) {
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
                Text("TXN #$txId", fontWeight = FontWeight.Bold)
                StatusBadge(status)
            }
            Text(
                "Order #$orderId • Rs. ${"%,.0f".format(amount)}",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
            )
            Text(
                "Mode: ${PaymentMode.displayName(mode)} • Ref: $refNo",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
            )
            Text(
                DateUtils.formatDateTime(date), style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            if (remarks.isNotBlank())
                Text(
                    "Remarks: $remarks", style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Primary
                )

            val showRefundButton = status == "REFUND_PENDING" || (status == "APPROVED" && isOrderCancelled && mode != "CASH_ON_DELIVERY")

            if (status == "PENDING" || showRefundButton) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (status == "PENDING") {
                        Button(
                            onClick = onApprove, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("Approve")
                        }
                        OutlinedButton(
                            onClick = onReject, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                        ) {
                            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("Reject")
                        }
                    } else {
                        Button(
                            onClick = onRefund, modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Warning)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("Process Refund")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemarksDialog(
    title: String = "Approve Transaction",
    confirmLabel: String = "Approve",
    confirmColor: Color = AppColors.Success,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var remarks by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add optional remarks for this decision:")
                OutlinedTextField(
                    value = remarks, onValueChange = { remarks = it },
                    placeholder = { Text("Remarks (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(remarks) },
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) { Text(confirmLabel) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(16.dp)
    )
}
