package com.oma.maksut.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object SyncUtils {
    private const val PREF_SYNC_FOLDER = "sync_folder_uri"
    private const val SYNC_FILE_NAME = "maksut_sync.json"

    fun getDefaultSyncFolder(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "sync")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getSyncFile(context: Context): File {
        return File(getDefaultSyncFolder(context), SYNC_FILE_NAME)
    }

    suspend fun exportToSyncFile(context: Context): File = withContext(Dispatchers.IO) {
        val json = JsonExportImportUtils.exportToJson(context)
        val file = getSyncFile(context)
        file.writeText(json)
        file
    }

    suspend fun importFromSyncFile(context: Context): Boolean = withContext(Dispatchers.IO) {
        val file = getSyncFile(context)
        if (!file.exists()) return@withContext false
        val json = file.readText()
        JsonExportImportUtils.importFromJson(context, json)
        true
    }

    fun setSyncFolderUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString(PREF_SYNC_FOLDER, uri.toString())
        }
    }
    fun getSyncFolderUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        val uriStr = prefs.getString(PREF_SYNC_FOLDER, null)
        return uriStr?.toUri()
    }
    suspend fun exportToUserSyncFolder(context: Context): Boolean = withContext(Dispatchers.IO) {
        val uri = getSyncFolderUri(context) ?: return@withContext false
        val json = JsonExportImportUtils.exportToJson(context)
        val docFile = DocumentFile.fromTreeUri(context, uri) ?: return@withContext false
        val syncFile = docFile.findFile(SYNC_FILE_NAME) ?: docFile.createFile("application/json", SYNC_FILE_NAME)
        val outStream = context.contentResolver.openOutputStream(syncFile!!.uri) ?: return@withContext false
        outStream.use { it.write(json.toByteArray()) }
        true
    }
    suspend fun importFromUserSyncFolder(context: Context): Boolean = withContext(Dispatchers.IO) {
        val uri = getSyncFolderUri(context) ?: return@withContext false
        val docFile = DocumentFile.fromTreeUri(context, uri) ?: return@withContext false
        val syncFile = docFile.findFile(SYNC_FILE_NAME) ?: return@withContext false
        val inStream = context.contentResolver.openInputStream(syncFile.uri) ?: return@withContext false
        val json = inStream.bufferedReader().readText()
        JsonExportImportUtils.importFromJson(context, json)
        true
    }
}