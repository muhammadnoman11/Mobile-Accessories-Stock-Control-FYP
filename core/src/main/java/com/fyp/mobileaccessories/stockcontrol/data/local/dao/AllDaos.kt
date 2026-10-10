package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.PaymentCompanyEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.StockMovementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// USER DAO
// CRUD + search for all users by name, role, date
// ─────────────────────────────────────────────
@Dao
interface UserDao {

    // Insert a new user (returns the new row ID)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    // Update existing user profile
    @Update
    suspend fun updateUser(user: UserEntity)

    // Soft delete — sets isActive = false
    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: Int)

    // Hard delete (admin only)
    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Get all active users as a Flow (auto-updates UI on change)
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY dateOfRegistration DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    // Get users by role (ADMIN / MODERATOR / USER)
    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    // Login — find user by email and hashed password
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash AND isActive = 1 LIMIT 1")
    suspend fun login(email: String, passwordHash: String): UserEntity?

    // Check if email already exists (for registration validation)
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

    // Search users by first name, last name, or contact number
    @Query("""
        SELECT * FROM users WHERE isActive = 1 AND (
            firstName LIKE '%' || :query || '%' OR
            lastName LIKE '%' || :query || '%' OR
            contactNo LIKE '%' || :query || '%' OR
            email LIKE '%' || :query || '%'
        ) AND (:role = '' OR role = :role)
        AND (:domain = '' OR domain LIKE '%' || :domain || '%')
        AND (:fromDate = 0 OR dateOfRegistration >= :fromDate)
        AND (:toDate = 0 OR dateOfRegistration <= :toDate)
        ORDER BY dateOfRegistration DESC
    """)
    fun searchUsers(
        query: String,
        role: String = "",
        domain: String = "",
        fromDate: Long = 0L,
        toDate: Long = 0L
    ): Flow<List<UserEntity>>

    // Get a single user by ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("UPDATE users SET fcmToken = :token WHERE id = :userId")
    suspend fun updateFcmToken(userId: Int, token: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    // Get a single user by Email
    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Get user count by role (for admin dashboard stats)
    @Query("SELECT COUNT(*) FROM users WHERE role = :role AND isActive = 1")
    suspend fun countUsersByRole(role: String): Int
}

// ─────────────────────────────────────────────
// CATEGORY DAO
// CRUD for product categories
// ─────────────────────────────────────────────
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    // Get all categories as a live list
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<CategoryEntity>
}

// ─────────────────────────────────────────────
// PRODUCT DAO
// Full CRUD + search by name, category, subcategory, date
// ─────────────────────────────────────────────
@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Get all approved products (what users see)
    @Query("SELECT * FROM products WHERE isApproved = 1 ORDER BY dateAdded DESC")
    fun getApprovedProducts(): Flow<List<ProductEntity>>

    // Get ALL products (admin view — including unapproved)
    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    // Get only unapproved products (moderator review queue)
    @Query("SELECT * FROM products WHERE isApproved = 0 ORDER BY dateAdded ASC")
    fun getPendingApprovalProducts(): Flow<List<ProductEntity>>

    // Search products by name, description, subcategory — with optional category filter
    @Query("""
        SELECT * FROM products WHERE
        (name LIKE '%' || :query || '%' OR
         description LIKE '%' || :query || '%' OR
         subCategory LIKE '%' || :query || '%')
        AND (:categoryId = -1 OR categoryId = :categoryId)
        AND (:onlyApproved = 0 OR isApproved = 1)
        ORDER BY dateAdded DESC
    """)
    fun searchProducts(
        query: String,
        categoryId: Int = -1,
        onlyApproved: Boolean = true
    ): Flow<List<ProductEntity>>

    // Get products with low stock (admin alert)
    @Query("SELECT * FROM products WHERE stockQuantity <= lowStockThreshold AND isApproved = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    // Approve a product (moderator action)
    @Query("UPDATE products SET isApproved = :approved WHERE id = :productId")
    suspend fun setApprovalStatus(productId: Int, approved: Boolean)

    // Update stock quantity after order or manual adjustment
    @Query("UPDATE products SET stockQuantity = :newQuantity WHERE id = :productId")
    suspend fun updateStock(productId: Int, newQuantity: Int)

    // Get a single product
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    // Count products with matching name (for duplicate name check TC-01-02)
    @Query("SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countProductsByName(name: String, excludeId: Int = 0): Int

    // Get products by category
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isApproved = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>>

    // Admin dashboard: total products count
    @Query("SELECT COUNT(*) FROM products WHERE isApproved = 1")
    suspend fun countApprovedProducts(): Int

    // Count pending approval
    @Query("SELECT COUNT(*) FROM products WHERE isApproved = 0")
    suspend fun countPendingProducts(): Int

    // Count low stock alerts
    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= lowStockThreshold AND isApproved = 1")
    suspend fun countLowStockProducts(): Int

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<ProductEntity>
}

// ─────────────────────────────────────────────
// COURIER DAO
// Manage delivery companies
// ─────────────────────────────────────────────
@Dao
interface CourierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: CourierEntity): Long

    @Update
    suspend fun updateCourier(courier: CourierEntity)

    @Delete
    suspend fun deleteCourier(courier: CourierEntity)

    @Query("SELECT * FROM couriers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCouriers(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers ORDER BY name ASC")
    fun getAllCouriers(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getCourierById(id: Int): CourierEntity?
}

