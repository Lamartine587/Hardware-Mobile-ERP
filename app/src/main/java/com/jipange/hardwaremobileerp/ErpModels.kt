package com.jipange.hardwaremobileerp

import com.google.gson.annotations.SerializedName

data class Contractor(
    val id: Int = 0,
    val companyName: String,
    val contactPerson: String,
    val phoneNumber: String,
    val creditLimit: Double,
    val currentBalance: Double,
    val status: String = "Active"
)

data class Supplier(
    val id: Int = 0,
    val supplierName: String,
    val contactPerson: String,
    val phoneNumber: String,
    val email: String? = null,
    val outstandingBalance: Double = 0.0,
    val status: String = "Active"
)

data class Vehicle(
    val id: Int = 0,
    val licensePlate: String,
    val vehicleType: String,
    val capacity: String,
    val status: String = "Available"
)

data class Delivery(
    val id: Int = 0,
    val invoiceNumber: String,
    val destination: String,
    val driverName: String,
    @SerializedName("licensePlate")
    val vehicleLicensePlate: String? = null,
    val dispatchTime: String? = null,
    val deliveryStatus: String = "Dispatched"
)

data class ProfitLossSummary(
    @SerializedName("grossRevenue")
    val totalSalesRevenue: Double = 0.0,
    @SerializedName("cogs")
    val totalCostOfGoods: Double = 0.0,
    val grossProfit: Double = 0.0,
    val grossMarginPercent: Double = 0.0,
    @SerializedName("operatingExpenses")
    val totalOperatingExpenses: Double = 0.0,
    val netProfit: Double = 0.0
)

data class FinancialTransaction(
    val id: Int = 0,
    val transactionRef: String,
    val direction: String,
    val category: String,
    val paymentMethod: String,
    val amount: Double,
    val payerPayee: String? = null,
    val description: String? = null,
    val createdBy: String,
    val transactionDate: String
)

data class Sale(
    val id: Int = 0,
    val receiptNumber: String,
    val cashierUsername: String,
    val paymentMethod: String,
    val contractorId: Int? = null,
    val totalAmount: Double,
    val saleDate: String? = null
)

data class InvoiceItem(
    val id: Int = 0,
    val sku: String,
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val subtotal: Double
)

data class SystemLog(
    val id: Int = 0,
    val username: String = "SYSTEM",
    val actionType: String = "INFO",
    val moduleName: String = "GENERAL",
    val description: String = "",
    val logTime: String = ""
)


