package com.oma.maksut

import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppearanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)

        // Paluu <-
        findViewById<TextView>(R.id.tv_back_appearance).setOnClickListener {
            finish()
        }

        // Tässä esimerkki: kaksi vaihtoehtoa: Light ja Dark

        findViewById<RadioButton>(R.id.rb_light).isChecked = true

        findViewById<RadioButton>(R.id.rb_light).setOnClickListener {
            findViewById<RadioButton>(R.id.rb_dark).isChecked = false
            // Voit tallentaa valinnan esim. SharedPreferencesiin
        }
        findViewById<RadioButton>(R.id.rb_dark).setOnClickListener {
            findViewById<RadioButton>(R.id.rb_light).isChecked = false
            // Vaihda sovelluksen teema tummaksi jne.
        }
    }
}
