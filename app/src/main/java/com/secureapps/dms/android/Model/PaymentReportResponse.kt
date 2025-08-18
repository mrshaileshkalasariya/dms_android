package com.secureapps.dms.android.Model

data class PaymentReportResponse(
    val status: Boolean,
    val data: List<PaymentReport>
)

data class PaymentReport(
    val PaymentId: Int,
    val CustomerId: Int,
    val UTRnumber: String,
    val Amount: Int,
    val PaymentStatus: String,
    val PaymentDate: String,
    val FormattedPaymentDate: String,
    val CustomerFirstName: String,
    val CustomerLastName: String,
    val CustomerMobile: String
)