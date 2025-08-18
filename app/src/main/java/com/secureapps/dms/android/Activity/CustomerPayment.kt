package com.secureapps.dms.android.Activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.secureapps.dms.android.R

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.Adapter.PaymentReportAdapter

import com.secureapps.dms.android.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerPayment : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentReportAdapter
    private lateinit var progressBar: android.widget.ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_payment)

        // Set the Toolbar as the ActionBar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Customer Payment"
            setDisplayHomeAsUpEnabled(true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        val paymentModeSwitch = findViewById<SwitchCompat>(R.id.paymentModeSwitch)
        val qrCodeImage = findViewById<ImageView>(R.id.qrCodeImage)
        val utrEditText = findViewById<EditText>(R.id.utrEditText)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val submitButton = findViewById<Button>(R.id.submitButton)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PaymentReportAdapter()
        recyclerView.adapter = adapter

        // Load payment reports
        loadPaymentReports()

        // Set switch listener
        paymentModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                qrCodeImage.visibility = ImageView.VISIBLE
                utrEditText.visibility = EditText.VISIBLE
                amountEditText.visibility = EditText.VISIBLE
                submitButton.visibility = Button.VISIBLE
                paymentModeSwitch.text = "Payment Mode: ON"
            } else {
                qrCodeImage.visibility = ImageView.GONE
                utrEditText.visibility = EditText.GONE
                amountEditText.visibility = EditText.GONE
                submitButton.visibility = Button.GONE
                paymentModeSwitch.text = "Make Payment"
            }
        }

        submitButton.setOnClickListener {
            val utr = utrEditText.text.toString()
            val amount = amountEditText.text.toString()

            if (utr.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please enter UTR and Amount", Toast.LENGTH_SHORT).show()
            } else {
                // Here you would process the payment
                Toast.makeText(this, "Payment submitted!", Toast.LENGTH_SHORT).show()
                // After successful payment, refresh the list
                loadPaymentReports()
            }
        }
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
                            this@CustomerPayment,
                            "Failed to load payment reports",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@CustomerPayment,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
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