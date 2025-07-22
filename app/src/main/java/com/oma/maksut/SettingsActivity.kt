package com.oma.maksut

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.oma.maksut.utils.EncryptionUtils
import com.oma.maksut.utils.JsonExportImportUtils
import com.oma.maksut.utils.DiagnosticsUtils
import com.oma.maksut.utils.SyncUtils
import java.io.File
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var switchSync: SwitchMaterial
    private lateinit var switchEncryption: SwitchMaterial
    private lateinit var switchPinCode: SwitchMaterial
    private lateinit var etPinCode: EditText
    private lateinit var tvSyncFolderPath: TextView
    private lateinit var tvSyncStatus: TextView
    private val pickFolderLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            SyncUtils.setSyncFolderUri(this, uri)
            tvSyncFolderPath.text = uri.toString()
            Toast.makeText(this, "Synkronointikansio valittu", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        try {
            setupViews()
            setupToolbar()
            setupListeners()
            loadSettings()
        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show()
            finish()
        }
        tvSyncFolderPath = findViewById(R.id.tv_sync_folder_path)
        tvSyncStatus = findViewById(R.id.tv_sync_status)
        findViewById<Button>(R.id.btn_export_diagnostics).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val file = DiagnosticsUtils.exportDiagnostics(this@SettingsActivity)
                    Toast.makeText(this@SettingsActivity, "Raportti viety: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Virhe raportin viennissä", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("SettingsActivity", "Error exporting diagnostics", e)
                }
            }
        }
        findViewById<Button>(R.id.btn_view_events).setOnClickListener {
            val file = File(getExternalFilesDir(null), "diagnostics/events.json")
            if (file.exists()) {
                val text = file.readText()
                showTextDialog("Tapahtumat", text)
            } else {
                Toast.makeText(this, "Ei tapahtumalokia", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_view_errors).setOnClickListener {
            val file = File(getExternalFilesDir(null), "diagnostics/errors.json")
            if (file.exists()) {
                val text = file.readText()
                showTextDialog("Virheet", text)
            } else {
                Toast.makeText(this, "Ei virhelokia", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_pick_sync_folder).setOnClickListener {
            pickFolderLauncher.launch(null)
        }
        findViewById<Button>(R.id.btn_export_sync).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val uri = SyncUtils.getSyncFolderUri(this@SettingsActivity)
                    val ok = if (uri != null) {
                        SyncUtils.exportToUserSyncFolder(this@SettingsActivity)
                    } else {
                        SyncUtils.exportToSyncFile(this@SettingsActivity)
                        true
                    }
                    tvSyncStatus.text = if (ok) "Synkronointitiedosto viety" else "Virhe viennissä"
                } catch (e: Exception) {
                    tvSyncStatus.text = "Virhe viennissä"
                    android.util.Log.e("SettingsActivity", "Error exporting sync", e)
                }
            }
        }
        findViewById<Button>(R.id.btn_import_sync).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val uri = SyncUtils.getSyncFolderUri(this@SettingsActivity)
                    val ok = if (uri != null) {
                        SyncUtils.importFromUserSyncFolder(this@SettingsActivity)
                    } else {
                        SyncUtils.importFromSyncFile(this@SettingsActivity)
                    }
                    tvSyncStatus.text = if (ok) "Synkronointitiedosto tuotu" else "Virhe tuonnissa"
                } catch (e: Exception) {
                    tvSyncStatus.text = "Virhe tuonnissa"
                    android.util.Log.e("SettingsActivity", "Error importing sync", e)
                }
            }
        }
        // Show current sync folder path
        try {
            val uri = SyncUtils.getSyncFolderUri(this)
            tvSyncFolderPath.text = uri?.toString() ?: SyncUtils.getDefaultSyncFolder(this).absolutePath
        } catch (e: Exception) {
            tvSyncFolderPath.text = "Virhe synkronointikansion lataamisessa"
            android.util.Log.e("SettingsActivity", "Error loading sync folder", e)
        }
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
                try {
                    val jsonData = JsonExportImportUtils.exportToJson(this@SettingsActivity)
                    val file = File(getExternalFilesDir(null), "maksut_backup_${System.currentTimeMillis()}.json")
                    file.writeText(jsonData)
                    
                    Toast.makeText(this@SettingsActivity, 
                        "Backup exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, 
                        "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Import button
        findViewById<Button>(R.id.btn_import_json).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/json"
            }
            startActivityForResult(intent, REQUEST_IMPORT_JSON)
        }
        
        // Encryption algorithm selection
        findViewById<LinearLayout>(R.id.ll_encryption_algorithm).setOnClickListener {
            showEncryptionAlgorithmDialog()
        }
        
        // Sync frequency selection
        findViewById<LinearLayout>(R.id.ll_sync_frequency).setOnClickListener {
            showSyncFrequencyDialog()
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
            .edit { putBoolean("theme_dark", isDark) }
    }
    
    private fun saveSyncSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean("sync_enabled", isEnabled) }
    }
    
    private fun saveEncryptionSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putBoolean("encryption_enabled", isEnabled) }
    }
    
    private fun savePinCodeSetting(isEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit {
                putBoolean("pin_code_enabled", isEnabled)
                putString("pin_code", if (isEnabled) etPinCode.text.toString() else "")
            }
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
                    .edit { putString("encryption_algorithm", algorithms[which]) }
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
                    .edit { putString("sync_frequency", frequencies[which]) }
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
            .edit { clear() }
        
        loadSettings()
        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
    }
    
    private fun showTextDialog(title: String, text: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, null)
            .create()
        dialog.show()
    }
    
    companion object {
        private const val REQUEST_IMPORT_JSON = 1001
    }
}
