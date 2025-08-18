package com.secureapps.dms.android.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.R
import com.secureapps.dms.android.model.Transaction

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val showCustomerInfo: Boolean
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        val product: TextView = itemView.findViewById(R.id.product)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.transactionDate)
        val customer_name: TextView = itemView.findViewById(R.id.customer_name)
        val mobile_number: TextView = itemView.findViewById(R.id.mobile_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.transactionId.text = "TransactionId : ${transaction.TransactionId}"

        // ✅ Only add notes if not null/blank
        val notesPart = if (transaction.Notes.isNotBlank()) " (${transaction.Notes})" else ""
        holder.product.text = "Product : ${transaction.Product}$notesPart"

        holder.quantity.text = "Quantity : ${transaction.Quantity}"
        holder.amount.text = "Amount : ₹${"%.2f".format(transaction.Amount)}"
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

    override fun getItemCount(): Int = transactions.size

    fun updateData(newData: List<Transaction>) {
        transactions = newData
        notifyDataSetChanged()
    }
}