package com.mindsparq.os.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

expect fun readPlatformEnv(name: String): String?

/**
 * Resolves Supabase connection in priority order:
 * 1. In-app runtime override (for self-hosted / open-source users)
 * 2. Build-time generated config (from .env / CI secrets for Play Store & desktop releases)
 * 3. Platform environment variables
 */
object SupabaseConfig {
    private var runtimeUrl: String? = null
    private var runtimeAnonKey: String? = null
    private var cachedClient: SupabaseClient? = null
    private var cachedKey: Pair<String, String>? = null

    val url: String?
        get() = runtimeUrl?.takeIf { it.isNotBlank() }
            ?: GeneratedSupabaseConfig.URL.takeIf { it.isNotBlank() }
            ?: readPlatformEnv("MINDSPARQ_SUPABASE_URL")?.takeIf { it.isNotBlank() }

    val anonKey: String?
        get() = runtimeAnonKey?.takeIf { it.isNotBlank() }
            ?: GeneratedSupabaseConfig.ANON_KEY.takeIf { it.isNotBlank() }
            ?: readPlatformEnv("MINDSPARQ_SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() }

    val isConfigured: Boolean
        get() = url != null && anonKey != null

    fun configureRuntime(supabaseUrl: String, supabaseAnonKey: String) {
        runtimeUrl = supabaseUrl.trim()
        runtimeAnonKey = supabaseAnonKey.trim()
        cachedClient = null
        cachedKey = null
    }

    val client: SupabaseClient?
        get() {
            val u = url ?: return null
            val k = anonKey ?: return null
            val pair = u to k
            if (cachedClient == null || cachedKey != pair) {
                cachedClient = createSupabaseClient(supabaseUrl = u, supabaseKey = k) {
                    install(Auth)
                    install(Postgrest)
                    install(Storage)
                }
                cachedKey = pair
            }
            return cachedClient
        }
}
