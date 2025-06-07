package com.oma.maksut

import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        // Paluu <- nappi ylhäällä:
        findViewById<TextView>(R.id.tv_back_language).setOnClickListener {
            finish()
        }

        // Alussa oletuskieli: Suomi
        findViewById<RadioButton>(R.id.rb_finnish).isChecked = true

        // Kun valitaan Suomen radio, poista English
        findViewById<RadioButton>(R.id.rb_finnish).setOnClickListener {
            findViewById<RadioButton>(R.id.rb_english).isChecked = false
        }

        // Kun valitaan English, poista Finnish
        findViewById<RadioButton>(R.id.rb_english).setOnClickListener {
            findViewById<RadioButton>(R.id.rb_finnish).isChecked = false
        }
    }
}
