package de.traewelling.api

import de.traewelling.api.models.BearerToken
import de.traewelling.api.models.LoginCredentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL =
    "https://traewelling.de/api/v0/"

private val client = OkHttpClient.Builder().addInterceptor(Interceptor() { chain ->
    val newRequest =
        chain
            .request()
            .newBuilder()
            .addHeader("Authorization", "Bearer ${TraewellingApi.jwt}")
            .build()

    chain.proceed(newRequest)
}).build()

private val retrofit =
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

interface AuthApiService {
    @POST("auth/login")
    fun login(@Body credentials: LoginCredentials): Call<BearerToken>
}

object TraewellingApi {
    var jwt: String = ""
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}