package com.secureapps.dms.android.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.secureapps.dms.android.Adapter.PaymentReportAdapter
import com.secureapps.dms.android.ApiInterface.PaymentRequest
import com.secureapps.dms.android.R
import com.secureapps.dms.android.Retrofit.RetrofitClient
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

        // Toolbar
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

        // RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PaymentReportAdapter()
        recyclerView.adapter = adapter

        // Load payment reports initially
        loadPaymentReports()

        // Switch listener
        paymentModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                qrCodeImage.visibility = ImageView.VISIBLE
                utrEditText.visibility = EditText.VISIBLE
                amountEditText.visibility = EditText.VISIBLE
                submitButton.visibility = Button.VISIBLE
                paymentModeSwitch.text = "Payment Mode: ON"

                // Load QR
                loadQrImage()
            } else {
                qrCodeImage.visibility = ImageView.GONE
                utrEditText.visibility = EditText.GONE
                amountEditText.visibility = EditText.GONE
                submitButton.visibility = Button.GONE
                paymentModeSwitch.text = "Make Payment"
            }
        }

        // Submit Button click -> Call API
        submitButton.setOnClickListener {
            val utr = utrEditText.text.toString().trim()
            val amount = amountEditText.text.toString().trim()

            // ✅ Hide system keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.let { view ->
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            if (utr.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please enter UTR and Amount", Toast.LENGTH_LONG).show()
            } else {
                submitPayment(utr, amount.toInt())
            }
        }
    }

    private fun submitPayment(utr: String, amount: Int) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = PaymentRequest(
                    customerId = 1,
                    utrNumber = utr,
                    amount = amount
                )

                val response = RetrofitClient.instance.setPayment(request)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(
                            this@CustomerPayment,
                            response.body()?.message ?: "Payment submitted!",
                            Toast.LENGTH_LONG
                        ).show()

                        // ✅ Clear form inputs
                        findViewById<EditText>(R.id.utrEditText).text.clear()
                        findViewById<EditText>(R.id.amountEditText).text.clear()

                        // ✅ Hide form
                        qrCodeImage.visibility = View.GONE
                        findViewById<EditText>(R.id.utrEditText).visibility = View.GONE
                        findViewById<EditText>(R.id.amountEditText).visibility = View.GONE
                        findViewById<Button>(R.id.submitButton).visibility = View.GONE

                        // ✅ Reset switch
                        val paymentModeSwitch = findViewById<SwitchCompat>(R.id.paymentModeSwitch)
                        paymentModeSwitch.isChecked = false
                        paymentModeSwitch.text = "Make Payment"

                        // Refresh list
                        loadPaymentReports()
                    } else {
                        Toast.makeText(
                            this@CustomerPayment,
                            "Failed: ${response.body()?.message ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@CustomerPayment, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadQrImage() {
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
                        val qrUrl = user?.PaymentQrImage?.replace("localhost", RetrofitClient.BaseIP)

                        if (!qrUrl.isNullOrEmpty()) {
                            Glide.with(this@CustomerPayment)
                                .load(qrUrl)
                                .error(R.drawable.dms_logo)
                                .into(qrCodeImage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustomerPayment, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@CustomerPayment,
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
