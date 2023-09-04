package de.hbch.traewelling.logging

interface ILogger {
    fun captureException(t: Throwable)
    fun captureMessage(message: String, additionalInfo: Map<String, String>)
}