// ─────────────────────────────────────────────
// ORDER DAO
// CRUD + search/filter for orders
// ─────────────────────────────────────────────
@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // Get all orders (admin)
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    // Get orders for a specific user
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrdersByUser(userId: Int): Flow<List<OrderEntity>>

    // Get orders by status
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    // Update order status (moderator/admin)
    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    // Search orders
    @Query("""
        SELECT * FROM orders WHERE
        (:status = '' OR status = :status)
        ORDER BY orderDate DESC
    """)
    fun filterOrders(status: String = ""): Flow<List<OrderEntity>>

    // Get a single order
    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): OrderEntity?

    // Count orders by status (dashboard)
    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun countOrdersByStatus(status: String): Int

    @Query("SELECT * FROM orders")
    suspend fun getAllOrdersList(): List<OrderEntity>

    // Total revenue from completed orders
    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM orders WHERE status = 'DELIVERED'")
    suspend fun getTotalRevenue(): Double
}

// ─────────────────────────────────────────────
// TRANSACTION DAO
// Mock payment management
// ─────────────────────────────────────────────
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    // Get all transactions (admin)
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Get transactions for a specific user
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    fun getTransactionsByUser(userId: Int): Flow<List<TransactionEntity>>

    // Update transaction status (admin: PENDING → APPROVED/REJECTED)
    @Query("UPDATE transactions SET status = :status, remarks = :remarks WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: Int, status: String, remarks: String = "")

    // Get transaction by ID
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    // Get transaction by order ID
    @Query("SELECT * FROM transactions WHERE orderId = :orderId LIMIT 1")
    suspend fun getTransactionByOrderId(orderId: Int): TransactionEntity?

    // Filter transactions by status or payment mode
    @Query("""
        SELECT * FROM transactions WHERE
        (:status = '' OR status = :status) AND
        (:paymentMode = '' OR paymentMode = :paymentMode)
        ORDER BY transactionDate DESC
    """)
    fun filterTransactions(status: String = "", paymentMode: String = ""): Flow<List<TransactionEntity>>

    // Dashboard stats
    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'")
    suspend fun countPendingTransactions(): Int
}

// ─────────────────────────────────────────────
// ANNOUNCEMENT DAO
// Admin creates/manages announcements
// ─────────────────────────────────────────────
@Dao
interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    // Get all active announcements
    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY dateCreated DESC")
    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>>

    // Get all announcements (admin view including inactive)
    @Query("SELECT * FROM announcements ORDER BY dateCreated DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    // Search by type or date range
    @Query("""
        SELECT * FROM announcements WHERE
        (:type = '' OR type = :type) AND
        (:fromDate = 0 OR dateCreated >= :fromDate)
        ORDER BY dateCreated DESC
    """)
    fun filterAnnouncements(type: String = "", fromDate: Long = 0L): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements")
    suspend fun getAllAnnouncementsList(): List<AnnouncementEntity>
}

// ─────────────────────────────────────────────
// STOCK MOVEMENT DAO
// Audit trail for all stock changes
// ─────────────────────────────────────────────
@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    // Get all movements for a product (history)
    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY date DESC")
    fun getMovementsForProduct(productId: Int): Flow<List<StockMovementEntity>>

    // Get all recent movements (admin log)
    @Query("SELECT * FROM stock_movements ORDER BY date DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 50): Flow<List<StockMovementEntity>>
}

// ─────────────────────────────────────────────
// PAYMENT COMPANY DAO
// Manage insurance/payment receiving companies
// ─────────────────────────────────────────────
@Dao
interface PaymentCompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentCompany(company: PaymentCompanyEntity): Long

    @Update
    suspend fun updatePaymentCompany(company: PaymentCompanyEntity)

    @Delete
    suspend fun deletePaymentCompany(company: PaymentCompanyEntity)

    @Query("SELECT * FROM payment_companies WHERE isActive = 1 ORDER BY name ASC")
    fun getActivePaymentCompanies(): Flow<List<PaymentCompanyEntity>>

    @Query("SELECT * FROM payment_companies ORDER BY name ASC")
    fun getAllPaymentCompanies(): Flow<List<PaymentCompanyEntity>>

    @Query("SELECT * FROM payment_companies WHERE id = :id LIMIT 1")
    suspend fun getPaymentCompanyById(id: Int): PaymentCompanyEntity?

    @Query("""
        SELECT * FROM payment_companies WHERE 
        (name LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchPaymentCompanies(query: String): Flow<List<PaymentCompanyEntity>>
}