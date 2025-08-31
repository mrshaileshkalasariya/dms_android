package com.secureapps.dms.android

//data class Transaction(
//    val TransactionId: Int,
//    val Product: String,
//    val Quantity: Int,
//    val Amount: Double,
//    val FormattedTransactionDate: String,
//    val CustomerFirstName: String,
//    val CustomerLastName: String,
//    val CustomerMobile: String,
//    val Notes: String,
//)
//
//data class ReportResponse(
//    val total: Int,
//    val data: List<Transaction>,
//    val message: String
//)

data class Transaction(
    val TransactionId: Int,
    val BranchId: Int?,
    val CustomerId: Int?,
    val Product: String?,                 // nullable
    val Quantity: Int?,
    val Notes: String?,                   // nullable
    val Amount: Double?,
    val TransactionDate: String?,         // nullable
    val IsEdited: Int?,
    val EditedDate: String?,              // nullable
    val IsDeleted: Int?,
    val DeletedDate: String?,             // nullable
    val FormattedTransactionDate: String?,// nullable
    val CustomerFirstName: String?,       // nullable
    val CustomerLastName: String?,        // nullable
    val CustomerMobile: String?           // nullable
)

data class ReportResponse(
    val total: Int,
    val data: List<Transaction>?,  // API may send null
    val message: String?
)

