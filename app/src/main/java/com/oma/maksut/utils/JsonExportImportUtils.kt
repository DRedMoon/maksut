package com.oma.maksut.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oma.maksut.database.AppDatabase
import com.oma.maksut.database.entities.Transaction
import com.oma.maksut.database.entities.Category
import com.oma.maksut.database.entities.Loan
import com.oma.maksut.database.entities.Credit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlinx.coroutines.flow.first

// Data class for export/import
data class ExportData(
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val loans: List<Loan>,
    val credits: List<Credit>
)

class JsonExportImportUtils {
    
    companion object {
        private val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()
        
        suspend fun exportToJson(context: Context): String = withContext(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            
            val transactions = database.transactionDao().getAllTransactions().first()
            val categories = database.categoryDao().getAllCategories().first()
            val loans = database.loanDao().getAllLoans().first()
            val credits = database.creditDao().getAllCredits().first()
            
            val exportData = ExportData(
                transactions = transactions,
                categories = categories,
                loans = loans,
                credits = credits,
            )
            
            val jsonString = gson.toJson(exportData)
            
            // Check if encryption is enabled
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val encryptionEnabled = prefs.getBoolean("encryption_enabled", false)
            
            return@withContext if (encryptionEnabled) {
                EncryptionUtils.encrypt(jsonString, context)
            } else {
                jsonString
            }
        }
        
        suspend fun importFromJson(context: Context, jsonData: String) = withContext(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            
            // Check if data is encrypted
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val encryptionEnabled = prefs.getBoolean("encryption_enabled", false)
            
            val decryptedData = if (encryptionEnabled) {
                try {
                    EncryptionUtils.decrypt(jsonData, context)
                } catch (e: Exception) {
                    // Try as plain JSON if decryption fails
                    jsonData
                }
            } else {
                jsonData
            }
            
            val exportData = gson.fromJson(decryptedData, ExportData::class.java)
            
            // Clear existing data
            database.clearAllTables()
            
            // Import new data
            exportData.categories.forEach { category ->
                database.categoryDao().insertCategory(category)
            }
            
            exportData.loans.forEach { loan ->
                database.loanDao().insertLoan(loan)
            }
            
            exportData.credits.forEach { credit ->
                database.creditDao().insertCredit(credit)
            }
            
            exportData.transactions.forEach { transaction ->
                database.transactionDao().insertTransaction(transaction)
            }
        }
    }
}