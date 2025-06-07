package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Liitetään settings-layout tähän Activityyn
        setContentView(R.layout.activity_settings)

        // 1) “Kieli” → avaa LanguageActivity
        findViewById<TextView>(R.id.tv_language).setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        // 2) “Ulkoasu” → avaa AppearanceActivity
        findViewById<TextView>(R.id.tv_appearance).setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }

        // 3) “Yksityisyys & Turvallisuus” → avaa PrivacySecurityActivity
        findViewById<TextView>(R.id.tv_privacy_security).setOnClickListener {
            startActivity(Intent(this, PrivacySecurityActivity::class.java))
        }

        // 4) “Sulje” (X-ikoni) ylhäältä: Sulkee SettingsActivityn
        findViewById<TextView>(R.id.tv_close_settings).setOnClickListener {
            finish()
        }
    }
}
