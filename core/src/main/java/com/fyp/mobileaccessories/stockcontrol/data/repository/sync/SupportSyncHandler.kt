package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CourierDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.PaymentCompanyDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.TransactionDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.PaymentCompanyEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SupportSyncHandler(
    private val transactionDao: TransactionDao,
    private val courierDao: CourierDao,
    private val paymentCompanyDao: PaymentCompanyDao,
    private val scope: CoroutineScope
) {
    private val TAG = "SupportSync"

    fun handleTransactionSync(snapshot: DataSnapshot) {
        scope.launch {
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    val orderId = child.child("orderId").getValue(Int::class.java) ?: continue
                    val userId = child.child("userId").getValue(Int::class.java) ?: continue
                    val paymentCompanyId = child.child("paymentCompanyId").getValue(Int::class.java)
                    val amount = child.child("amount").getValue(Double::class.java) ?: 0.0
                    val paymentMode = child.child("paymentMode").getValue(String::class.java) ?: ""
                    val status = child.child("status").getValue(String::class.java) ?: "PENDING"
                    val referenceNo = child.child("referenceNo").getValue(String::class.java) ?: ""
                    val remarks = child.child("remarks").getValue(String::class.java) ?: ""
                    val transactionDate = child.child("transactionDate").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val transaction = TransactionEntity(
                        id = id, orderId = orderId, userId = userId, paymentCompanyId = paymentCompanyId,
                        amount = amount, paymentMode = paymentMode, status = status,
                        referenceNo = referenceNo, remarks = remarks, transactionDate = transactionDate
                    )
                    transactionDao.insertTransaction(transaction)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing transaction: ${e.message}")
                }
            }
        }
    }

    fun handleCourierSync(snapshot: DataSnapshot) {
        scope.launch {
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val contactNo = child.child("contactNo").getValue(String::class.java) ?: ""
                    val email = child.child("email").getValue(String::class.java) ?: ""
                    val deliveryCharges = child.child("deliveryCharges").getValue(Double::class.java) ?: 0.0
                    val estimatedDays = child.child("estimatedDays").getValue(Int::class.java) ?: 3
                    val isActive = child.child("active").getValue(Boolean::class.java)
                        ?: child.child("isActive").getValue(Boolean::class.java) ?: true

                    val courier = CourierEntity(
                        id = id, name = name, contactNo = contactNo, email = email,
                        deliveryCharges = deliveryCharges, estimatedDays = estimatedDays, isActive = isActive
                    )
                    courierDao.insertCourier(courier)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing courier: ${e.message}")
                }
            }
        }
    }

    fun handlePaymentCompanySync(snapshot: DataSnapshot) {
        scope.launch {
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val type = child.child("type").getValue(String::class.java) ?: "PAYMENT_RECEIVER"
                    val accountNo = child.child("accountNo").getValue(String::class.java) ?: ""
                    val isActive = child.child("active").getValue(Boolean::class.java)
                        ?: child.child("isActive").getValue(Boolean::class.java) ?: true

                    val company = PaymentCompanyEntity(
                        id = id, name = name, type = type, accountNo = accountNo, isActive = isActive
                    )
                    paymentCompanyDao.insertPaymentCompany(company)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing payment company: ${e.message}")
                }
            }
        }
    }

    fun uploadTransaction(ref: DatabaseReference, tx: TransactionEntity) {
        val map = mapOf(
            "id" to tx.id,
            "orderId" to tx.orderId,
            "userId" to tx.userId,
            "paymentCompanyId" to (tx.paymentCompanyId as Any?),
            "amount" to tx.amount,
            "paymentMode" to tx.paymentMode,
            "status" to tx.status,
            "transactionDate" to tx.transactionDate,
            "referenceNo" to tx.referenceNo,
            "remarks" to tx.remarks
        )
        ref.child("transactions").child(tx.id.toString()).setValue(map)
    }

    fun uploadCourier(ref: DatabaseReference, courier: CourierEntity) {
        val map = mapOf(
            "id" to courier.id,
            "name" to courier.name,
            "contactNo" to courier.contactNo,
            "email" to courier.email,
            "deliveryCharges" to courier.deliveryCharges,
            "estimatedDays" to courier.estimatedDays,
            "isActive" to courier.isActive
        )
        ref.child("couriers").child(courier.id.toString()).setValue(map)
    }

    fun uploadPaymentCompany(ref: DatabaseReference, company: PaymentCompanyEntity) {
        val map = mapOf(
            "id" to company.id,
            "name" to company.name,
            "type" to company.type,
            "accountNo" to company.accountNo,
            "isActive" to company.isActive
        )
        ref.child("payment_companies").child(company.id.toString()).setValue(map)
    }
}
