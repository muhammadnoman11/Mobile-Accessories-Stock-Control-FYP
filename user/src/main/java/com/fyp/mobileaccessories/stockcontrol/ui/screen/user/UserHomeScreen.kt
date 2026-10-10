package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.navigation.Screen
import com.fyp.mobileaccessories.stockcontrol.navigation.navigateAndClear
import com.fyp.mobileaccessories.stockcontrol.ui.component.OfflineBanner
import com.fyp.mobileaccessories.stockcontrol.ui.component.RequestNotificationPermissionEffect
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// FILE: presentation/user/UserHomeScreen.kt
// Customer home screen — scrollable page showing:
// · Welcome header with user name
// · Active announcements carousel
// · Featured / latest products grid
// · Bottom navigation to key sections
// ============================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserHomeScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    RequestNotificationPermissionEffect()

    Scaffold(
        topBar = {},
        bottomBar = {
            UserBottomNav(navController = navController, currentRoute = Screen.UserHome.route)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
               OfflineBanner(isOffline = !isOnline)
            }
            // ── Unified Gradient Header ───────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(AppColors.Primary, AppColors.CardGradientEnd)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "StockControl",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Hello, ${session?.name?.split(" ")?.firstOrNull() ?: "there"} 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color.White
                            )
                            Text(
                                "Browse the latest mobile accessories",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        // Search shortcut button
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.ProductList.route) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Search products...")
                        }
                    }
                }
            }

            // ── Announcements Carousel ────────────────────────
            if (announcements.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        SectionHeader(
                            title = "Announcements",
                            action = "See All",
                            onAction = { navController.navigate(Screen.Announcements.route) }
                        )
                        AnnouncementsCarousel(announcements = announcements.take(5))
                    }
                }
            }

            // ── Featured Products (latest 6) ──────────────────
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    SectionHeader(
                        title = "Latest Products",
                        action = "View All",
                        onAction = { navController.navigate(Screen.ProductList.route) }
                    )
                }
            }

            // 2-column product grid
            val chunked = products.take(6).chunked(2)
            items(chunked) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { product ->
                        ProductGridCard(
                            product = product,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate(Screen.ProductDetail.createRoute(product.id))
                            }
                        )
                    }
                    // Fill empty slot in last row
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            // Bottom padding
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Announcements Horizontal Pager ───────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnnouncementsCarousel(announcements: List<AnnouncementEntity>) {
    val pagerState = rememberPagerState { announcements.size }
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp
        ) { page ->
            val ann = announcements[page]
            val (bg, accent) = when (ann.type) {
                "SALE" -> AppColors.SuccessContainer to AppColors.Success
                "ALERT" -> AppColors.ErrorContainer to AppColors.Error
                "UPDATE" -> AppColors.InfoContainer to AppColors.Info
                else -> AppColors.PrimaryContainer to AppColors.Primary
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Campaign,
                            null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            ann.type, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = accent
                        )
                    }
                    Text(
                        ann.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = accent
                    )
                    Text(
                        ann.content, style = MaterialTheme.typography.bodySmall,
                        color = accent.copy(alpha = 0.8f), maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        DateUtils.formatDate(ann.dateCreated),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent.copy(alpha = 0.6f)
                    )
                }
            }
        }
        // Pager dots indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(announcements.size) { i ->
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .background(
                            if (isSelected) AppColors.Primary
                            else AppColors.Divider,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// ── Compact product card for the 2-column home grid ──────────
@Composable
fun ProductGridCard(
    product: ProductEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOutOfStock = product.stockQuantity == 0
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 36.dp
                )
                if (isOutOfStock) {
                    Surface(
                        color = AppColors.ErrorContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            "Out", style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Error,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Rs. ${"%,.0f".format(product.price)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            Text(
                text = "Stock: ${product.stockQuantity}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOutOfStock) AppColors.Error else AppColors.TextSecondary
            )
        }
    }
}

// ── Section header helper ─────────────────────────────────────
@Composable
private fun SectionHeader(title: String, action: String = "", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (action.isNotEmpty()) {
            TextButton(onClick = onAction) {
                Text(
                    action,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Primary
                )
            }
        }
    }
}

// ── User Bottom Navigation Bar ────────────────────────────────
@Composable
fun UserBottomNav(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("Home", Icons.Filled.Home, Screen.UserHome.route),
            Triple("Products", Icons.Filled.Inventory2, Screen.ProductList.route),
            Triple("My Orders", Icons.Filled.ShoppingBag, Screen.MyOrders.route),
            Triple("Profile", Icons.Filled.AccountCircle, Screen.UserProfile.route),
        )
        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) navController.navigate(route) {
                        popUpTo(Screen.UserHome.route); launchSingleTop = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Primary,
                    selectedTextColor = AppColors.Primary,
                    indicatorColor = AppColors.PrimaryContainer
                )
            )
        }
    }
}
