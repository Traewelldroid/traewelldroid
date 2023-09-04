package de.hbch.traewelling.api.interceptors

import de.hbch.traewelling.logging.Logger
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class LogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val copy = request.newBuilder().build()
        val response = chain.proceed(request)

        val ignoredCodes = listOf(401, 409)
        val path = request.url.encodedPath

        if (!response.isSuccessful && !ignoredCodes.contains(response.code)) {
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            val requestBody = buffer.readUtf8()
            buffer.close()

            val additionalInfo = mapOf(
                Pair("path", path),
                Pair("code", response.code.toString()),
                Pair("responseBody", response.body?.string() ?: "no body"),
                Pair("requestBody", requestBody)
            )

            Logger.captureMessage("[${response.code}] $path", additionalInfo)
        }
        return response
    }
}
