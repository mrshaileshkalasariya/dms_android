package com.secureapps.dms.android.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.Adapter.BranchPaymentReportAdapter
import com.secureapps.dms.android.ApiInterface.UpdatePaymentRequest
import com.secureapps.dms.android.R
import com.secureapps.dms.android.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentBranch : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BranchPaymentReportAdapter
    private lateinit var progressBar: android.widget.ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_branch)

        // Toolbar setup
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Branch Payment"
            setDisplayHomeAsUpEnabled(true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView with click listener for payment updates
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BranchPaymentReportAdapter { paymentId, action ->
            updatePaymentStatus(paymentId, action)
        }
        recyclerView.adapter = adapter

        // Load payment reports
        loadPaymentReports()
    }

    private fun loadPaymentReports() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPaymentReports()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.status == true) {
                        val paymentData = response.body()?.data ?: emptyList()
                        adapter.updateList(paymentData)
                    } else {
                        Toast.makeText(
                            this@PaymentBranch,
                            "Failed to load payment reports",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@PaymentBranch,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ✅ NEW: Call API to update payment status
    private fun updatePaymentStatus(paymentId: Int, action: Int) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdatePaymentRequest(paymentId, action)
                val response = RetrofitClient.instance.updatePayment(request)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(
                            this@PaymentBranch,
                            response.body()?.message ?: "Payment updated",
                            Toast.LENGTH_LONG
                        ).show()

                        // Reload list after update
                        loadPaymentReports()
                    } else {
                        Toast.makeText(
                            this@PaymentBranch,
                            response.body()?.message ?: "Update failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@PaymentBranch,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
