package com.oma.maksut

/**
 * Keskitetty paikka testidatalle ja todellisille tapahtumille.
 */
object TransactionRepository {
    val transactions = mutableListOf<Transaction>(
        // Esimerkkilaskelma: Asuntolaina
        Transaction(
            iconRes        = R.drawable.ic_loan,
            label          = "Asuntolaina",
            amount         = -190800.0,
            time           = "2025-06-10",
            category       = Category.LOAN,
            rate           = 2.6760,
            fee            = 2.50,
            monthlyPayment = 878.65,
            dueDate        = "01"
        ),
        // Muutama muuta esimerkki
        Transaction(
            iconRes        = R.drawable.ic_shopping,
            label          = "Ruokakulut",
            amount         = -150.0,
            time           = "2025-06-05",
            category       = Category.EXPENSE
        ),
        Transaction(
            iconRes        = R.drawable.ic_bank,
            label          = "Palkka",
            amount         = +2500.0,
            time           = "2025-06-01",
            category       = Category.INCOME
        ),
        Transaction(
            iconRes        = R.drawable.ic_subscription,
            label          = "Netflix",
            amount         = -12.99,
            time           = "2025-06-01",
            category       = Category.SUBSCRIPTION
        )
    )
}
