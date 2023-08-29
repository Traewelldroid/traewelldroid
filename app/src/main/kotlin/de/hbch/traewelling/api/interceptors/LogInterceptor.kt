package de.hbch.traewelling.api.interceptors

import io.sentry.Sentry
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

            Sentry.captureMessage("[${response.code}] $path") { scope ->
                scope.setExtra("path", path)
                scope.setExtra("code", response.code.toString())
                scope.setExtra("responseBody", response.body?.string() ?: "no body")
                scope.setExtra("requestBody", requestBody)
            }
        }
        return response
    }
}
