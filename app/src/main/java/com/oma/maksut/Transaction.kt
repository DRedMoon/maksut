package com.oma.maksut

import com.oma.maksut.Category
/**
 * Yksittäisen tapahtuman malli.
 *
 * @param iconRes   Kuvake resurssina (esim. R.drawable.ic_bank)
 * @param label     Tapahtuman nimi tai kuvaus (esim. \"Palkka\")
 * @param amount    Summan määrä (positiivinen tulo, negatiivinen meno)
 * @param time      Aikaleima merkkijonona (esim. \"12:34\")
 */
data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val iconRes: Int,
    val label: String,
    val amount: Double,
    val time: String,       // esim. "2025-06-09"
    val rate: Double = 0.0,
    val category: Category  // uusi kenttä
)
