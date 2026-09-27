package com.jipange.hardwaremobileerp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class MobileSaleItem(
    val sku: String,
    val quantity: Double
)

data class MobileCheckoutRequest(
    val paymentMethod: String? = "Cash",
    val contractorId: Int? = null,
    val items: List<MobileSaleItem>? = null
)

interface ApiService {

    // --- PRODUCTS ---
    @GET("api/products")
    suspend fun getAllProducts(): List<Product>

    @POST("api/products")
    suspend fun createProduct(@Body product: Product): Response<Map<String, Boolean>>

    @PUT("api/products")
    suspend fun updateProduct(@Body product: Product): Response<Map<String, Boolean>>

    // --- AUTHENTICATION ---
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // --- HR STAFF ---
    @GET("api/staff")
    suspend fun getAllStaff(@Header("User-Role") role: String = "Admin"): List<Employee>

    @POST("api/staff")
    suspend fun createStaff(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Boolean>>

    @PUT("api/staff/{id}")
    suspend fun updateStaff(@Path("id") id: Int, @Body employee: Employee): Response<Map<String, Boolean>>

    @POST("api/staff/{id}/reset-password")
    suspend fun resetStaffPassword(@Path("id") id: Int): Response<Map<String, Any>>

    // --- FINANCIALS & P&L ---
    @GET("api/financials")
    suspend fun getFinancialStats(@Header("User-Role") role: String = "Admin"): FinancialStats

    @GET("api/financials/pnl")
    suspend fun getProfitLoss(@Header("User-Role") role: String = "Admin"): ProfitLossSummary

    @GET("api/financials/ledger")
    suspend fun getGeneralLedger(@Header("User-Role") role: String = "Admin"): List<FinancialTransaction>

    // --- CONTRACTORS CRM ---
    @GET("api/contractors")
    suspend fun getAllContractors(): List<Contractor>

    @POST("api/contractors")
    suspend fun createContractor(@Body contractor: Contractor): Response<Map<String, Boolean>>

    @PUT("api/contractors")
    suspend fun updateContractor(@Body contractor: Contractor): Response<Map<String, Boolean>>

    @POST("api/contractors/{id}/payment")
    suspend fun recordContractorPayment(@Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Boolean>>

    // --- SUPPLIERS CRM ---
    @GET("api/suppliers")
    suspend fun getAllSuppliers(): List<Supplier>

    @POST("api/suppliers")
    suspend fun createSupplier(@Body supplier: Supplier): Response<Map<String, Boolean>>

    @PUT("api/suppliers")
    suspend fun updateSupplier(@Body supplier: Supplier): Response<Map<String, Boolean>>

    @POST("api/suppliers/{id}/payment")
    suspend fun recordSupplierPayment(@Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Boolean>>

    // --- LOGISTICS & FLEET ---
    @GET("api/logistics/vehicles")
    suspend fun getVehicles(): List<Vehicle>

    @POST("api/logistics/vehicles")
    suspend fun createVehicle(@Body vehicle: Vehicle): Response<Map<String, Boolean>>

    @GET("api/logistics/deliveries")
    suspend fun getDeliveries(): List<Delivery>

    @POST("api/logistics/deliveries")
    suspend fun dispatchDelivery(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Boolean>>

    @POST("api/logistics/deliveries/{id}/delivered")
    suspend fun markDelivered(@Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Boolean>>

    // --- SALES HISTORY & INVOICES ---
    @GET("api/sales")
    suspend fun getAllSales(): List<Sale>

    @GET("api/sales/{id}/items")
    suspend fun getSaleItems(@Path("id") id: Int): List<InvoiceItem>

    // --- AUDIT LOGS ---
    @GET("api/logs")
    suspend fun getLogs(@Header("User-Role") role: String = "Admin"): okhttp3.ResponseBody

    @GET("api/logs")
    suspend fun getAuditLogs(@Header("User-Role") role: String = "Admin"): List<SystemLog>


    // --- POS CHECKOUT & STK PUSH ---
    @POST("api/checkout")
    suspend fun processCheckout(@Body request: MobileCheckoutRequest = MobileCheckoutRequest()): Response<CheckoutResponse>

    @POST("api/paystack/stk")
    suspend fun triggerStkPush(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Any>>

    companion object {
        var BASE_URL = "https://hardware-erp-backend-xln9.onrender.com/"

        fun create(baseUrl: String = BASE_URL): ApiService {
            val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}