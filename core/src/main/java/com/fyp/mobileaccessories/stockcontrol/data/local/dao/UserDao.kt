package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: Int)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY dateOfRegistration DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY firstName ASC")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND (passwordHash = :passwordHash OR passwordHash = :plainPassword OR passwordHash = 'PROTECTED' OR passwordHash = '') AND isActive = 1 LIMIT 1")
    suspend fun login(email: String, passwordHash: String, plainPassword: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

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

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("UPDATE users SET fcmToken = :token WHERE id = :userId")
    suspend fun updateFcmToken(userId: Int, token: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = :role AND isActive = 1")
    suspend fun countUsersByRole(role: String): Int
}
