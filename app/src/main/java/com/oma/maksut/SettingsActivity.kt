package com.oma.maksut

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.oma.maksut.utils.EncryptionUtils
import com.oma.maksut.utils.JsonExportImportUtils
import java.io.File
import java.util.*
import androidx.lifecycle.lifecycleScope

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var switchSync: SwitchMaterial
    private lateinit var switchEncryption: SwitchMaterial
    private lateinit var switchPinCode: SwitchMaterial
    private lateinit var etPinCode: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupViews()
        setupToolbar()
        setupListeners()
        loadSettings()
    }
    
    private fun setupViews() {
        switchTheme = findViewById(R.id.switch_theme)
        switchSync = findViewById(R.id.switch_sync)
        switchEncryption = findViewById(R.id.switch_encryption)
        switchPinCode = findViewById(R.id.switch_pin_code)
        etPinCode = findViewById(R.id.et_pin_code)
    }
    
    private fun setupToolbar() {
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupListeners() {
        // Theme switch
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            saveThemeSetting(isChecked)
            applyTheme(isChecked)
        }
        
        // Sync switch
        switchSync.setOnCheckedChangeListener { _, isChecked ->
            saveSyncSetting(isChecked)
        }
        
        // Encryption switch
        switchEncryption.setOnCheckedChangeListener { _, isChecked ->
            saveEncryptionSetting(isChecked)
            if (isChecked) {
                showEncryptionSetupDialog()
            }
        }
        
        // PIN code switch
        switchPinCode.setOnCheckedChangeListener { _, isChecked ->
            savePinCodeSetting(isChecked)
            etPinCode.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        // Export button
        findViewById<Button>(R.id.btn_export_json).setOnClickListener {
            lifecycleScope.launch {
                val jsonData = JsonExportImportUtils.exportToJson(this@SettingsActivity)
                val file = File(getExternalFilesDir(null), "maksut_backup_${System.currentTimeMillis()}.json")
                file.writeText(jsonData)
                
                Toast.makeText(this@SettingsActivity, 
                    "Backup exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        }
        
        // Import button
        findViewById<Button>(R.id.btn_import_json).setOnClickListener {
            lifecycleScope.launch {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/json"
                }
                startActivityForResult(intent, REQUEST_IMPORT_JSON)
            }
        }
        
        // Encryption algorithm selection
        findViewById<LinearLayout>(R.id.ll_encryption_algorithm).setOnClickListener {
            lifecycleScope.launch {
                showEncryptionAlgorithmDialog()
            }
        }
        
        // Sync frequency selection
        findViewById<LinearLayout>(R.id.ll_sync_frequency).setOnClickListener {
            lifecycleScope.launch {
                showSyncFrequencyDialog()
            }
        }
    }
    
    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        
        switchTheme.isChecked = prefs.getBoolean("theme_dark", false)
        switchSync.isChecked = prefs.getBoolean("sync_enabled", false)
        switchEncryption.isChecked = prefs.getBoolean("encryption_enabled", false)
        switchPinCode.isChecked = prefs.getBoolean("pin_code_enabled", false)
        
        etPinCode.setText(prefs.getString("pin_code", ""))
        etPinCode.visibility = if (switchPinCode.isChecked) android.view.View.VISIBLE else android.view.View.GONE
        
        // Update display texts
        updateEncryptionAlgorithmDisplay()
        updateSyncFrequencyDisplay()
    }
    
    private fun saveThemeSetting(isDark: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("theme_dark", isDark)
            .apply()
    }
    
    private fun saveSyncSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("sync_enabled", isEnabled)
            .apply()
    }
    
    private fun saveEncryptionSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("encryption_enabled", isEnabled)
            .apply()
    }
    
    private fun savePinCodeSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean("pin_code_enabled", isEnabled)
            .putString("pin_code", if (isEnabled) etPinCode.text.toString() else "")
            .apply()
    }
    
    private fun applyTheme(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun showEncryptionSetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.encryption_setup))
            .setMessage(getString(R.string.encryption_setup_message))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun showEncryptionAlgorithmDialog() {
        val algorithms = arrayOf("XChaCha20-Poly1305", "AES-256-GCM", "AES-256-CBC")
        val currentAlgorithm = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("encryption_algorithm", "XChaCha20-Poly1305") ?: "XChaCha20-Poly1305"
        
        val currentIndex = algorithms.indexOf(currentAlgorithm)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_encryption_algorithm))
            .setSingleChoiceItems(algorithms, currentIndex) { _, which ->
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("encryption_algorithm", algorithms[which])
                    .apply()
                updateEncryptionAlgorithmDisplay()
            }
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun showSyncFrequencyDialog() {
        val frequencies = arrayOf("Every hour", "Every 6 hours", "Daily", "Weekly")
        val currentFrequency = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("sync_frequency", "Daily") ?: "Daily"
        
        val currentIndex = frequencies.indexOf(currentFrequency)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_sync_frequency))
            .setSingleChoiceItems(frequencies, currentIndex) { _, which ->
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("sync_frequency", frequencies[which])
                    .apply()
                updateSyncFrequencyDisplay()
            }
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun updateEncryptionAlgorithmDisplay() {
        val algorithm = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("encryption_algorithm", "XChaCha20-Poly1305") ?: "XChaCha20-Poly1305"
        findViewById<TextView>(R.id.tv_encryption_algorithm).text = algorithm
    }
    
    private fun updateSyncFrequencyDisplay() {
        val frequency = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("sync_frequency", "Daily") ?: "Daily"
        findViewById<TextView>(R.id.tv_sync_frequency).text = frequency
    }
    
    private fun exportToJson() {
        try {
            val jsonData = JsonExportImportUtils.exportToJson(this)
            val file = File(getExternalFilesDir(null), "maksut_backup_${System.currentTimeMillis()}.json")
            file.writeText(jsonData)
            
            Toast.makeText(this, 
                "Backup exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, 
                "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun importFromJson() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT_JSON)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_IMPORT_JSON && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                lifecycleScope.launch {
                    try {
                        val jsonData = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        jsonData?.let {
                            JsonExportImportUtils.importFromJson(this@SettingsActivity, it)
                            Toast.makeText(this@SettingsActivity, 
                                "Import successful", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, 
                            "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset_settings -> {
                showResetSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showResetSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.reset_settings))
            .setMessage(getString(R.string.reset_settings_confirmation))
            .setPositiveButton(getString(R.string.reset)) { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun resetAllSettings() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .clear()
            .apply()
        
        loadSettings()
        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
    }
    
    companion object {
        private const val REQUEST_IMPORT_JSON = 1001
    }
}
