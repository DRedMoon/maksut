package com.oma.maksut.utils

import android.content.Context
import android.os.Build
import com.oma.maksut.repository.FinanceRepository
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

object DiagnosticsUtils {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

    data class DiagnosticsReport(
        val appInfo: AppInfo,
        val database: DatabaseInfo,
        val events: List<Event> = emptyList(),
        val errors: List<ErrorLog> = emptyList(),
        val sync: SyncInfo? = null,
        val userNotes: String? = null
    )
    data class AppInfo(
        val version: String,
        val build: Int,
        val device: String,
        val androidVersion: String,
        val locale: String
    )
    data class DatabaseInfo(
        val schemaVersion: Int,
        val transactionCount: Int,
        val categoryCount: Int,
        val loanCount: Int,
        val creditCount: Int
    )
    data class Event(val timestamp: String, val type: String, val details: String? = null)
    data class ErrorLog(val timestamp: String, val message: String, val stack: String? = null)
    data class SyncInfo(val lastSync: String?, val filePath: String?, val status: String)

    private fun getDiagnosticsDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "diagnostics")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun logEvent(context: Context, type: String, details: String? = null) {
        val event = Event(dateFormat.format(Date()), type, details)
        appendToLog(context, "events.json", event)
    }

    fun logError(context: Context, message: String, stack: String? = null) {
        val error = ErrorLog(dateFormat.format(Date()), message, stack)
        appendToLog(context, "errors.json", error)
    }

    private fun appendToLog(context: Context, fileName: String, obj: Any) {
        val dir = getDiagnosticsDir(context)
        val file = File(dir, fileName)
        val list = if (file.exists()) {
            gson.fromJson(file.readText(), List::class.java) ?: mutableListOf<Any>()
        } else mutableListOf<Any>()
        val newList = list.toMutableList().apply { add(obj) }
        file.writeText(gson.toJson(newList))
    }

    suspend fun exportDiagnostics(context: Context, userNotes: String? = null, syncInfo: SyncInfo? = null): File {
        val repo = FinanceRepository(context)
        val appInfo = AppInfo(
            version = "1.0", // TODO: get from BuildConfig
            build = 1,
            device = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            locale = Locale.getDefault().toString()
        )
        val transactions = repo.getRealTransactions().first()
        val categories = repo.getAllCategories().first()
        val loans = repo.getAllActiveLoans().first()
        val credits = repo.getAllActiveCredits().first()
        val dbInfo = DatabaseInfo(
            schemaVersion = 1, // TODO: get from Room if possible
            transactionCount = transactions.size,
            categoryCount = categories.size,
            loanCount = loans.size,
            creditCount = credits.size
        )
        val eventsFile = File(getDiagnosticsDir(context), "events.json")
        val errorsFile = File(getDiagnosticsDir(context), "errors.json")
        val events = if (eventsFile.exists()) gson.fromJson(eventsFile.readText(), Array<Event>::class.java)?.toList() ?: emptyList() else emptyList()
        val errors = if (errorsFile.exists()) gson.fromJson(errorsFile.readText(), Array<ErrorLog>::class.java)?.toList() ?: emptyList() else emptyList()
        val report = DiagnosticsReport(appInfo, dbInfo, events, errors, syncInfo, userNotes)
        val outFile = File(getDiagnosticsDir(context), "diagnostics_${dateFormat.format(Date())}.json")
        outFile.writeText(gson.toJson(report))
        return outFile
    }
}