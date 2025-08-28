package com.secureapps.dms.android.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.secureapps.dms.android.PaymentReport
import com.secureapps.dms.android.R
import androidx.appcompat.app.AlertDialog


class BranchPaymentReportAdapter(
    private val paymentList: MutableList<PaymentReport> = mutableListOf(),
    private val onActionClick: (paymentId: Int, action: Int) -> Unit
) : RecyclerView.Adapter<BranchPaymentReportAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPaymentId: TextView = itemView.findViewById(R.id.tvPaymentId)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvUtrNumber: TextView = itemView.findViewById(R.id.tvUtrNumber)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvPaymentDate: TextView = itemView.findViewById(R.id.tvPaymentDate)
        val tvCustomerMobile: TextView = itemView.findViewById(R.id.tvCustomerMobile)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch_payment_report, parent, false)
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
        holder.tvCustomerMobile.text = "CustomerMobile: ${currentItem.CustomerMobile}"

        // Set status color
        when (currentItem.PaymentStatus.uppercase()) {
            "SUCCESS" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "FAILED" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            "PENDING" -> holder.tvStatus.setTextColor(Color.parseColor("#FFC107"))
            "APPROVED" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "REJECTED" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            "APPROVAL PENDING" -> holder.tvStatus.setTextColor(Color.parseColor("#FFC107"))
            else -> holder.tvStatus.setTextColor(Color.parseColor("#2196F3"))
        }

        // Show buttons only for APPROVAL PENDING status
        if (currentItem.PaymentStatus.equals("APPROVAL PENDING", ignoreCase = true)) {
            holder.btnApprove.visibility = View.VISIBLE
            holder.btnReject.visibility = View.VISIBLE
        } else {
            holder.btnApprove.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
        }

//        // ✅ Button Clicks -> Send action
//        holder.btnApprove.setOnClickListener {
//            onActionClick(currentItem.PaymentId, 1) // Approve
//        }
//        holder.btnReject.setOnClickListener {
//            onActionClick(currentItem.PaymentId, 0) // Reject
//        }
        // ✅ Approve button
        holder.btnApprove.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirm Approval")
                .setMessage("Are you sure you want to approve this payment?")
                .setPositiveButton("Yes") { _, _ ->
                    onActionClick(currentItem.PaymentId, 1) // Approve
                }
                .setNegativeButton("No", null) // dismiss
                .show()
        }

// ❌ Reject button
        holder.btnReject.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirm Rejection")
                .setMessage("Are you sure you want to reject this payment?")
                .setPositiveButton("Yes") { _, _ ->
                    onActionClick(currentItem.PaymentId, 0) // Reject
                }
                .setNegativeButton("No", null) // dismiss
                .show()
        }
    }

    override fun getItemCount() = paymentList.size

    fun updateList(newList: List<PaymentReport>) {
        paymentList.clear()
        paymentList.addAll(newList)
        notifyDataSetChanged()
    }
}
