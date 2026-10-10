package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────
// USER ENTITY
// Stores all user types: ADMIN, MODERATOR, USER
// ─────────────────────────────────────────────
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]  // email must be unique
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String = "",                        // Firebase Authentication UID
    val firstName: String,
    val lastName: String,
    val gender: String,                          // "Male", "Female", "Other"
    val address: String,
    val contactNo: String,
    val email: String,
    val passwordHash: String,                    // SHA-256 hashed, never plain text
    val role: String,                            // "ADMIN", "MODERATOR", "USER""
    val domain: String? = null,                  // Moderator's product domain
    val dateOfRegistration: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,                // Soft delete support
    val fcmToken: String = ""                    // Firebase Cloud Messaging token
)

// ─────────────────────────────────────────────
// CATEGORY ENTITY
// Product categories: Chargers, Earphones, Cases, etc.
// ─────────────────────────────────────────────
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

// ─────────────────────────────────────────────
// PRODUCT ENTITY
// Core inventory item with stock tracking
// ─────────────────────────────────────────────
@Entity(
    tableName = "products",
    indices = [Index("categoryId"), Index("addedByUserId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val categoryId: Int?,
    val subCategory: String = "",                // Sub-category within category
    val price: Double,
    val stockQuantity: Int,                      // Current stock level
    val lowStockThreshold: Int = 5,             // Alert when stock ≤ this
    val addedByUserId: Int?,
    val isApproved: Boolean = false,            // Moderator must approve
    val dateAdded: Long = System.currentTimeMillis(),
    val imageUrl: String = ""                   // Optional product image path
)

// ─────────────────────────────────────────────
// COURIER ENTITY
// Delivery companies available for orders
// ─────────────────────────────────────────────
@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val contactNo: String,
    val email: String = "",
    val address: String = "",
    val deliveryCharges: Double = 0.0,
    val estimatedDays: Int = 3,                 // Estimated delivery days
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// PAYMENT COMPANY ENTITY
// Insurance & Payment receiving companies (e.g. Easypaisa, TCS Insurance)
// ─────────────────────────────────────────────
@Entity(tableName = "payment_companies")
data class PaymentCompanyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,                           // e.g. "Easypaisa", "TCS Insurance"
    val type: String,                           // "PAYMENT_RECEIVER", "INSURANCE", "BOTH"
    val accountNo: String = "",                 // Bank/Wallet account number (optional)
    val contactNo: String = "",
    val email: String = "",
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// ORDER ENTITY
// Customer order with product, courier, payment
// ─────────────────────────────────────────────
@Entity(
    tableName = "orders",
    indices = [Index("userId"), Index("productId"), Index("courierId"), Index("paymentCompanyId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val quantity: Int,
    val totalAmount: Double,                    // price × quantity + delivery
    val courierId: Int?,
    val paymentCompanyId: Int? = null,          // Linked payment/insurance company
    val paymentMode: String,                    // "CASH_ON_DELIVERY", "BANK_TRANSFER", "CARD"
    val status: String = "PENDING",             // PENDING → CONFIRMED → DISPATCHED → DELIVERED
    val deliveryAddress: String,
    val notes: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long? = null             // Set when delivered
)

// ─────────────────────────────────────────────
// TRANSACTION ENTITY
// Mock payment record linked to an order
// Admin manages approval of all transactions
// ─────────────────────────────────────────────
@Entity(
    tableName = "transactions",
    indices = [Index("orderId"), Index("userId"), Index("paymentCompanyId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,
    val userId: Int,
    val paymentCompanyId: Int? = null,          // Linked payment company
    val amount: Double,
    val paymentMode: String,                    // "CASH_ON_DELIVERY", "BANK_TRANSFER", "CARD"
    val status: String = "PENDING",             // PENDING, APPROVED, REJECTED
    val transactionDate: Long = System.currentTimeMillis(),
    val referenceNo: String = "TXN-${System.currentTimeMillis()}",
    val remarks: String = ""                    // Admin notes
)

// ─────────────────────────────────────────────
// ANNOUNCEMENT ENTITY
// Admin posts announcements by date and type
// ─────────────────────────────────────────────
@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val type: String,                           // "GENERAL", "SALE", "ALERT", "UPDATE"
    val isActive: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null               // Optional expiry
)

// ─────────────────────────────────────────────
// STOCK MOVEMENT ENTITY
// Logs all stock changes (in/out) for audit trail
// ─────────────────────────────────────────────
@Entity(
    tableName = "stock_movements",
    indices = [Index("productId")]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val movementType: String,                   // "STOCK_IN", "STOCK_OUT", "ADJUSTMENT"
    val quantity: Int,                          // Positive for in, negative for out
    val reason: String,                         // "Sale", "Manual Adjustment", "Return"
    val previousStock: Int,
    val newStock: Int,
    val movedByUserId: Int,
    val date: Long = System.currentTimeMillis()
)