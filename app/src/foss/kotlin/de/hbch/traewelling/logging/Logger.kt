package de.hbch.traewelling.logging

import android.util.Log

class Logger private constructor(): ILogger {

    companion object {
        @Volatile
        private var instance: Logger? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: Logger().also {
                instance = it
            }
        }

        fun captureException(t: Throwable) {
            getInstance().captureException(t)
        }

        fun captureMessage(message: String, additionalInfo: Map<String, String>) {
            getInstance().captureMessage(message, additionalInfo)
        }
    }

    override fun captureException(t: Throwable) {
        Log.e("Error", t.stackTraceToString())
    }

    override fun captureMessage(message: String, additionalInfo: Map<String, String>) {
        Log.i("Info", message)
    }
}
