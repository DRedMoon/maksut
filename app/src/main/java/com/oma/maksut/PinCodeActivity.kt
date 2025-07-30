package com.oma.maksut

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class PinCodeActivity : AppCompatActivity() {
    
    private lateinit var etPinCode: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code)
        
        try {
            setupViews()
            setupListeners()
        } catch (e: Exception) {
            android.util.Log.e("PinCodeActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupViews() {
        etPinCode = findViewById(R.id.et_pin_code)
        btnSubmit = findViewById(R.id.btn_submit)
        btnCancel = findViewById(R.id.btn_cancel)
        tvTitle = findViewById(R.id.tv_title)
        tvSubtitle = findViewById(R.id.tv_subtitle)
        
        // Set focus to PIN input
        etPinCode.requestFocus()
    }
    
    private fun setupListeners() {
        btnSubmit.setOnClickListener {
            validatePinCode()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
        
        // Allow submission on Enter key
        etPinCode.setOnEditorActionListener { _, _, _ ->
            validatePinCode()
            true
        }
    }
    
    private fun validatePinCode() {
        val enteredPin = etPinCode.text.toString()
        val savedPin = getSavedPinCode()
        
        if (enteredPin == savedPin) {
            // PIN is correct, proceed to main app
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // PIN is incorrect
            Toast.makeText(this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show()
            etPinCode.text.clear()
            etPinCode.requestFocus()
        }
    }
    
    private fun getSavedPinCode(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("pin_code", "") ?: ""
    }
} 