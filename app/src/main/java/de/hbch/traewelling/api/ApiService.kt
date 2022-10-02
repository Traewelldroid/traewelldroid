package de.hbch.traewelling.api

import com.google.gson.GsonBuilder
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.auth.BearerToken
import de.hbch.traewelling.api.models.auth.LoginCredentials
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.station.StationData
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.api.models.status.*
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
                .addHeader(
                    "User-Agent",
                    "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
                )
                .build()

        chain.proceed(newRequest)
    }).build()

private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssX").create()

private val retrofit =
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .client(client)
        .build()

interface AuthService {
    @POST("auth/login")
    fun login(
        @Body credentials: LoginCredentials
    ): Call<Data<BearerToken>>

    @POST("auth/refresh")
    fun refreshToken(): Call<Data<BearerToken>>

    @POST("auth/logout")
    fun logout(): Call<Unit>

    @GET("auth/user")
    fun getLoggedInUser(): Call<Data<User>>

    @PUT("trains/station/{stationName}/home")
    fun setUserHomelandStation(
        @Path("stationName") stationName: String
    ): Call<Data<Station>>

    @GET("trains/station/history")
    fun getLastVisitedStations(): Call<Data<List<Station>>>
}

interface StatisticsService {
    @GET("statistics")
    fun getPersonalStatistics(
        @Query("from") from: Date,
        @Query("until") until: Date
    ): Call<Data<PersonalStatistics>>
}

interface CheckInService {
    @GET("dashboard")
    fun getPersonalDashboard(
        @Query("page") page: Int
    ): Call<StatusPage>

    @GET("statuses")
    fun getStatuses(): Call<StatusPage>

    @GET("statuses/{id}")
    fun getStatusById(
        @Path("id") id: Int
    ): Call<Data<Status>>

    @DELETE("statuses/{id}")
    fun deleteStatusById(
        @Path("id") id: Int
    ): Call<Void>

    @GET("polyline/{ids}")
    fun getPolylinesForStatuses(
        @Path("ids") statusIds: String
    ): Call<Data<FeatureCollection>>

    @GET("user/{username}/statuses")
    fun getStatusesForUser(
        @Path("username") username: String,
        @Query("page") page: Int
    ): Call<StatusPage>

    @DELETE("statuses/{statusId}")
    fun deleteStatus(
        @Path("statusId") statusId: Int
    ): Call<Any>

    @POST("trains/checkin")
    fun checkIn(
        @Body checkIn: CheckInRequest
    ): Call<Data<CheckInResponse>>

    @PUT("statuses/{statusId}")
    fun updateCheckIn(
        @Path("statusId") statusId: Int,
        @Body update: UpdateStatusRequest
    ): Call<Data<Status>>

    @POST("like/{statusId}")
    fun createFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>

    @DELETE("like/{statusId}")
    fun deleteFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>

    @GET("activeEvents")
    fun getActiveEvents(): Call<Data<List<Event>>>
}

interface TravelService {
    @GET("trains/trip")
    fun getTrip(
        @Query("tripID") tripId: String,
        @Query("tripId") tripId2: String,
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
        @Path("station", encoded = false) station: String,
        @Query("when") time: Date
    ): Call<HafasTripPage>

    @GET("trains/station/autocomplete/{station}")
    fun autoCompleteStationSearch(
        @Path("station", encoded = false) station: String
    ): Call<Data<List<Station>>>
}

interface NotificationService {
    @GET("notifications/count")
    fun getUnreadNotificationsCount(): Call<Data<Int>>
}

interface UserService {
    @GET("user/{username}")
    fun getUser(
        @Path("username") username: String
    ): Call<Data<User>>
}

object TraewellingApi {
    var jwt: String = ""
    var appVersion: String = ""
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val statisticsService: StatisticsService by lazy {
        retrofit.create(StatisticsService::class.java)
    }
    val checkInService: CheckInService by lazy {
        retrofit.create(CheckInService::class.java)
    }
    val travelService: TravelService by lazy {
        retrofit.create(TravelService::class.java)
    }
    val notificationService: NotificationService by lazy {
        retrofit.create(NotificationService::class.java)
    }
}