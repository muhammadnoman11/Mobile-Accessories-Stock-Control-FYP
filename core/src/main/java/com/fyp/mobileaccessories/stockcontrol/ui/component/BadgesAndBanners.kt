package com.fyp.mobileaccessories.stockcontrol.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    message: String = "⚡ Offline Mode: Viewing cached data. Internet required for actions."
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = AppColors.WarningContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = AppColors.Warning,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Warning,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RoleBadge(role: String) {
    val (bg, fg, label) = when (role) {
        "ADMIN" -> Triple(AppColors.AdminBadgeContainer, AppColors.AdminBadge, "Admin")
        "MODERATOR" -> Triple(AppColors.ModBadgeContainer, AppColors.ModBadge, "Moderator")
        else -> Triple(AppColors.UserBadgeContainer, AppColors.UserBadge, "User")
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "PENDING", "REFUND_PENDING" -> AppColors.WarningContainer to AppColors.Warning
        "CONFIRMED", "APPROVED", "REFUNDED" -> AppColors.InfoContainer to AppColors.Info
        "DISPATCHED" -> AppColors.PrimaryContainer to AppColors.Primary
        "DELIVERED" -> AppColors.SuccessContainer to AppColors.Success
        "CANCELLED", "REJECTED" -> AppColors.ErrorContainer to AppColors.Error
        else -> AppColors.SurfaceVariant to AppColors.TextSecondary
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: String = "",
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.TextTertiary
        )
        Text(message, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
        if (action.isNotEmpty()) {
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Delete",
    confirmColor: Color = AppColors.Error
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) { Text(confirmText) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AppColors.Primary)
            Text(
                "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
    }
}
