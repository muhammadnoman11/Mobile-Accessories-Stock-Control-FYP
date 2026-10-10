package com.fyp.mobileaccessories.stockcontrol.util


// ============================================================
// FILE: util/UiState.kt
// Generic UI state wrapper for all screens
// ============================================================


// Sealed class to represent all possible UI states
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Constants for roles
object UserRole {
    const val ADMIN = "ADMIN"
    const val MODERATOR = "MODERATOR"
    const val USER = "USER"
    const val EMPLOYEE = "EMPLOYEE"
}

// Constants for order status
object OrderStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val DISPATCHED = "DISPATCHED"
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"
}

// Constants for payment modes
object PaymentMode {
    const val CASH_ON_DELIVERY = "CASH_ON_DELIVERY"
    const val BANK_TRANSFER = "BANK_TRANSFER"
    const val CARD = "CARD"

    fun displayName(mode: String): String = when (mode) {
        CASH_ON_DELIVERY -> "Cash on Delivery"
        BANK_TRANSFER -> "Bank Transfer"
        CARD -> "Card Payment"
        else -> mode
    }
}

// Constants for transaction status
object TransactionStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

// Constants for announcement types
object AnnouncementType {
    const val GENERAL = "GENERAL"
    const val SALE = "SALE"
    const val ALERT = "ALERT"
    const val UPDATE = "UPDATE"
}