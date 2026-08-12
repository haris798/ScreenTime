package com.atharok.screentime.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.atharok.screentime.data.supabase.SupabaseClient
import com.atharok.screentime.domain.entities.Period
import com.atharok.screentime.domain.entities.usage.DeviceUsage
import com.atharok.screentime.domain.repositories.DeviceUsageRepository
import com.atharok.screentime.domain.usecases.SettingsUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class SupabaseSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val settingsUseCase: SettingsUseCase by inject()
    private val deviceUsageRepository: DeviceUsageRepository by inject()
    private val supabaseClient: SupabaseClient by inject()

    override suspend fun doWork(): Result {
        return try {
            val credentials = settingsUseCase.getSupabaseCredentials().first()
            if (credentials.url.isBlank() || credentials.anonKey.isBlank()) {
                settingsUseCase.saveSupabaseIsConnected(false)
                return Result.success()
            }

            // Test connection first
            val isOnline = supabaseClient.testConnection(credentials).getOrDefault(false)
            settingsUseCase.saveSupabaseIsConnected(isOnline)

            if (!isOnline) {
                return Result.retry()
            }

            // Fetch today's usage stats
            val now = System.currentTimeMillis()
            val timeInterval = com.atharok.screentime.common.utils.DateTimeUtils.computeDayIntervalFrom(now, 0)
            val dayTimestamp = com.atharok.screentime.common.utils.DateTimeUtils.truncateToDay(now)
            val dailyUsage = deviceUsageRepository.getDayDeviceUsage(timeInterval, dayTimestamp)
            val appUsages = dailyUsage.appUsageList.map { app ->
                Triple(app.packageName, app.appName, app.getTotalTimeUsed())
            }

            val success = supabaseClient.upsertUsageData(
                context = applicationContext,
                credentials = credentials,
                appUsages = appUsages
            ).getOrDefault(false)

            if (success) {
                settingsUseCase.saveSupabaseLastSync(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "SupabaseAutoSyncWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SupabaseSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
