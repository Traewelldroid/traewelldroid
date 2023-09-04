package de.hbch.traewelling.logging

import io.sentry.Sentry

class Logger private constructor(): ILogger {

    companion object {
        @Volatile
        private var instance: Logger? = null

        private fun getInstance() = instance ?: synchronized(this) {
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
        Sentry.captureException(t)
    }

    override fun captureMessage(message: String, additionalInfo: Map<String, String>) {
        Sentry.captureMessage(message) {
            additionalInfo.forEach { (key, value) ->
                it.setExtra(key, value)
            }
        }
    }
}
