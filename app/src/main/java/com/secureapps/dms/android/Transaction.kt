package com.secureapps.dms.android.model

data class Transaction(
    val TransactionId: Int,
    val Product: String,
    val Quantity: Int,
    val Amount: Double,
    val FormattedTransactionDate: String
)

data class ReportResponse(
    val total: Int,
    val data: List<Transaction>,
    val message: String
)