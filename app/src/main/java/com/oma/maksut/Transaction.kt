package com.oma.maksut

/**
 * Yksi maksutapahtuma:
 * @param iconRes  ikoni-resurssi (esim. pankin tai kulun symboli)
 * @param title    tapahtuman nimi (esim. "Palkka" tai "Ruoka")
 * @param amount   summa (positiivinen = tulo, negatiivinen = meno)
 */
data class Transaction(
    val iconRes: Int,
    val title: String,
    val amount: Double
)
