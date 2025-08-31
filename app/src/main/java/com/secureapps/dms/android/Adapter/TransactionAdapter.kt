package com.secureapps.dms.android.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.R
import com.secureapps.dms.android.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val showCustomerInfo: Boolean,
    private var totalRecords: Int = 0
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TRANSACTION = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    // ViewHolder for transaction items
    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        val product: TextView = itemView.findViewById(R.id.product)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.transactionDate)
        val customer_name: TextView = itemView.findViewById(R.id.customer_name)
        val mobile_number: TextView = itemView.findViewById(R.id.mobile_number)
    }

    // ViewHolder for footer (total records)
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val totalRecords: TextView = itemView.findViewById(R.id.totalRecords)
        val reportDate: TextView = itemView.findViewById(R.id.reportDate)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == transactions.size) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            VIEW_TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_total_footer, parent, false)
                FooterViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> {
                val transaction = transactions[position]
                holder.transactionId.text = "Transaction Id : ${transaction.TransactionId}"

                val notesPart = if (transaction.Notes.isNotBlank()) " (${transaction.Notes})" else ""
                holder.product.text = "Product : ${transaction.Product}$notesPart"

                holder.quantity.text = "Quantity : ${transaction.Quantity}"
                holder.amount.text = "${"%.2f".format(transaction.Amount)}"
                holder.date.text = transaction.FormattedTransactionDate

                if (showCustomerInfo) {
                    holder.customer_name.visibility = View.VISIBLE
                    holder.mobile_number.visibility = View.VISIBLE
                    holder.customer_name.text =
                        "Customer Name : ${transaction.CustomerFirstName} ${transaction.CustomerLastName}"
                    holder.mobile_number.text = "Mobile No : ${transaction.CustomerMobile}"
                } else {
                    holder.customer_name.visibility = View.GONE
                    holder.mobile_number.visibility = View.GONE
                }
            }
            is FooterViewHolder -> {
                holder.totalRecords.text = "Total: $totalRecords"

                // Set current date in the footer
                val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                holder.reportDate.text = "Report generated on: $currentDate"
            }
        }
    }

    override fun getItemCount(): Int = transactions.size + 1 // +1 for footer

    fun updateData(newData: List<Transaction>, total: Int) {
        transactions = newData
        totalRecords = total
        notifyDataSetChanged()
    }

    fun updateTotalRecords(total: Int) {
        totalRecords = total
        notifyItemChanged(transactions.size) // Update only the footer
    }
}