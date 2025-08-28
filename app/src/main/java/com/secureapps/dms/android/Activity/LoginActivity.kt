package com.secureapps.dms.android.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.secureapps.dms.android.ApiInterface.ApiService
import com.secureapps.dms.android.Retrofit.RetrofitClient
import com.secureapps.dms.android.ApiInterface.LoginRequest
import com.secureapps.dms.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Optional: Remove any window background that might change with theme
        getWindow().setBackgroundDrawableResource(android.R.color.white);

        // Initialize Retrofit service
        apiService = RetrofitClient.instance

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val dropdownSpinner = findViewById<Spinner>(R.id.dropdownSpinner)

        // Dropdown values (first item is placeholder)
        val userTypes = listOf("Select login type", "Customer", "Branch")

        // Custom adapter to disable placeholder & set colors
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            userTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (placeholder)
                return position != 0
            }

            override fun getDropDownView(
                position: Int, convertView: View?, parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(Color.GRAY) // Placeholder color
                } else {
                    textView.setTextColor(Color.BLACK) // Normal options
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdownSpinner.adapter = adapter

        // Handle dropdown selection
        dropdownSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val selectedType = userTypes[position]
                    Log.e("DropdownSelection", "Selected: $selectedType")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Login button click
        loginButton.setOnClickListener {
            val mobile = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val selectedTypePosition = dropdownSpinner.selectedItemPosition

            if (mobile.isEmpty() || password.isEmpty() || selectedTypePosition == 0) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Add log here
            Log.d("LoginInput", "Mobile Entered: $mobile")
            Log.d("LoginInput", "Password Entered: $password")
            Log.d("LoginInput", "UserType: ${userTypes[selectedTypePosition]}")

            // Get the actual selected type as string
            val selectedType = userTypes[selectedTypePosition]

            // Create login request
            val loginRequest = LoginRequest(
                mobile = mobile,
                password = password,
                usertype = selectedTypePosition // Send position as usertype (1 for Customer, 2 for Branch)
            )

            // Call API in a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.login(loginRequest)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse?.status == true) {
                                // Login successful
                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                                // Determine which activity to launch based on user type
                                val intent = when (selectedType) {
                                    "Customer" -> Intent(this@LoginActivity, MainActivity::class.java)
                                    "Branch" -> Intent(this@LoginActivity, BranchReport::class.java)
                                    else -> Intent(this@LoginActivity, MainActivity::class.java) // default
                                }

                                // Example: if going to CustomerPayment activity
//                                val intent = Intent(this@LoginActivity, CustomerPayment::class.java)
                                intent.putExtra("MOBILE", mobile)
                                intent.putExtra("PASSWORD", password)
                                intent.putExtra("USERTYPE", selectedTypePosition)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                // Login failed
                                Toast.makeText(this@LoginActivity, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // API call failed
                            Toast.makeText(
                                this@LoginActivity,
                                "Login failed: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("LoginError", "API call failed", e)
                    }
                }
            }
        }
    }
}