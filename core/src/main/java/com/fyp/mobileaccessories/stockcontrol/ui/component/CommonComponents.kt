package com.fyp.mobileaccessories.stockcontrol.ui.component

// ============================================================
// FILE: presentation/common/CommonComponents.kt
// Reusable Jetpack Compose components used across all screens
// ============================================================

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

// ─────────────────────────────────────────────
// APP TOP BAR
// Modern top bar with gradient background
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String = "",
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.Primary,
            scrolledContainerColor = AppColors.PrimaryVariant
        )
    )
}

// ─────────────────────────────────────────────
// STAT CARD
// Dashboard metric card with icon and color
// ─────────────────────────────────────────────
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color = AppColors.PrimaryContainer,
    contentColor: Color = AppColors.Primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(contentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// OFFLINE BANNER
// Non-intrusive warning banner shown when device is offline
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    action: String = "",
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (action.isNotEmpty()) {
            TextButton(onClick = onAction) {
                Text(action, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ─────────────────────────────────────────────
// SEARCH BAR
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextSecondary)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = AppColors.TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Primary,
            unfocusedBorderColor = AppColors.Divider,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = AppColors.SurfaceVariant
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

// ─────────────────────────────────────────────
// APP TEXT FIELD
// Standard input field with consistent styling
// ─────────────────────────────────────────────
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    readOnly: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) ({
                Text(
                    placeholder,
                    color = AppColors.TextTertiary
                )
            }) else null,
            leadingIcon = if (leadingIcon != null) ({
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (isError) AppColors.Error else AppColors.TextSecondary
                )
            }) else null,
            trailingIcon = when {
                isPassword -> ({
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password",
                            tint = AppColors.TextSecondary
                        )
                    }
                })

                trailingIcon != null -> trailingIcon
                else -> null
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Divider,
                errorBorderColor = AppColors.Error
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// PRIMARY BUTTON
// ─────────────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────
// ROLE BADGE
// Colored badge for Admin / Moderator / User
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// STATUS BADGE — for orders and transactions
// ─────────────────────────────────────────────
@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "PENDING" -> AppColors.WarningContainer to AppColors.Warning
        "CONFIRMED", "APPROVED" -> AppColors.InfoContainer to AppColors.Info
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

// ─────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// CONFIRM DIALOG
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// LOADING INDICATOR
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// PRODUCT CARD
// Reusable card shown in product lists
// ─────────────────────────────────────────────
@Composable
fun ProductCard(
    name: String,
    description: String,
    price: Double,
    stockQuantity: Int,
    lowStockThreshold: Int,
    imageUrl: String = "",
    isApproved: Boolean = true,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isLowStock = stockQuantity <= lowStockThreshold
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    ProductImage(
                        imageUrl = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        iconSize = 28.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rs. ${"%,.0f".format(price)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                    if (!isApproved) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppColors.WarningContainer
                        ) {
                            Text(
                                "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.Warning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isLowStock) AppColors.ErrorContainer else AppColors.SuccessContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isLowStock) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isLowStock) AppColors.Error else AppColors.Success
                            )
                            Text(
                                "Stock: $stockQuantity",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLowStock) AppColors.Error else AppColors.Success,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AppColors.Error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// USER CARD — for user/moderator lists
// ─────────────────────────────────────────────
@Composable
fun UserCard(
    firstName: String,
    lastName: String,
    email: String,
    contactNo: String,
    role: String,
    isActive: Boolean,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$firstName $lastName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    RoleBadge(role)
                }
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                Text(
                    contactNo,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                if (!isActive) {
                    Text(
                        "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Error
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (onEdit != null) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AppColors.Error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// GRADIENT HEADER CARD — hero card with gradient
// ─────────────────────────────────────────────
@Composable
fun GradientHeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AppColors.CardGradientStart, AppColors.CardGradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// DROPDOWN FIELD — for role/status/category pickers
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Divider
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}


// ── Product Image Helper ──────────────────────────────────────────
@Composable
fun ProductImage(
    imageUrl: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    iconSize: Dp = 36.dp
) {
    if (imageUrl.isNotBlank()) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.Primary,
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        )
    } else {
        Box(
            modifier = modifier.background(AppColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// ── Product Grid Card ──────────────────────────────────────────
@Composable
fun ProductGridCard(
    product: ProductEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOutOfStock = product.stockQuantity == 0

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 36.dp
                )
                // Out of stock overlay
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Out of Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                product.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Rs. ${"%,.0f".format(product.price)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            if (product.stockQuantity <= product.lowStockThreshold && !isOutOfStock) {
                Text(
                    "Only ${product.stockQuantity} left!",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Warning
                )
            }
        }
    }
}