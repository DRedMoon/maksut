package com.oma.maksut

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataImportExportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_import_export)

        // Paluu <-
        findViewById<TextView>(R.id.tv_back_data_import).setOnClickListener {
            finish()
        }

        // Lisää napit CSV-vienti ja CSV-tuonti: toiminnot myöhemmin
    }
}
