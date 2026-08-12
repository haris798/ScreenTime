package com.atharok.screentime.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseCredentials(
    val url: String = "",
    val anonKey: String = "",
    val email: String = "",
    val password: String = ""
)

@Serializable
data class SupabaseJsonRoot(
    val supabase: SupabaseCredentials = SupabaseCredentials()
)

data class SupabaseSyncStatus(
    val isConnected: Boolean = false,
    val lastSyncTime: Long = 0L,
    val errorMessage: String? = null
)
