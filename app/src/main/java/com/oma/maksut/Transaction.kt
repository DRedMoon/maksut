package com.oma.maksut

/**
 * Yksittäisen tapahtuman malli.
 *
 * @param iconRes   Kuvake resurssina (esim. R.drawable.ic_bank)
 * @param label     Tapahtuman nimi tai kuvaus (esim. \"Palkka\")
 * @param amount    Summan määrä (positiivinen tulo, negatiivinen meno)
 * @param time      Aikaleima merkkijonona (esim. \"12:34\")
 */
data class Transaction(
    val iconRes: Int,
    val label: String,
    val amount: Double,
    val time: String
)
