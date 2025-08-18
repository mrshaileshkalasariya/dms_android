package com.secureapps.dms.android.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.Model.PaymentReport
import com.secureapps.dms.android.R

class PaymentReportAdapter(private val paymentList: MutableList<PaymentReport> = mutableListOf()) :
    RecyclerView.Adapter<PaymentReportAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPaymentId: TextView = itemView.findViewById(R.id.tvPaymentId)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvUtrNumber: TextView = itemView.findViewById(R.id.tvUtrNumber)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvPaymentDate: TextView = itemView.findViewById(R.id.tvPaymentDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_report, parent, false)
        return PaymentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val currentItem = paymentList[position]

        holder.tvPaymentId.text = "Payment ID: ${currentItem.PaymentId}"
        holder.tvCustomerName.text = "Customer: ${currentItem.CustomerFirstName} ${currentItem.CustomerLastName}"
        holder.tvUtrNumber.text = "UTR: ${currentItem.UTRnumber}"
        holder.tvAmount.text = "Amount: ₹${currentItem.Amount}"
        holder.tvStatus.text = "Status: ${currentItem.PaymentStatus}"
        holder.tvPaymentDate.text = "Date: ${currentItem.FormattedPaymentDate}"

        // Set status color
        when (currentItem.PaymentStatus.uppercase()) {
            "SUCCESS" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            "FAILED" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // Red
            "PENDING" -> holder.tvStatus.setTextColor(Color.parseColor("#FFC107")) // Amber
            else -> holder.tvStatus.setTextColor(Color.parseColor("#2196F3")) // Blue
        }
    }

    override fun getItemCount() = paymentList.size

    fun updateList(newList: List<PaymentReport>) {
        paymentList.clear()
        paymentList.addAll(newList)
        notifyDataSetChanged()
    }
}