package com.fyp.mobileaccessories.stockcontrol.util

// ============================================================
// FILE: util/PasswordHasher.kt
// SHA-256 password hashing — never store plain text passwords
// ============================================================


import java.security.MessageDigest

object PasswordHasher {

    // Hash a plain text password using SHA-256
    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        // Convert byte array to hex string
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Verify a plain text password against a stored hash
    fun verify(plainText: String, hashedValue: String): Boolean =
        hash(plainText) == hashedValue
}