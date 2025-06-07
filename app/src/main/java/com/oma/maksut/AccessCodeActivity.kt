package com.oma.maksut

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccessCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access_code)

        // Paluu <-
        findViewById<TextView>(R.id.tv_back_access_code).setOnClickListener {
            finish()
        }

        // Täällä voit lisätä kentän PIN-koodille ja tallennusnapin
    }
}
