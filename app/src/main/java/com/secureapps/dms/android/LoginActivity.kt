package com.secureapps.dms.android

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val dropdownSpinner = findViewById<Spinner>(R.id.dropdownSpinner)

        // Dropdown values (first item is placeholder)
        val userTypes = listOf("Select login type", "Branch", "Customer")

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
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val selectedType = dropdownSpinner.selectedItem.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && selectedType != "Select login type") {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
