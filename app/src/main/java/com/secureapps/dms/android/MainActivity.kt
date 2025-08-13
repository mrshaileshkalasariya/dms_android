package com.secureapps.dms.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.secureapps.dms.android.adapter.TransactionAdapter
import com.secureapps.dms.android.ApiInterface.ApiService
import com.secureapps.dms.android.model.ReportResponse
import com.secureapps.dms.android.Retrofit.RetrofitClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var monthSpinner: Spinner
    private lateinit var filterButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedMonth: String = ""

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

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            supportActionBar?.title = "Report"
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

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        // Initialize adapter with empty list
        transactionAdapter = TransactionAdapter(emptyList())
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
                        if (reportResponse.data.isNotEmpty()) {
                            // Update the existing adapter's data
                            transactionAdapter.updateData(reportResponse.data)
                        } else {
                            Toast.makeText(this@MainActivity, "No data available", Toast.LENGTH_SHORT).show()
                            transactionAdapter.updateData(emptyList())
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching data", e)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun generateLast12Months(): List<String> {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return List(12) { i ->
            calendar.add(Calendar.MONTH, if (i == 0) 0 else -1)
            dateFormat.format(calendar.time)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_payment -> true.also { Log.w("MainActivity", "Payment clicked") }
            R.id.action_logout -> true.also { showLogoutDialog() }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Logout") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}