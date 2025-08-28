package com.secureapps.dms.android.Activity

import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.secureapps.dms.android.ApiInterface.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerPayment : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentReportAdapter
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var qrCodeImage: ImageView

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
        qrCodeImage = findViewById(R.id.qrCodeImage)
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

                // ✅ Load QR Code when switch is ON
                loadQrImage()

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
                Toast.makeText(this, "Payment submitted!", Toast.LENGTH_SHORT).show()
                loadPaymentReports()
            }
        }
    }

    private fun loadQrImage() {
//        val mobile = intent.getStringExtra("MOBILE") ?: ""
//        val password = intent.getStringExtra("PASSWORD") ?: ""
//        val usertype = intent.getIntExtra("USERTYPE", 0)
//
//        Log.e("NextActivity", "Mobile: $mobile, Password: $password, UserType: $usertype")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.login(
                    com.secureapps.dms.android.ApiInterface.LoginRequest(
                        mobile = "1212121212",
                        password = "123456",
                        usertype = 1
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val user = response.body()?.data?.user
//                        val qrUrl = user?.PaymentQrImage?.replace("localhost", "10.227.14.202") // or BaseIP
                        val qrUrl = user?.PaymentQrImage?.replace("localhost", RetrofitClient.BaseIP) // or BaseIP

                        if (!qrUrl.isNullOrEmpty()) {
                            Glide.with(this@CustomerPayment)
                                .load(qrUrl)
//                                .placeholder(R.drawable.arrow)
                                .error(R.drawable.dms_logo)
                                .into(qrCodeImage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustomerPayment, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
