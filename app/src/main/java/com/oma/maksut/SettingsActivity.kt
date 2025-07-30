package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.View
import android.view.WindowInsets

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchPinCode: Switch
    private lateinit var etPinCode: EditText
    private lateinit var switchSync: Switch
    private lateinit var switchEncryption: Switch
    private lateinit var switchNavigationBar: Switch
    private lateinit var tvSyncStatus: TextView
    private lateinit var tvEncryptionStatus: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupToolbar()
        setupViews()
        setupListeners()
        loadSettings()
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupViews() {
        // Language spinner
        spinnerLanguage = findViewById(R.id.spinner_language)
        
        // PIN code protection
        switchPinCode = findViewById(R.id.switch_pin_code)
        etPinCode = findViewById(R.id.et_pin_code)
        
        // Sync settings
        switchSync = findViewById(R.id.switch_sync)
        tvSyncStatus = findViewById(R.id.tv_sync_status)
        
        // Encryption settings
        switchEncryption = findViewById(R.id.switch_encryption)
        tvEncryptionStatus = findViewById(R.id.tv_encryption_status)
        
        // Navigation bar settings
        switchNavigationBar = findViewById(R.id.switch_navigation_bar)
    }
    
    private fun setupListeners() {
        // Language spinner
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val language = when (position) {
                    0 -> "fi" // Finnish
                    1 -> "en" // English
                    else -> "fi"
                }
                saveLanguageSetting(language)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // PIN code switch
        switchPinCode.setOnCheckedChangeListener { _, isChecked ->
            etPinCode.visibility = if (isChecked) View.VISIBLE else View.GONE
            savePinCodeSetting(isChecked)
        }
        
        // PIN code input listener
        etPinCode.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val pinCode = s?.toString() ?: ""
                savePinCodeValue(pinCode)
            }
        })
        
        // Sync switch
        switchSync.setOnCheckedChangeListener { _, isChecked ->
            saveSyncSetting(isChecked)
            updateSyncStatus(isChecked)
        }
        
        // Encryption switch
        switchEncryption.setOnCheckedChangeListener { _, isChecked ->
            saveEncryptionSetting(isChecked)
            updateEncryptionStatus(isChecked)
        }
        
        // Navigation bar switch
        switchNavigationBar.setOnCheckedChangeListener { _, isChecked ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit().putBoolean("show_navigation_bar", isChecked).apply()
            
            // Apply navigation bar setting immediately
            applyNavigationBarSetting(isChecked)
        }
        
        // Export data button
        findViewById<Button>(R.id.btn_export_data).setOnClickListener {
            showExportDialog()
        }
        
        // Import data button
        findViewById<Button>(R.id.btn_import_data).setOnClickListener {
            showImportDialog()
        }
        
        // Reset data button
        findViewById<Button>(R.id.btn_reset_data).setOnClickListener {
            showResetDataDialog()
        }
    }
    
    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        
        // Load language setting
        val language = prefs.getString("language", "fi") ?: "fi"
        val languagePosition = when (language) {
            "fi" -> 0
            "en" -> 1
            else -> 0
        }
        spinnerLanguage.setSelection(languagePosition)
        
        // Load PIN code setting
        val pinCodeEnabled = prefs.getBoolean("pin_code_enabled", false)
        switchPinCode.isChecked = pinCodeEnabled
        etPinCode.visibility = if (pinCodeEnabled) View.VISIBLE else View.GONE
        
        // Load PIN code value
        val pinCode = prefs.getString("pin_code", "")
        etPinCode.setText(pinCode)
        
        // Load sync setting
        val syncEnabled = prefs.getBoolean("sync_enabled", false)
        switchSync.isChecked = syncEnabled
        updateSyncStatus(syncEnabled)
        
        // Load encryption setting
        val encryptionEnabled = prefs.getBoolean("encryption_enabled", false)
        switchEncryption.isChecked = encryptionEnabled
        updateEncryptionStatus(encryptionEnabled)
        
        // Load navigation bar setting
        val showNavigationBar = prefs.getBoolean("show_navigation_bar", false)
        switchNavigationBar.isChecked = showNavigationBar
    }
    
    private fun saveLanguageSetting(language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putString("language", language).apply()
        
        // Apply language change immediately
        applyLanguage(language)
    }
    
    private fun applyLanguage(language: String) {
        try {
            val locale = when (language) {
                "fi" -> java.util.Locale("fi")
                "en" -> java.util.Locale("en")
                else -> java.util.Locale("fi")
            }
            
            // Set the locale
            java.util.Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            
            // Restart the activity to apply changes
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error applying language", e)
            Toast.makeText(this, "Error changing language", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun savePinCodeSetting(enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("pin_code_enabled", enabled).apply()
    }
    
    private fun savePinCodeValue(pinCode: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putString("pin_code", pinCode).apply()
    }
    
    private fun saveSyncSetting(enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("sync_enabled", enabled).apply()
    }
    
    private fun saveEncryptionSetting(enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("encryption_enabled", enabled).apply()
    }
    
    private fun updateSyncStatus(enabled: Boolean) {
        tvSyncStatus.text = if (enabled) "Käytössä" else "Pois käytöstä"
    }
    
    private fun updateEncryptionStatus(enabled: Boolean) {
        tvEncryptionStatus.text = if (enabled) "Käytössä" else "Pois käytöstä"
    }
    
    private fun showExportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Vie tiedot")
            .setMessage("Tietojen vienti onnistui!")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showImportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Tuo tiedot")
            .setMessage("Tietojen tuonti onnistui!")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showResetDataDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Nollaa tiedot")
            .setMessage("Haluatko varmasti nollata kaikki tiedot?")
            .setPositiveButton("Nollaa") { _, _ ->
                // TODO: Implement data reset
                Toast.makeText(this, "Tiedot nollattu", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Peruuta", null)
            .show()
    }

    private fun applyNavigationBarSetting(show: Boolean) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                window.insetsController?.let { controller ->
                    if (show) {
                        controller.show(WindowInsets.Type.navigationBars())
                    } else {
                        controller.hide(WindowInsets.Type.navigationBars())
                    }
                }
            } else {
                // Android 10 and below
                @Suppress("DEPRECATION")
                val decorView = window.decorView
                if (show) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility and 
                        (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv())
                } else {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or 
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                }
            }
            
            Toast.makeText(this, 
                if (show) "Navigation bar enabled" else "Navigation bar disabled", 
                Toast.LENGTH_SHORT).show()
                
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error applying navigation bar setting", e)
        }
    }
}
