package com.mindsparq.os.core.network

actual fun readPlatformEnv(name: String): String? = System.getenv(name)
