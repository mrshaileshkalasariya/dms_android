package com.secureapps.dms.android.ApiInterface

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login.php")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
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
    val PaymentQTmage: String?
)