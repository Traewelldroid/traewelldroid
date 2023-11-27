package de.hbch.traewelling.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.adapters.ZonedDateTimeGsonConverter
import de.hbch.traewelling.adapters.ZonedDateTimeRetrofitConverterFactory
import de.hbch.traewelling.api.interceptors.AuthInterceptor
import de.hbch.traewelling.api.interceptors.ErrorInterceptor
import de.hbch.traewelling.api.interceptors.LogInterceptor
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.notifications.Notification
import de.hbch.traewelling.api.models.notifications.NotificationPage
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.statistics.DailyStatistics
import de.hbch.traewelling.api.models.statistics.PersonalStatistics
import de.hbch.traewelling.api.models.status.*
import de.hbch.traewelling.api.models.status.Tag
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import de.hbch.traewelling.api.models.trip.HafasTripPage
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.api.models.webhook.WebhookUserCreateRequest
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

const val TRWL_BASE_URL = "https://traewelling.de/api/v1/"

val HTTP_CLIENT = OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(LogInterceptor())
    .addInterceptor(ErrorInterceptor())
    .addInterceptor(AuthInterceptor())
    .build()

fun getGson(): Gson = GsonBuilder()
        .setExclusionStrategies(ExcludeAnnotationExclusionStrategy())
        .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeGsonConverter())
        .serializeNulls()
        .create()

val GSON: Gson = getGson()

