package com.secureapps.dms.android.ApiInterface

import com.secureapps.dms.android.PaymentReportResponse
import com.secureapps.dms.android.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login.php")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/get_report.php")  // Not iget_report.php
    suspend fun getReports(): Response<ReportResponse>

    @POST("api/get_payment_report.php")
    suspend fun getPaymentReports(): Response<PaymentReportResponse>

    // ✅ New API for updating payment status
    @POST("api/update_payment.php")
    suspend fun updatePayment(@Body request: UpdatePaymentRequest): Response<UpdatePaymentResponse>

    // ✅ New API for fetching customers
    @POST("api/get_customers.php")
    suspend fun getCustomers(@Body request: BranchRequest): Response<CustomerResponse>

    // ✅ New API for creating payment
    @POST("api/set_payment.php")
    suspend fun setPayment(@Body request: PaymentRequest): Response<PaymentResponse>
}

data class LoginRequest(
    val mobile: String,
    val password: String,
    val usertype: Int
)

data class LoginResponse(
    val status: Boolean,
    val usertype: String,
    val data: UserData?
)

data class UserData(
    val user: User?
)

data class User(
    val CustomerId: Int?,
    val CustomerCode: String?,
    val BranchId: Int?,
    val FirstName: String?,
    val LastName: String?,
    val Mobile: String?,
    val Address: String?,
    val OutstandingAmount: Int?,
    val LimitAmount: Int?,
    val LimitNotifyAmount: Int?,
    val PaymentQrImage: String?
)

// ------------------ ✅ New Models for update_payment ------------------ //
data class UpdatePaymentRequest(
    val paymentId: Int,
    val action: Int
)

data class UpdatePaymentResponse(
    val status: Boolean,
    val message: String
)

// ------------------ ✅ New Models for Customers ------------------ //
data class BranchRequest(
    val branchId: Int
)

data class CustomerResponse(
    val status: Boolean,
    val data: List<Customer>?
)

data class Customer(
    val CustomerId: Int,
    val BranchId: Int,
    val FirstName: String,
    val LastName: String,
    val Mobile: String
)

// ------------------ ✅ New Models for set_payment ------------------ //
data class PaymentRequest(
    val customerId: Int,
    val utrNumber: String,
    val amount: Int
)

data class PaymentResponse(
    val status: Boolean,
    val message: String,
    val paymentId: Int?
)