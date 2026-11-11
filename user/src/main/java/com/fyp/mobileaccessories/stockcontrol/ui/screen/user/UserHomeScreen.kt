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
import com.fyp.mobileaccessories.stockcontrol.ui.component.ProductImage
import com.fyp.mobileaccessories.stockcontrol.ui.component.RequestNotificationPermissionEffect
import com.fyp.mobileaccessories.stockcontrol.ui.screen.user.component.AnnouncementsCarousel
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors

@Composable
fun UserHomeScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    val userName = session?.name ?: profile?.let { "${it.firstName} ${it.lastName}" } ?: "Valued Customer"
    val regDate = profile?.dateOfRegistration ?: System.currentTimeMillis()

    val welcomeAnnouncement = remember(userName, regDate) {
        AnnouncementEntity(
            id = -1,
            title = "Welcome, $userName! 🎉",
            content = "Thank you for joining Mobile Accessories Store. Enjoy exploring our collections!",
            type = "WELCOME",
            isActive = true,
            dateCreated = regDate
        )
    }

    val allAnnouncements = remember(announcements, welcomeAnnouncement) {
        (listOf(welcomeAnnouncement) + announcements).sortedByDescending { it.dateCreated }
    }

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
            /** Unified Gradient Header */
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(AppColors.Primary, AppColors.CardGradientEnd)
                            )
                        )
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
                    }
                }
            }

            /** Announcements Carousel */
            if (allAnnouncements.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        SectionHeader(
                            title = "Announcements",
                            action = "See All",
                            onAction = { navController.navigate(Screen.Announcements.route) }
                        )
                        AnnouncementsCarousel(announcements = allAnnouncements.take(5))
                    }
                }
            }

            /** Featured Products */
            item {
                Column(modifier = Modifier.padding(top = 5.dp)) {
                    SectionHeader(
                        title = "Latest Products",
                        action = "View All",
                        onAction = { navController.navigate(Screen.ProductList.route) }
                    )
                }
            }

            if (products.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = AppColors.InfoContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = AppColors.Info,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "No products available right now",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Info
                                )
                                Text(
                                    "Check back soon for new accessories and updates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.Info.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            } else {
                /** 2-column product grid */
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
                        /** Fill empty slot in last row */
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            /** Bottom padding */
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/** Compact product card for the 2-column home grid */
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
            /** Product image */
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
                maxLines = 1,
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

/** Section header helper */
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

/** User Bottom Navigation Bar */
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
