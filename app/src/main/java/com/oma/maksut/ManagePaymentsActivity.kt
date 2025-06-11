package com.oma.maksut

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ManagePaymentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_payments)

        // 1) “X”-painike sulkee tämän sivun
        findViewById<TextView>(R.id.tv_close_settings)
            .setOnClickListener { finish() }

        // Alavalikot, vie omille sivuille
        findViewById<TextView>(R.id.tv_loans).setOnClickListener {
            startActivity(Intent(this, LoansActivity::class.java))
        }
        findViewById<TextView>(R.id.tv_credits).setOnClickListener {
            startActivity(Intent(this, CreditsActivity::class.java))
        }
        findViewById<TextView>(R.id.tv_subscriptions).setOnClickListener {
            startActivity(Intent(this, SubscriptionsActivity::class.java))
        }
    }
}
