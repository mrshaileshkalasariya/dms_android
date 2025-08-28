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
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.secureapps.dms.android.Adapter.TransactionAdapter
import com.secureapps.dms.android.ApiInterface.ApiService
import com.secureapps.dms.android.R
import com.secureapps.dms.android.Retrofit.RetrofitClient
import com.secureapps.dms.android.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var monthSpinner: Spinner
    private lateinit var filterButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noRecordsText: TextView
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedMonth: String = ""
    private var totalRecords = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        apiService = RetrofitClient.instance
        initViews()
        setupMonthSpinner()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        monthSpinner = findViewById(R.id.monthSpinner)
        filterButton = findViewById(R.id.filterButton)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        noRecordsText = findViewById(R.id.noRecordsText)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            supportActionBar?.title = "Customer Report"

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

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        transactionAdapter = TransactionAdapter(emptyList(), false)
        recyclerView.adapter = transactionAdapter
        fetchTransactions()
    }

    private fun setupListeners() {
        filterButton.setOnClickListener {
            fetchTransactions()
        }
    }

    private fun fetchTransactions() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = apiService.getReports()
                if (response.isSuccessful) {
                    val reportResponse = response.body()
                    if (reportResponse != null) {
                        totalRecords = reportResponse.total

                        if (reportResponse.data.isNotEmpty()) {
                            // 🔹 Filter by month/year
                            val filteredData = filterByMonth(reportResponse.data, selectedMonth)

                            if (filteredData.isNotEmpty()) {
                                recyclerView.visibility = View.VISIBLE
                                noRecordsText.visibility = View.GONE
                                transactionAdapter.updateData(filteredData, filteredData.size)
                            } else {
                                recyclerView.visibility = View.GONE
                                noRecordsText.visibility = View.VISIBLE
                            }
                        } else {
                            recyclerView.visibility = View.GONE
                            noRecordsText.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load data: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching data", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun filterByMonth(transactions: List<Transaction>, selectedMonth: String): List<Transaction> {
        val apiDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault()) // API date
        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault()) // Spinner format

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
            val monthYear = dateFormat.format(calendar.time)
            calendar.add(Calendar.MONTH, -1)
            monthYear
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
                Toast.makeText(this, "Page Reloaded", Toast.LENGTH_SHORT).show()
                restartActivity()
                true
            }
            R.id.action_payment -> {
                Log.w("MainActivity", "Payment clicked")
                startActivity(Intent(this@MainActivity, CustomerPayment::class.java))
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
        val intent = Intent(this, MainActivity::class.java)
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
