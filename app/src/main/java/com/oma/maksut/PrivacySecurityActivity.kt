package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacySecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_security)

        // Paluu <-
        findViewById<TextView>(R.id.tv_back_privacy).setOnClickListener {
            finish()
        }

        // 1) “Tietojen nollaus / Poista tallennetut” → avaa DataResetActivity
        findViewById<TextView>(R.id.tv_data_reset).setOnClickListener {
            startActivity(Intent(this, DataResetActivity::class.java))
        }

        // 2) “Pääsykoodin lukitus / Salaus” → avaa AccessCodeActivity
        findViewById<TextView>(R.id.tv_access_code).setOnClickListener {
            startActivity(Intent(this, AccessCodeActivity::class.java))
        }

        // 3) “Tietojen vienti / tuonti” → avaa DataImportExportActivity
        findViewById<TextView>(R.id.tv_data_import_export).setOnClickListener {
            startActivity(Intent(this, DataImportExportActivity::class.java))
        }
    }
}
