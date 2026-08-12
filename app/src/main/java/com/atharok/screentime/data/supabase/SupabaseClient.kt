package com.atharok.screentime.data.supabase

import android.content.Context
import android.provider.Settings
import com.atharok.screentime.domain.entities.SupabaseCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SupabaseClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun testConnection(credentials: SupabaseCredentials): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val urlString = credentials.url.trimEnd('/') + "/rest/v1/"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("apikey", credentials.anonKey)
            connection.setRequestProperty("Authorization", "Bearer ${credentials.anonKey}")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode in 200..299 || responseCode == 404 || responseCode == 401) {
                // Connection reached server
                true
            } else {
                false
            }
        }
    }

    suspend fun upsertUsageData(
        context: Context,
        credentials: SupabaseCredentials,
        appUsages: List<Triple<String, String, Long>>, // packageName, appName, totalTimeUsed
        customDeviceName: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            if (credentials.url.isBlank() || credentials.anonKey.isBlank()) {
                return@runCatching false
            }

            val deviceId = if (!customDeviceName.isNullOrBlank()) {
                customDeviceName
            } else {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
            }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            val todayDate = dateFormat.format(Date())

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val currentIsoTime = isoFormat.format(Date())

            val jsonArray = buildJsonArray {
                appUsages.forEach { (packageName, appName, timeUsed) ->
                    add(buildJsonObject {
                        put("device_id", deviceId)
                        put("usage_date", todayDate)
                        put("package_name", packageName)
                        put("app_name", appName)
                        put("total_time_in_foreground", timeUsed)
                        put("updated_at", currentIsoTime)
                    })
                }
            }

            val urlString = credentials.url.trimEnd('/') + "/rest/v1/screen_time_stats"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("apikey", credentials.anonKey)
            connection.setRequestProperty("Authorization", "Bearer ${credentials.anonKey}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonArray.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            responseCode in 200..299
        }
    }
}
