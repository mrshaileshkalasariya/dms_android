package com.secureapps.dms.android.Activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.secureapps.dms.android.ApiInterface.ApiService
import com.secureapps.dms.android.ApiInterface.BranchRequest
import com.secureapps.dms.android.ApiInterface.CustomerResponse
import com.secureapps.dms.android.Retrofit.RetrofitClient
import com.secureapps.dms.android.Adapter.TransactionAdapter
import com.secureapps.dms.android.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import com.secureapps.dms.android.Transaction
import java.util.Calendar
import java.util.Locale

class BranchReport : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var monthSpinner: Spinner
    private lateinit var customerSpinner: Spinner
    private lateinit var filterButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noRecordsText: TextView
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedMonth: String = ""
    private var customerList = mutableListOf<String>()
    private var totalRecords = 0

    private var isFirstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_branch_report)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.setBackgroundDrawableResource(android.R.color.white)

        apiService = RetrofitClient.instance
        initViews()
        setupMonthSpinner()
        setupCustomerSpinner()
        setupRecyclerView()
        setupListeners()

        // ✅ Fetch customers for spinner
        fetchCustomers(branchId = 1)

        // ✅ Fetch transactions
        fetchTransactions()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstTime) {
            filterButton.post {
                filterButton.performClick()
            }
            isFirstTime = false
        }
    }

    private fun initViews() {
        monthSpinner = findViewById(R.id.monthSpinner)
        customerSpinner = findViewById(R.id.customerSpinner)
        filterButton = findViewById(R.id.filterButton)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        noRecordsText = findViewById(R.id.noRecordsText)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            supportActionBar?.title = "Branch Report"
            val homeDrawable = ResourcesCompat.getDrawable(resources, R.drawable.home, null)
            supportActionBar?.setHomeAsUpIndicator(homeDrawable)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupMonthSpinner() {
        val monthsList = generateLast12Months()
        monthSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            monthsList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        monthSpinner.setSelection(0)
        selectedMonth = monthsList[0]
    }

    private fun setupCustomerSpinner() {
        customerSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            customerList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun updateCustomerSpinner(customers: List<String>) {
        customerList.clear()
        customerList.add("All Customers")
        customerList.addAll(customers)
        (customerSpinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        transactionAdapter = TransactionAdapter(emptyList(), true, 0)
        recyclerView.adapter = transactionAdapter
    }

    private fun setupListeners() {
        filterButton.setOnClickListener {
            selectedMonth = monthSpinner.selectedItem.toString()
            fetchTransactions()
        }
    }

    // ✅ Fetch Customers for Spinner
    private fun fetchCustomers(branchId: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getCustomers(BranchRequest(branchId))
                if (response.isSuccessful) {
                    val customerResponse: CustomerResponse? = response.body()
                    if (customerResponse != null && customerResponse.data != null) {
                        val customerNames = customerResponse.data.map {
                            "${it.FirstName} ${it.LastName} (${it.Mobile})"
                        }
                        updateCustomerSpinner(customerNames)
                    }
                } else {
                    Toast.makeText(
                        this@BranchReport,
                        "Failed to load customers: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("BranchReport", "Error fetching customers", e)
            } finally {
                showLoading(false)
            }
        }
    }

    // Existing Transactions fetch
    private fun fetchTransactions() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getReports()
                if (response.isSuccessful) {
                    val reportResponse = response.body()
                    if (reportResponse != null) {
                        totalRecords = reportResponse.total

                        val selectedCustomer = customerSpinner.selectedItem?.toString() ?: "All Customers"
                        var filteredData = if (selectedCustomer == "All Customers") {
                            reportResponse.data
                        } else {
                            reportResponse.data?.filter {
                                "${it.CustomerFirstName} ${it.CustomerLastName} (${it.CustomerMobile})" == selectedCustomer
                            }
                        }

                        filteredData = filteredData?.let { filterByMonth(it, selectedMonth) }

                        if (!filteredData.isNullOrEmpty()) {
                            recyclerView.visibility = View.VISIBLE
                            noRecordsText.visibility = View.GONE
                            transactionAdapter.updateData(filteredData, totalRecords)
                        } else {
                            recyclerView.visibility = View.GONE
                            noRecordsText.visibility = View.VISIBLE
                            noRecordsText.text = "No transactions found."
                            transactionAdapter.updateData(emptyList(), 0)
                        }
                    }
                } else {
                    Toast.makeText(
                        this@BranchReport,
                        "Failed to load data: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("BranchReport", "Error fetching data", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun filterByMonth(transactions: List<Transaction>, selectedMonth: String): List<Transaction> {
        val apiDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        return transactions.filter { transaction ->
            try {
                val date = apiDateFormat.parse(transaction.FormattedTransactionDate)
                val transactionMonthYear = monthYearFormat.format(date!!)
                transactionMonthYear.equals(selectedMonth, ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            recyclerView.visibility = View.INVISIBLE
            noRecordsText.visibility = View.GONE
        }
    }

    private fun generateLast12Months(): List<String> {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return List(12) {
            val month = dateFormat.format(calendar.time)
            calendar.add(Calendar.MONTH, -1)
            month
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        for (i in 0 until menu?.size()!!) {
            val menuItem = menu.getItem(i)
            val spanString = SpannableString(menuItem?.title.toString())
            spanString.setSpan(ForegroundColorSpan(Color.BLACK), 0, spanString.length, 0)
            menuItem?.title = spanString
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Toast.makeText(this, "Page Reloaded", Toast.LENGTH_LONG).show()
                restartActivity()
                true
            }
            R.id.action_payment -> {
                startActivity(Intent(this@BranchReport, PaymentBranch::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun restartActivity() {
        val intent = Intent(this, BranchReport::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                this.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                cacheDir.deleteRecursively()
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