private val trwlRetrofit =
    Retrofit.Builder()
        .addConverterFactory(ZonedDateTimeRetrofitConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .baseUrl(TRWL_BASE_URL)
        .client(HTTP_CLIENT)
        .build()

private val webhookRelayRetrofit =
    Retrofit.Builder()
        .baseUrl("${BuildConfig.WEBHOOK_URL}/api/")
        .client(HTTP_CLIENT)
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .build()

interface AuthService {
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

    @DELETE("webhooks/{id}")
    fun deleteWebhook(
        @Path("id") id: Int
    ): Call<Unit>
}

interface StatisticsService {
    @GET("statistics")
    fun getPersonalStatistics(
        @Query("from") from: LocalDate,
        @Query("until") until: LocalDate
    ): Call<Data<PersonalStatistics>>

    @GET("statistics/daily/{date}?withPolylines")
    fun getDailyStatistics(
        @Path("date") date: String
    ) : Call<Data<DailyStatistics>>
}

interface CheckInService {
    @GET("dashboard")
    fun getPersonalDashboard(
        @Query("page") page: Int
    ): Call<StatusPage>

    @GET("statuses")
    fun getStatuses(): Call<StatusPage>

    @GET("status/{id}")
    fun getStatusById(
        @Path("id") id: Int
    ): Call<Data<Status>>

    @GET("status/{id}/likes")
    fun getLikesForStatusById(
        @Path("id") id: Int
    ): Call<Data<List<User>>>

    @GET("status/{id}/tags")
    fun getTagsForStatusById(
        @Path("id") id: Int
    ): Call<Data<List<Tag>>>

    @POST("status/{id}/tags")
    fun createTagForStatus(
        @Path("id") id: Int,
        @Body tag: Tag
    ): Call<Data<Tag>>

    @PUT("status/{id}/tags/{key}")
    fun updateTagForStatus(
        @Path("id") id: Int,
        @Path("key") key: String,
        @Body tag: Tag
    ): Call<Data<Tag>>

    @DELETE("status/{id}/tags/{key}")
    fun deleteTagForStatus(
        @Path("id") id: Int,
        @Path("key") key: String
    ): Call<Any>

    @GET("polyline/{ids}")
    fun getPolylinesForStatuses(
        @Path("ids") statusIds: String
    ): Call<Data<FeatureCollection>>

    @GET("user/{username}/statuses")
    fun getStatusesForUser(
        @Path("username") username: String,
        @Query("page") page: Int
    ): Call<StatusPage>

    @DELETE("status/{statusId}")
    fun deleteStatus(
        @Path("statusId") statusId: Int
    ): Call<Any>

    @PUT("status/{statusId}")
    fun updateCheckIn(
        @Path("statusId") statusId: Int,
        @Body update: TrwlCheckInUpdateRequest
    ): Call<Data<Status>>

    @POST("status/{statusId}/like")
    fun createFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>

    @DELETE("status/{statusId}/like")
    fun deleteFavorite(
        @Path("statusId") statusId: Int
    ): Call<Unit>

    @GET("activeEvents")
    fun getActiveEvents(): Call<Data<List<Event>>>
}

interface TravelService {
    @GET("trains/trip")
    fun getTrip(
        @Query("hafasTripId") tripId: String,
        @Query("lineName") lineName: String,
        @Query("start") start: Int
    ): Call<Data<HafasTrainTrip>>

    @GET("trains/station/nearby")
    suspend fun getNearbyStation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Data<Station>

    @GET("trains/station/{station}/departures")
    fun getDeparturesAtStation(
        @Path("station", encoded = false) station: String,
        @Query("when") time: ZonedDateTime,
        @Query("travelType") filter: String
    ): Call<HafasTripPage>

    @GET("trains/station/autocomplete/{station}")
    suspend fun autoCompleteStationSearch(
        @Path("station", encoded = false) station: String
    ): Data<List<Station>>
}

interface NotificationService {
    @GET("notifications/unread/count")
    fun getUnreadNotificationsCount(): Call<Data<Int>>

    @GET("notifications")
    fun getNotifications(
        @Query("page") page: Int
    ): Call<NotificationPage>

    @PUT("notifications/read/{id}")
    fun markAsRead(
        @Path("id") id: String
    ): Call<Data<Notification>>

    @PUT("notifications/unread/{id}")
    fun markAsUnread(
        @Path("id") id: String
    ): Call<Data<Notification>>

    @PUT("notifications/read/all")
    fun markAllAsRead(): Call<Unit>
}

interface UserService {
    @GET("user/search/{query}")
    suspend fun searchUsers(
        @Path("query") query: String,
        @Query("page") page: Int
    ): Data<List<User>>

    @GET("user/{username}")
    fun getUser(
        @Path("username") username: String
    ): Call<Data<User>>

    @POST("user/{userId}/follow")
    fun followUser(
        @Path("userId") userId: Int
    ): Call<Data<Unit>>

    @DELETE("user/{userId}/follow")
    fun unfollowUser(
        @Path("userId") userId: Int
    ): Call<Data<Unit>>

    @POST("user/{id}/mute")
    fun muteUser(
        @Path("id") userId: Int
    ): Call<Data<Unit>>

    @DELETE("user/{id}/mute")
    fun unmuteUser(
        @Path("id") userId: Int
    ): Call<Data<Unit>>
}

interface WebhookRelayService {
    @POST("webhookUser")
    fun createWebhookUser(
        @Body webhookUser: WebhookUserCreateRequest
    ): Call<String>

    @DELETE("webhookUser/{id}")
    fun deleteWebhookUser(
        @Path("id") id: String
    ): Call<Unit>
}

object TraewellingApi {
    var jwt: String = ""

    val userService: UserService by lazy {
        trwlRetrofit.create(UserService::class.java)
    }
    val authService: AuthService by lazy {
        trwlRetrofit.create(AuthService::class.java)
    }
    val statisticsService: StatisticsService by lazy {
        trwlRetrofit.create(StatisticsService::class.java)
    }
    val checkInService: CheckInService by lazy {
        trwlRetrofit.create(CheckInService::class.java)
    }
    val travelService: TravelService by lazy {
        trwlRetrofit.create(TravelService::class.java)
    }
    val notificationService: NotificationService by lazy {
        trwlRetrofit.create(NotificationService::class.java)
    }
}

object WebhookRelayApi {
    val service: WebhookRelayService by lazy {
        webhookRelayRetrofit.create(WebhookRelayService::class.java)
    }
}
