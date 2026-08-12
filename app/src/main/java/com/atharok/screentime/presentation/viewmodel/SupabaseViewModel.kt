package com.atharok.screentime.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.atharok.screentime.data.supabase.SupabaseClient
import com.atharok.screentime.data.workers.SupabaseSyncWorker
import com.atharok.screentime.domain.entities.SupabaseCredentials
import com.atharok.screentime.domain.entities.SupabaseJsonRoot
import com.atharok.screentime.domain.usecases.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class SupabaseViewModel(
    private val settingsUseCase: SettingsUseCase,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val credentialsFlow: Flow<SupabaseCredentials> = settingsUseCase.getSupabaseCredentials()
    val isConnectedFlow: Flow<Boolean> = settingsUseCase.isSupabaseConnected()
    val lastSyncFlow: Flow<Long> = settingsUseCase.getSupabaseLastSync()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _testResultMessage = MutableStateFlow<String?>(null)
    val testResultMessage: StateFlow<String?> = _testResultMessage.asStateFlow()

    fun saveCredentials(credentials: SupabaseCredentials) {
        viewModelScope.launch {
            settingsUseCase.saveSupabaseCredentials(credentials)
        }
    }

    fun importJsonFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return false
            importJsonCredentials(content)
        } catch (e: Exception) {
            _testResultMessage.value = "Gagal membaca file JSON: ${e.localizedMessage}"
            false
        }
    }

    fun importFromDownloadFolder(context: Context): Boolean {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val jsonFiles = downloadDir.listFiles { file -> file.extension.equals("json", ignoreCase = true) }
            if (jsonFiles.isNullOrEmpty()) {
                _testResultMessage.value = "Tidak ditemukan file .json di folder Download"
                return false
            }
            // Pick the most recently modified json file
            val latestJsonFile = jsonFiles.maxByOrNull { it.lastModified() } ?: return false
            val content = latestJsonFile.readText()
            val success = importJsonCredentials(content)
            if (success) {
                _testResultMessage.value = "Kredensial berhasil diimpor dari ${latestJsonFile.name}"
            }
            success
        } catch (e: Exception) {
            _testResultMessage.value = "Gagal mengakses folder Download: ${e.localizedMessage}"
            false
        }
    }

    fun importJsonCredentials(jsonString: String): Boolean {
        return try {
            val credentials = try {
                val root = json.decodeFromString<SupabaseJsonRoot>(jsonString)
                root.supabase
            } catch (_: Exception) {
                json.decodeFromString<SupabaseCredentials>(jsonString)
            }

            if (credentials.url.isNotBlank()) {
                saveCredentials(credentials)
                _testResultMessage.value = "Kredensial Supabase berhasil diimpor!"
                testConnection()
                true
            } else {
                _testResultMessage.value = "Format JSON kredensial tidak valid"
                false
            }
        } catch (e: Exception) {
            _testResultMessage.value = "Gagal memproses JSON: ${e.localizedMessage}"
            false
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testResultMessage.value = null
            val credentials = settingsUseCase.getSupabaseCredentials().first()
            if (credentials.url.isBlank() || credentials.anonKey.isBlank()) {
                settingsUseCase.saveSupabaseIsConnected(false)
                _testResultMessage.value = "URL dan Anon Key tidak boleh kosong"
                _isTestingConnection.value = false
                return@launch
            }

            val result = supabaseClient.testConnection(credentials).getOrDefault(false)
            settingsUseCase.saveSupabaseIsConnected(result)
            _testResultMessage.value = if (result) {
                "Koneksi ke Supabase Berhasil! (Online)"
            } else {
                "Gagal terhubung ke Supabase (Offline)"
            }
            _isTestingConnection.value = false
        }
    }

    fun triggerManualSync(context: Context) {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = OneTimeWorkRequestBuilder<SupabaseSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncWork)
            _testResultMessage.value = "Auto-sync telah dipicu di background!"
        }
    }

    fun clearTestMessage() {
        _testResultMessage.value = null
    }
}
