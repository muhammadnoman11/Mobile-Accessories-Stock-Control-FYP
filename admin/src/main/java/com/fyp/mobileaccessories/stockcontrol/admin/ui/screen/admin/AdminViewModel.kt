package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.data.repository.AnnouncementRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.CategoryRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.CourierRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.OrderRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.ProductRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.StockMovementRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.TransactionRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.UserRepository
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import com.fyp.mobileaccessories.stockcontrol.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// FILE: presentation/admin/AdminViewModel.kt
// Central ViewModel for all Admin screens.
// Provides: dashboard stats, user CRUD, product CRUD,
// order management, payment management, courier CRUD,
// announcements CRUD, category CRUD, stock reports.
// ============================================================

// ── Dashboard stats ───────────────────────────────────────────
data class DashboardStats(
    val totalUsers: Int = 0,
    val totalModerators: Int = 0,
    val totalProducts: Int = 0,
    val pendingProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val pendingPayments: Int = 0,
    val totalRevenue: Double = 0.0
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val productRepo: ProductRepository,
    private val categoryRepo: CategoryRepository,
    private val courierRepo: CourierRepository,
    private val orderRepo: OrderRepository,
    private val transactionRepo: TransactionRepository,
    private val announcementRepo: AnnouncementRepository,
    private val stockMoveRepo: StockMovementRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ── Real-time Connectivity ──────────────────────────────
    val isOnline: StateFlow<Boolean> = ConnectionDetector.observeConnectivity(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectionDetector.isConnected(context))

    // ── Session info ──────────────────────────────────────────
    val session = sessionManager.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ──────────────────────────────────────────────────────────
    // DASHBOARD STATS
    // ──────────────────────────────────────────────────────────
    val stats: StateFlow<DashboardStats> = combine(
        userRepo.getAllUsers(),
        productRepo.getAllProducts(),
        orderRepo.getAllOrders(),
        transactionRepo.getAllTransactions()
    ) { users, products, orders, transactions ->
        DashboardStats(
            totalUsers = users.count { it.role == "USER" && it.isActive },
            totalModerators = users.count { it.role == "MODERATOR" && it.isActive },
            totalProducts = products.count { it.isApproved },
            pendingProducts = products.count { !it.isApproved },
            lowStockCount = products.count { it.stockQuantity <= it.lowStockThreshold && it.isApproved },
            totalOrders = orders.size,
            pendingOrders = orders.count { it.status == "PENDING" },
            pendingPayments = transactions.count { it.status == "PENDING" },
            totalRevenue = orders.filter { it.status == "DELIVERED" }.sumOf { it.totalAmount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    fun loadDashboardStats() {
        // Reactive stream automatically handles loading and updates
    }

    // ──────────────────────────────────────────────────────────
    // USERS
    // ──────────────────────────────────────────────────────────
    private val _userSearchQuery = MutableStateFlow("")
    private val _userRoleFilter = MutableStateFlow("")  // "" = all roles
    private val _userDomainFilter = MutableStateFlow("") // moderator domain filter
    private val _userFromDateFilter = MutableStateFlow(0L)

    val userSearchQuery = _userSearchQuery.asStateFlow()
    val userRoleFilter = _userRoleFilter.asStateFlow()
    val userDomainFilter = _userDomainFilter.asStateFlow()

    // Live search — updates automatically when query, role, domain, or date filter changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val users: StateFlow<List<UserEntity>> = combine(
        _userSearchQuery, _userRoleFilter, _userDomainFilter, _userFromDateFilter
    ) { q, r, d, dt ->
        userRepo.searchUsers(query = q, role = r, domain = d, fromDate = dt)
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onUserSearchChange(query: String) = _userSearchQuery.update { query }
    fun onUserRoleFilter(role: String) = _userRoleFilter.update { role }
    fun onUserDomainFilter(domain: String) = _userDomainFilter.update { domain }
    fun onUserFromDateFilter(dateMs: Long) = _userFromDateFilter.update { dateMs }

    // Add / Update user
    fun saveUser(user: UserEntity, plainPassword: String = "", onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to add/edit users.")
                return@launch
            }
            try {
                if (user.id == 0) {
                    val result = userRepo.register(user, plainPassword)
                    if (result.isSuccess) onDone(true, "Account added successfully")
                    else onDone(false, result.exceptionOrNull()?.message ?: "Failed")
                } else {
                    userRepo.updateUser(user)
                    onDone(true, "User updated")
                }
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    fun deactivateUser(userId: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            userRepo.deactivateUser(userId); onDone()
        }
    }

    fun deleteUser(user: UserEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            userRepo.deleteUser(user); onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // PRODUCTS
    // ──────────────────────────────────────────────────────────
    private val _productQuery = MutableStateFlow("")
    private val _productCategory = MutableStateFlow(-1)

    val productQuery = _productQuery.asStateFlow()
    val productCategory = _productCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = combine(
        _productQuery, _productCategory
    ) { q, c -> Pair(q, c) }
        .flatMapLatest { (q, c) -> productRepo.searchProducts(q, c, onlyApproved = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowStockProducts = productRepo.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onProductQueryChange(q: String) = _productQuery.update { q }
    fun onProductCategoryChange(id: Int) = _productCategory.update { id }

    fun saveProduct(product: ProductEntity, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to add/edit products.")
                return@launch
            }
            try {
                if (productRepo.isProductNameDuplicate(product.name, product.id)) {
                    onDone(false, "A product named '${product.name}' already exists.")
                    return@launch
                }
                if (product.id == 0) productRepo.insertProduct(product)
                else productRepo.updateProduct(product)
                onDone(true, "Product saved")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    fun deleteProduct(product: ProductEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            productRepo.deleteProduct(product); onDone()
        }
    }

    suspend fun getProductById(id: Int): ProductEntity? = productRepo.getProductById(id)
    suspend fun getUserById(id: Int): UserEntity? = userRepo.getUserById(id)

    // ──────────────────────────────────────────────────────────
    // CATEGORIES
    // ──────────────────────────────────────────────────────────
    val categories: StateFlow<List<CategoryEntity>> = categoryRepo.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveCategory(category: CategoryEntity, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to manage categories.")
                return@launch
            }
            try {
                if (category.id == 0) categoryRepo.insertCategory(category)
                else categoryRepo.updateCategory(category)
                onDone(true, "Category saved")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    fun deleteCategory(category: CategoryEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            categoryRepo.deleteCategory(category); onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // COURIERS
    // ──────────────────────────────────────────────────────────
    val couriers: StateFlow<List<CourierEntity>> = courierRepo.getAllCouriers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveCourier(courier: CourierEntity, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to manage couriers.")
                return@launch
            }
            try {
                if (courier.id == 0) courierRepo.insertCourier(courier)
                else courierRepo.updateCourier(courier)
                onDone(true, "Courier saved")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    fun deleteCourier(courier: CourierEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            courierRepo.deleteCourier(courier); onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // ORDERS
    // ──────────────────────────────────────────────────────────
    private val _orderStatusFilter = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val orders: StateFlow<List<OrderEntity>> = _orderStatusFilter
        .flatMapLatest { status -> orderRepo.filterOrders(status) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onOrderStatusFilter(status: String) = _orderStatusFilter.update { status }

    fun updateOrderStatus(orderId: Int, status: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            orderRepo.updateOrderStatus(orderId, status); onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // TRANSACTIONS (PAYMENTS)
    // ──────────────────────────────────────────────────────────
    private val _txStatusFilter = MutableStateFlow("")
    private val _txPayModeFilter = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = combine(
        _txStatusFilter, _txPayModeFilter
    ) { s, p -> Pair(s, p) }
        .flatMapLatest { (s, p) -> transactionRepo.filterTransactions(s, p) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onTxStatusFilter(s: String) = _txStatusFilter.update { s }
    fun onTxPayModeFilter(p: String) = _txPayModeFilter.update { p }

    fun approveTransaction(id: Int, remarks: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            transactionRepo.updateStatus(id, "APPROVED", remarks)
            onDone()
        }
    }

    fun rejectTransaction(id: Int, remarks: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            transactionRepo.updateStatus(id, "REJECTED", remarks)
            onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // ANNOUNCEMENTS
    // ──────────────────────────────────────────────────────────
    private val _annTypeFilter = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val announcements: StateFlow<List<AnnouncementEntity>> = _annTypeFilter
        .flatMapLatest { type -> announcementRepo.filterAnnouncements(type) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onAnnTypeFilter(type: String) = _annTypeFilter.update { type }

    fun saveAnnouncement(announcement: AnnouncementEntity, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to manage announcements.")
                return@launch
            }
            try {
                if (announcement.id == 0) announcementRepo.insert(announcement)
                else announcementRepo.update(announcement)
                onDone(true, "Announcement saved")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    fun deleteAnnouncement(announcement: AnnouncementEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            announcementRepo.delete(announcement); onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // STOCK MOVEMENTS (for stock report)
    // ──────────────────────────────────────────────────────────
    val recentStockMovements = stockMoveRepo.getRecentMovements(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ──────────────────────────────────────────────────────────
    // ADJUST STOCK MANUALLY
    // ──────────────────────────────────────────────────────────
    fun adjustStock(
        product: ProductEntity,
        newQty: Int,
        reason: String,
        adminId: Int,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) return@launch
            productRepo.adjustStock(
                productId = product.id,
                currentStock = product.stockQuantity,
                newStock = newQty,
                movementType = if (newQty > product.stockQuantity) "STOCK_IN" else "STOCK_OUT",
                reason = reason,
                movedByUserId = adminId
            )
            onDone()
        }
    }

    // ──────────────────────────────────────────────────────────
    // ADMIN LOGOUT
    // ──────────────────────────────────────────────────────────
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onLogout()
        }
    }
}
