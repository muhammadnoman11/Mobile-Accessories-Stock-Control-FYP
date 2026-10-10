package com.fyp.mobileaccessories.stockcontrol.moderator.ui.screen.moderator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.repository.OrderRepository
import com.fyp.mobileaccessories.stockcontrol.data.repository.ProductRepository
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// FILE: presentation/moderator/ModViewModel.kt
// ViewModel for all Moderator screens.
// Handles: product approval, order tracking, profile.
// ============================================================

@HiltViewModel
class ModViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    private val orderRepo: OrderRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ── Real-time Connectivity ──────────────────────────────
    val isOnline: StateFlow<Boolean> = ConnectionDetector.observeConnectivity(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectionDetector.isConnected(context))

    // ── Session ───────────────────────────────────────────────
    val session = sessionManager.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Dashboard counts ──────────────────────────────────────
    private val _pendingProductCount = MutableStateFlow(0)
    val pendingProductCount = _pendingProductCount.asStateFlow()

    private val _pendingOrderCount = MutableStateFlow(0)
    val pendingOrderCount = _pendingOrderCount.asStateFlow()

    fun loadDashboardStats() {
        viewModelScope.launch {
            _pendingProductCount.value = productRepo.countPending()
            _pendingOrderCount.value = orderRepo.countByStatus("PENDING")
        }
    }

    // ── Products awaiting approval ────────────────────────────
    val pendingProducts: StateFlow<List<ProductEntity>> = productRepo.getPendingProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Approve a product — requires internet
    fun approveProduct(productId: Int, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to approve product.")
                return@launch
            }
            try {
                productRepo.setApprovalStatus(productId, true)
                onDone(true, "Product approved")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    // Reject product — requires internet
    fun rejectProduct(productId: Int, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to reject product.")
                return@launch
            }
            try {
                val product = productRepo.getProductById(productId)
                product?.let { productRepo.deleteProduct(it) }
                onDone(true, "Product rejected")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    // ── Orders to track ───────────────────────────────────────
    val orders: StateFlow<List<OrderEntity>> = orderRepo.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getProductById(id: Int) = productRepo.getProductById(id)

    fun updateOrderStatus(orderId: Int, status: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!ConnectionDetector.isConnected(context)) {
                onDone(false, "Internet connection required to update order status.")
                return@launch
            }
            try {
                orderRepo.updateOrderStatus(orderId, status)
                onDone(true, "Order status updated")
            } catch (e: Exception) {
                onDone(false, e.message ?: "Error")
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────
    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch { sessionManager.clearSession(); onLogout() }
    }
}
