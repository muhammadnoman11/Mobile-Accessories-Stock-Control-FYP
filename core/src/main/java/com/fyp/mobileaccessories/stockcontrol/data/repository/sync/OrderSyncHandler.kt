package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.OrderDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OrderSyncHandler(
    private val orderDao: OrderDao,
    private val scope: CoroutineScope
) {
    private val TAG = "OrderSync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            val cloudIds = mutableSetOf<Int>()
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    cloudIds.add(id)
                    val userId = child.child("userId").getValue(Int::class.java) ?: 1
                    val productId = child.child("productId").getValue(Int::class.java) ?: 1
                    val quantity = child.child("quantity").getValue(Int::class.java) ?: 1
                    val totalAmount = child.child("totalAmount").getValue(Double::class.java) ?: 0.0
                    val courierId = child.child("courierId").getValue(Int::class.java)
                    val paymentCompanyId = child.child("paymentCompanyId").getValue(Int::class.java)
                    val paymentMode = child.child("paymentMode").getValue(String::class.java) ?: "COD"
                    val status = child.child("status").getValue(String::class.java) ?: "PENDING"
                    val deliveryAddress = child.child("deliveryAddress").getValue(String::class.java) ?: ""
                    val notes = child.child("notes").getValue(String::class.java) ?: ""
                    val orderDate = child.child("orderDate").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val deliveryDate = child.child("deliveryDate").getValue(Long::class.java)

                    val order = OrderEntity(
                        id = id, userId = userId, productId = productId, quantity = quantity,
                        totalAmount = totalAmount, courierId = courierId, paymentCompanyId = paymentCompanyId,
                        paymentMode = paymentMode, status = status, deliveryAddress = deliveryAddress,
                        notes = notes, orderDate = orderDate, deliveryDate = deliveryDate
                    )
                    orderDao.insertOrder(order)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing order: ${e.message}")
                }
            }
            val localOrders = orderDao.getAllOrdersList()
            for (local in localOrders) {
                if (local.id !in cloudIds) {
                    orderDao.deleteOrder(local)
                }
            }
        }
    }

    fun upload(ref: DatabaseReference, order: OrderEntity) {
        val map = mapOf(
            "id" to order.id,
            "userId" to order.userId,
            "productId" to order.productId,
            "quantity" to order.quantity,
            "totalAmount" to order.totalAmount,
            "courierId" to (order.courierId as Any?),
            "paymentCompanyId" to (order.paymentCompanyId as Any?),
            "paymentMode" to order.paymentMode,
            "status" to order.status,
            "deliveryAddress" to order.deliveryAddress,
            "notes" to order.notes,
            "orderDate" to order.orderDate,
            "deliveryDate" to (order.deliveryDate as Any?)
        )
        ref.child("orders").child(order.id.toString()).setValue(map)
    }
}
