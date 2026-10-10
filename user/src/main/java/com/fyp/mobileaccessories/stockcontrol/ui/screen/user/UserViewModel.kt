package com.fyp.mobileaccessories.stockcontrol.ui.screen.user

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
// FILE: presentation/user/UserViewModel.kt
// ViewModel for all User (Customer) screens.
// Handles: product browsing/search, placing orders,
// viewing own orders and transactions, profile management.
// ============================================================

// ── UI state for PlaceOrderScreen ─────────────────────────────
data class PlaceOrderUiState(
    val selectedCourierId: Int? = null,
    val paymentMode: String = "CASH_ON_DELIVERY",
    val deliveryAddress: String = "",
    val quantity: Int = 1,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val orderId: Int? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    private val categoryRepo: CategoryRepository,
    private val courierRepo: CourierRepository,
    private val orderRepo: OrderRepository,
    private val transactionRepo: TransactionRepository,
    private val announcementRepo: AnnouncementRepository,
    private val userRepo: UserRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ── Real-time Connectivity ──────────────────────────────
    val isOnline: StateFlow<Boolean> = ConnectionDetector.observeConnectivity(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectionDetector.isConnected(context))

    // ── Session ───────────────────────────────────────────────
    val session = sessionManager.sessionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ──────────────────────────────────────────────────────────
    // PRODUCTS — search + category filter (only approved)
    // ──────────────────────────────────────────────────────────
    private val _productQuery = MutableStateFlow("")
    private val _productCategory = MutableStateFlow(-1)  // -1 = all categories

    val productQuery = _productQuery.asStateFlow()
    val productCategory = _productCategory.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = combine(
        _productQuery, _productCategory
    ) { q, c -> Pair(q, c) }
        .flatMapLatest { (q, c) ->
            // Only show approved products to customers
            productRepo.searchProducts(q, c, onlyApproved = true)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onProductQueryChange(q: String) = _productQuery.update { q }
    fun onCategoryChange(id: Int) = _productCategory.update { id }

    // Categories list for filter chips
    val categories: StateFlow<List<CategoryEntity>> = categoryRepo.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Get a single product by ID (for ProductDetailScreen)
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _selectedProduct.value = productRepo.getProductById(productId)
        }
    }

    suspend fun getProductById(id: Int) = productRepo.getProductById(id)

    // ──────────────────────────────────────────────────────────
    // COURIERS — for PlaceOrderScreen dropdown
    // ──────────────────────────────────────────────────────────
    val activeCouriers: StateFlow<List<CourierEntity>> = courierRepo.getActiveCouriers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ──────────────────────────────────────────────────────────
    // PLACE ORDER
    // ──────────────────────────────────────────────────────────
    private val _orderState = MutableStateFlow(PlaceOrderUiState())
    val orderState = _orderState.asStateFlow()

    // Field setters for PlaceOrderScreen
    fun onCourierSelect(id: Int) =
        _orderState.update { it.copy(selectedCourierId = id, error = null) }

    fun onPaymentModeChange(mode: String) =
        _orderState.update { it.copy(paymentMode = mode, error = null) }

    fun onAddressChange(addr: String) =
        _orderState.update { it.copy(deliveryAddress = addr, error = null) }

    fun onQuantityChange(qty: Int) =
        _orderState.update { it.copy(quantity = qty.coerceAtLeast(1), error = null) }

    fun onNotesChange(notes: String) = _orderState.update { it.copy(notes = notes) }

    // Populate delivery address from user profile
    fun prefillAddress() {
        viewModelScope.launch {
            val activeSession = sessionManager.sessionFlow.first() ?: return@launch
            val user = userRepo.getUserById(activeSession.userId)
            if (user != null && _orderState.value.deliveryAddress.isBlank()) {
                _orderState.update { it.copy(deliveryAddress = user.address) }
            }
        }
    }

    fun placeOrder(product: ProductEntity, courier: CourierEntity?, onSuccess: (Int) -> Unit) {
        val s = _orderState.value
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                _orderState.update { it.copy(error = "Internet connection required to place an order.") }
                return@launch
            }

            val activeSession = sessionManager.sessionFlow.first()
            if (activeSession == null) {
                _orderState.update { it.copy(error = "Please log in again") }
                return@launch
            }
            val userId = activeSession.userId

            // Validate before placing
            val error = when {
                s.quantity < 1 -> "Quantity must be at least 1"
                s.quantity > product.stockQuantity ->
                    "Only ${product.stockQuantity} units available"

                s.deliveryAddress.isBlank() -> "Delivery address is required"
                else -> null
            }
            if (error != null) {
                _orderState.update { it.copy(error = error) }; return@launch
            }

            val deliveryCharge = courier?.deliveryCharges ?: 0.0
            val total = (product.price * s.quantity) + deliveryCharge

            _orderState.update { it.copy(isLoading = true, error = null) }
            val result = orderRepo.placeOrder(
                OrderEntity(
                    userId = userId,
                    productId = product.id,
                    quantity = s.quantity,
                    totalAmount = total,
                    courierId = s.selectedCourierId,
                    paymentMode = s.paymentMode,
                    deliveryAddress = s.deliveryAddress.trim(),
                    notes = s.notes.trim()
                )
            )
            if (result.isSuccess) {
                val orderId = result.getOrNull()?.toInt() ?: 0
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        orderId = orderId
                    )
                }
                onSuccess(orderId)
            } else {
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Order failed"
                    )
                }
            }
        }
    }

    // Reset order form after success or when leaving the screen
    fun resetOrderState() = _orderState.update { PlaceOrderUiState() }

    // ──────────────────────────────────────────────────────────
    // MY ORDERS + TRANSACTIONS
    // ──────────────────────────────────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    val myOrders: StateFlow<List<OrderEntity>> = session
        .filterNotNull()
        .flatMapLatest { orderRepo.getOrdersByUser(it.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val myTransactions: StateFlow<List<TransactionEntity>> = session
        .filterNotNull()
        .flatMapLatest { transactionRepo.getTransactionsByUser(it.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ──────────────────────────────────────────────────────────
    // ANNOUNCEMENTS — active ones shown to users
    // ──────────────────────────────────────────────────────────
    val announcements: StateFlow<List<AnnouncementEntity>> =
        announcementRepo.getActiveAnnouncements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ──────────────────────────────────────────────────────────
    // USER PROFILE
    // ──────────────────────────────────────────────────────────
    private val _profile = MutableStateFlow<UserEntity?>(null)
    val profile = _profile.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            val activeSession = sessionManager.sessionFlow.first() ?: return@launch
            _profile.value = userRepo.getUserById(activeSession.userId)
        }
    }

    fun updateProfile(
        firstName: String, lastName: String,
        address: String, contactNo: String,
        newPassword: String,
        onDone: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to update profile.")
                return@launch
            }

            val current =
                _profile.value ?: run { onDone(false, "Profile not loaded"); return@launch }
            val updated = current.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                address = address.trim(),
                contactNo = contactNo.trim(),
                // Only re-hash if user entered a new password
                passwordHash = if (newPassword.isNotBlank()) PasswordHasher.hash(newPassword)
                else current.passwordHash
            )
            try {
                userRepo.updateUser(updated)
                // Refresh session name
                sessionManager.saveSession(
                    userId = updated.id,
                    role = updated.role,
                    name = "${updated.firstName} ${updated.lastName}",
                    email = updated.email
                )
                _profile.value = updated
                onDone(true, "Profile updated successfully")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Update failed")
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch { sessionManager.clearSession(); onLogout() }
    }
}
