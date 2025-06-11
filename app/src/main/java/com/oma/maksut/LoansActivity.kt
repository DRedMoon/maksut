package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

/**
 * Näyttää listan lainoista korttimuodossa.
 * Sisältää:
 *  - Toolbar back‐nuolella
 *  - Lisää / Muokkaa -painikkeet
 *  - Dynaminen korttilista ll_container_loans‐kontainerissa
 */
class LoansActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loans)

        // 1) Toolbarin back‐nappi sulkee Activityn
        findViewById<MaterialToolbar>(R.id.toolbar_loans)
            .setNavigationOnClickListener { finish() }

        // 2) Toimintapainikkeet
        findViewById<Button>(R.id.btn_add_loan)
            .setOnClickListener {
                // TODO: Avaa AddLoanActivity dialogilla vähintään
            }
        findViewById<Button>(R.id.btn_edit_loan)
            .setOnClickListener {
                // TODO: Avaa EditLoanActivity valitulla lainalla
            }

        // 3) Hae lainat ja luo kortit
        val loans = TransactionRepository.transactions
            .filter { it.category == Category.LOAN }

        // 4) Löydä korttien kontaineri

        val container = findViewById<LinearLayout>(R.id.ll_container_loans)
        val inflater  = LayoutInflater.from(this)

        // 5) Lisää jokainen laina korttipohjaan
        loans.forEach { loan ->
            val card = inflater.inflate(R.layout.item_payment_card, container, false)

            // Täytä kortin kentät
            card.findViewById<TextView>(R.id.tv_loan_name).text      = loan.label
            card.findViewById<TextView>(R.id.tv_loan_remaining).text =
                String.format(Locale.getDefault(), "%.2f €", -loan.amount)
            card.findViewById<TextView>(R.id.tv_loan_monthly).text   =
                String.format(Locale.getDefault(), "%.2f €/kk", loan.monthlyPayment)
            card.findViewById<TextView>(R.id.tv_loan_rate).text      =
                String.format(Locale.getDefault(), "Korko %.2f%%", loan.rate)
            card.findViewById<TextView>(R.id.tv_loan_fee).text       =
                String.format(Locale.getDefault(), "Palkkio %.2f€", loan.fee)
            card.findViewById<TextView>(R.id.tv_loan_due).text       =
                String.format(Locale.getDefault(), "Eräpäivä ${loan.dueDate}")

            // Lisää kortti näkyviin
            container.addView(card)
        }
    }
}
