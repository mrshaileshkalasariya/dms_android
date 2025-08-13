package com.secureapps.dms.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.R
import com.secureapps.dms.android.model.Transaction

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionId: TextView = itemView.findViewById(R.id.transactionId)
        val product: TextView = itemView.findViewById(R.id.product)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.transactionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.transactionId.text = "TXN-${transaction.TransactionId}"
        holder.product.text = transaction.Product
        holder.quantity.text = "Qty: ${transaction.Quantity}"
        holder.amount.text = "₹${"%.2f".format(transaction.Amount)}"
        holder.date.text = transaction.FormattedTransactionDate
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newData: List<Transaction>) {
        transactions = newData
        notifyDataSetChanged()
    }
}