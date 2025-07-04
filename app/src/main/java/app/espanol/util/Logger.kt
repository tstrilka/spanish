package app.espanol.util

import android.util.Log
import app.espanol.BuildConfig

object Logger {
    private const val TAG = "EspanolApp"
    private const val MAX_LOG_LENGTH = 4000

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            logChunked(Log.DEBUG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            logChunked(Log.ERROR, message, throwable)
        }
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            logChunked(Log.INFO, message)
        }
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG) {
            logChunked(Log.WARN, message)
        }
    }

    private fun logChunked(priority: Int, message: String, throwable: Throwable? = null) {
        if (message.length <= MAX_LOG_LENGTH) {
            Log.println(priority, TAG, message)
            throwable?.let { Log.println(priority, TAG, Log.getStackTraceString(it)) }
        } else {
            // Split long messages
            var i = 0
            while (i < message.length) {
                val end = minOf(i + MAX_LOG_LENGTH, message.length)
                Log.println(priority, TAG, message.substring(i, end))
                i = end
            }
        }
    }
}