package com.mindsparq.os

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        SupabaseConfig.client?.handleDeeplinks(intent)
        setContent {
            MindSparqApp(isDesktop = false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SupabaseConfig.client?.handleDeeplinks(intent)
    }
}
