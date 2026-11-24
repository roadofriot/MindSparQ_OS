package com.mindsparq.os.core.network

import io.github.jan.supabase.auth.AuthConfig

actual fun readPlatformEnv(name: String): String? = System.getenv(name)

actual fun configurePlatformAuth(config: AuthConfig) {
    config.httpCallbackConfig {
        httpPort = 54399
        htmlTitle = "MindSparQ OS — Authentication Complete"
    }
}
