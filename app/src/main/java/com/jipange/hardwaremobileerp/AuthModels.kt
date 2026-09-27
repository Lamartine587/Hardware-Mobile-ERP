package com.jipange.hardwaremobileerp

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val message: String,
    val role: String?,
    val username: String? = null,
    val permissions: String? = null,
    val forcePasswordChange: Boolean = false
)
data class CheckoutResponse(val message: String, val receiptNumber: String? = null)

data class Employee(
    val id: Int = 0,
    val fullName: String,
    val nationalId: String? = null,
    val kraPin: String? = null,
    val nssfNumber: String? = null,
    val nhifNumber: String? = null,
    val phoneNumber: String? = null,
    val basicSalary: Double = 0.0,
    val role: String,
    val username: String,
    val employmentStatus: String = "Active",
    val permissions: String? = "HOME",
    val forcePasswordChange: Boolean = false
)