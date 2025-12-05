package ru.fromchat.api

import android.util.Log
import io.ktor.client.plugins.logging.Logger

/**
 * Custom Android logger for Ktor that uses Android's Log system
 */
object AndroidKtorLogger : Logger {
    private const val TAG = "Ktor"
    
    override fun log(message: String) {
        Log.d(TAG, message)
    }
}

