package com.atharok.screentime.domain.repositories

import com.atharok.screentime.domain.entities.Period
import com.atharok.screentime.domain.entities.ThemeEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<ThemeEntity>
    suspend fun saveTheme(themeEntity: ThemeEntity)

    fun useDynamicColors(): Flow<Boolean>
    suspend fun saveUseDynamicColors(useDynamicColors: Boolean)

    fun useBlackColorForDarkTheme(): Flow<Boolean>
    suspend fun saveUseBlackColorForDarkTheme(useBlackColorForDarkTheme: Boolean)

    fun getDefaultPeriod(): Flow<Period>
    suspend fun saveDefaultPeriod(period: Period)

    fun shouldIgnoreSystemApps(): Flow<Boolean>
    suspend fun saveIgnoreSystemApps(ignoreSystemApps: Boolean)

    fun getIgnoredPackages(): Flow<List<String>>
    suspend fun saveIgnoredPackages(ignoredPackages: List<String>)

    fun getSupabaseCredentials(): Flow<com.atharok.screentime.domain.entities.SupabaseCredentials>
    suspend fun saveSupabaseCredentials(credentials: com.atharok.screentime.domain.entities.SupabaseCredentials)

    fun isSupabaseConnected(): Flow<Boolean>
    suspend fun saveSupabaseIsConnected(isConnected: Boolean)

    fun getSupabaseLastSync(): Flow<Long>
    suspend fun saveSupabaseLastSync(timestamp: Long)
}