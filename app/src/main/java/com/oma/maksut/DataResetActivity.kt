package com.oma.maksut

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DataResetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_reset)

        // Paluu <-
        findViewById<TextView>(R.id.tv_back_data_reset).setOnClickListener {
            finish()
        }

        // Tähän voit lisätä napit “Vahvista nollaus” tai “Peruuta”
    }
}
