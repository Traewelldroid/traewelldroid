package de.hbch.traewelling.api.interceptors

import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.api.TraewellingApi
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest =
            chain
                .request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ${TraewellingApi.jwt}")
                .addHeader(
                    "User-Agent",
                    "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
                )
                .addHeader("Accept", "application/json")
                .build()

        return chain.proceed(newRequest)
    }
}
