package de.traewelling.api

import android.util.Log
import com.google.gson.GsonBuilder
import de.traewelling.api.models.Data
import de.traewelling.api.models.auth.BearerToken
import de.traewelling.api.models.auth.LoginCredentials
import de.traewelling.api.models.station.StationData
import de.traewelling.api.models.status.Status
import de.traewelling.api.models.status.StatusPage
import de.traewelling.api.models.trip.HafasTripPage
import de.traewelling.api.models.user.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.concurrent.TimeUnit

private const val BASE_URL =
    "https://traewelling.de/api/v1/"

private val client = OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(Interceptor() { chain ->
    val newRequest =
        chain
            .request()
            .newBuilder()
            .addHeader("Authorization", "Bearer ${TraewellingApi.jwt}")
            .addHeader("User-Agent", "de.traewelling/1.0.0")
            .build()

    Log.d("Interceptor", "Interception JWT ${TraewellingApi.jwt}")
    Log.d("Interceptor", "Intercepting ${chain.request().url()}")

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
    @POST("auth/logout")
    fun logout(): Call<Unit>
    @GET("auth/user")
    fun getLoggedInUser(): Call<Data<User>>
}

interface CheckInService {
    @GET("dashboard")
    fun getPersonalDashboard(@Query("page") page: Int): Call<StatusPage>
    @GET("statuses")
    fun getStatuses(): Call<StatusPage>
}
interface TravelService {
    @GET("trains/station/nearby")
    fun getNearbyStation(@Query("latitude") latitude: Double, @Query("longitude") longitude: Double): Call<StationData>
    @GET("trains/station/{station}/departures")
    fun getDeparturesAtStation(@Path("station") station: String, @Query("when") time: Date): Call<HafasTripPage>
}

object TraewellingApi {
    var jwt: String = ""
    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    val checkInService: CheckInService by lazy {
        retrofit.create(CheckInService::class.java)
    }
    val travelService: TravelService by lazy {
        retrofit.create(TravelService::class.java)
    }
}