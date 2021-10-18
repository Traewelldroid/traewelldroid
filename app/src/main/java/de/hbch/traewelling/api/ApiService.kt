package de.hbch.traewelling.api

import android.util.Log
import com.google.gson.GsonBuilder
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.auth.BearerToken
import de.hbch.traewelling.api.models.auth.LoginCredentials
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.station.StationData
import de.hbch.traewelling.api.models.status.CheckInRequest
import de.hbch.traewelling.api.models.status.CheckInResponse
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import de.hbch.traewelling.api.models.trip.HafasTripPage
import de.hbch.traewelling.api.models.user.User
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
            .addHeader("User-Agent", "de.hbch.traewelling/1.0.0")
            .build()

    chain.proceed(newRequest)
}).build()

private val retrofit =
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

interface AuthService {
    @POST("auth/login")
    fun login(
        @Body credentials: LoginCredentials
    ): Call<BearerToken>

    @POST("auth/logout")
    fun logout(): Call<Unit>

    @GET("auth/user")
    fun getLoggedInUser(): Call<Data<User>>
}

interface CheckInService {
    @GET("dashboard")
    fun getPersonalDashboard(
        @Query("page") page: Int
    ): Call<StatusPage>

    @GET("statuses")
    fun getStatuses(): Call<StatusPage>

    @POST("trains/checkin")
    fun checkIn(
        @Body checkIn: CheckInRequest
    ): Call<Data<CheckInResponse>>

    @POST("like/{statusId}")
    fun createFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>

    @DELETE("like/{statusId}")
    fun deleteFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>
}

interface TravelService {
    @GET("trains/trip")
    fun getTrip(
        @Query("tripID") tripId: String,
        @Query("lineName") lineName: String,
        @Query("start") start: Int
    ): Call<Data<HafasTrainTrip>>

    @GET("trains/station/nearby")
    fun getNearbyStation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<StationData>

    @GET("trains/station/{station}/departures")
    fun getDeparturesAtStation(
        @Path("station") station: String,
        @Query("when") time: Date
    ): Call<HafasTripPage>

    @GET("trains/station/autocomplete/{station}")
    fun autoCompleteStationSearch(
        @Path("station") station: String
    ): Call<Data<List<Station>>>
}

object TraewellingApi {
    var jwt: String = ""
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val checkInService: CheckInService by lazy {
        retrofit.create(CheckInService::class.java)
    }
    val travelService: TravelService by lazy {
        retrofit.create(TravelService::class.java)
    }
}