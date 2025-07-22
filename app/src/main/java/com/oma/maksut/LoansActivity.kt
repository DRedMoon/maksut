package com.oma.maksut

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlin.math.abs
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.oma.maksut.repository.FinanceRepository
import java.text.SimpleDateFormat

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

        lifecycleScope.launch {
            val repository = FinanceRepository(this@LoansActivity)
            val loans = repository.getAllActiveLoans().first()

            // 4) Löydä korttien kontaineri
            val container = findViewById<LinearLayout>(R.id.ll_container_loans)
            val inflater = LayoutInflater.from(this@LoansActivity)

            // 5) Lisää jokainen laina korttipohjaan
            loans.forEach { loan ->
                val card = inflater.inflate(R.layout.item_payment_card, container, false)

                // Täytä kortin kentät
                card.findViewById<TextView>(R.id.tv_loan_name).text = loan.name
                card.findViewById<TextView>(R.id.tv_loan_remaining).text =
                    getString(R.string.loan_remaining_amount, kotlin.math.abs(loan.currentBalance))
                card.findViewById<TextView>(R.id.tv_loan_monthly).text =
                    getString(R.string.loan_monthly_payment, loan.monthlyPayment)
                card.findViewById<TextView>(R.id.tv_loan_rate).text =
                    getString(R.string.loan_interest_rate, loan.totalInterestRate)
                card.findViewById<TextView>(R.id.tv_loan_fee).text =
                    getString(R.string.loan_fee, loan.paymentFee)
                card.findViewById<TextView>(R.id.tv_loan_due).text =
                    getString(R.string.loan_due_date, loan.dueDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it) } ?: "-")

                // Lisää kortti näkyviin
                container.addView(card)
            }
        }
    }
}
