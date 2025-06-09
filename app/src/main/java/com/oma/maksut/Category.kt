package com.oma.maksut

// Tällä kerrotaan mihin kategoriaan kukin tapahtuma kuuluu
enum class Category {
    INCOME,       // tulot (palkka tms.)
    EXPENSE,      // kulut (shoppailu, ruoka tms.)
    LOAN,         // lainanlyhennykset
    SUBSCRIPTION,  // kuukausimaksut (Netflix, Spotify…)
    OTHER         // Muut
}