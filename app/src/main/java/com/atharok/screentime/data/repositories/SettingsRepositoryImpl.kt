package com.atharok.screentime.data.repositories

import com.atharok.screentime.data.dataStore.SettingsDataStore
import com.atharok.screentime.domain.entities.Period
import com.atharok.screentime.domain.entities.ThemeEntity
import com.atharok.screentime.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore
): SettingsRepository {

    override suspend fun saveTheme(themeEntity: ThemeEntity) {
        settingsDataStore.saveTheme(themeEntity)
    }

    override fun getTheme(): Flow<ThemeEntity> = settingsDataStore.themeFlow

    override suspend fun saveUseDynamicColors(useDynamicColors: Boolean) {
        settingsDataStore.saveUseDynamicColors(useDynamicColors)
    }

    override fun useDynamicColors(): Flow<Boolean> = settingsDataStore.useDynamicColorsFlow

    override suspend fun saveUseBlackColorForDarkTheme(useBlackColorForDarkTheme: Boolean) {
        settingsDataStore.saveUseBlackColorForDarkTheme(useBlackColorForDarkTheme)
    }

    override fun useBlackColorForDarkTheme(): Flow<Boolean> = settingsDataStore.useBlackColorForDarkThemeFlow

    override fun getDefaultPeriod(): Flow<Period> = settingsDataStore.defaultPeriodFlow

    override suspend fun saveDefaultPeriod(period: Period) {
        settingsDataStore.saveDefaultPeriod(period)
    }

    override fun shouldIgnoreSystemApps(): Flow<Boolean> = settingsDataStore.ignoreSystemAppsFlow

    override suspend fun saveIgnoreSystemApps(ignoreSystemApps: Boolean) {
        settingsDataStore.saveIgnoreSystemApps(ignoreSystemApps)
    }

    override fun getIgnoredPackages(): Flow<List<String>> = settingsDataStore.ignoredPackagesFlow

    override suspend fun saveIgnoredPackages(ignoredPackages: List<String>) {
        settingsDataStore.saveIgnoredPackages(ignoredPackages)
    }

    override fun getSupabaseCredentials(): Flow<com.atharok.screentime.domain.entities.SupabaseCredentials> = settingsDataStore.supabaseCredentialsFlow

    override suspend fun saveSupabaseCredentials(credentials: com.atharok.screentime.domain.entities.SupabaseCredentials) {
        settingsDataStore.saveSupabaseCredentials(credentials)
    }

    override fun isSupabaseConnected(): Flow<Boolean> = settingsDataStore.supabaseIsConnectedFlow

    override suspend fun saveSupabaseIsConnected(isConnected: Boolean) {
        settingsDataStore.saveSupabaseIsConnected(isConnected)
    }

    override fun getSupabaseLastSync(): Flow<Long> = settingsDataStore.supabaseLastSyncFlow

    override suspend fun saveSupabaseLastSync(timestamp: Long) {
        settingsDataStore.saveSupabaseLastSync(timestamp)
    }

    override fun getDeviceName(): Flow<String> = settingsDataStore.deviceNameFlow

    override suspend fun saveDeviceName(name: String) {
        settingsDataStore.saveDeviceName(name)
    }
}