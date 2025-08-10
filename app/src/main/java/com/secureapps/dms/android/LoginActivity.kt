package com.secureapps.dms.android

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.secureapps.dms.android.ApiInterface.ApiService
import com.secureapps.dms.android.Retrofit.RetrofitClient
import com.secureapps.dms.android.ApiInterface.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            val selectedType = dropdownSpinner.selectedItemPosition // Get position instead of string

            if (mobile.isEmpty() || password.isEmpty() || selectedType == 0) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Map spinner position to usertype (1 for Customer, 2 for Branch)
            val userType = selectedType // Assuming position 1 is Customer, position 2 is Branch

            // Create login request
            val loginRequest = LoginRequest(
                mobile = mobile,
                password = password,
                usertype = userType
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

                                // Save user data if needed
                                val userData = loginResponse.data?.user

                                // Start MainActivity
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Login failed
                                Toast.makeText(this@LoginActivity, "Invalid credentials!", Toast.LENGTH_SHORT).show()
//                                Toast.makeText(
//                                    this@LoginActivity,
//                                    loginResponse?.message ?: "Invalid credentials",
//                                    Toast.LENGTH_SHORT
//                                ).show()
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