package de.hbch.traewelling.providers.checkin.traewelling

import de.hbch.traewelling.adapters.ZonedDateTimeRetrofitConverterFactory
import de.hbch.traewelling.api.GSON
import de.hbch.traewelling.api.TRWL_BASE_URL
import de.hbch.traewelling.api.interceptors.AuthInterceptor
import de.hbch.traewelling.api.interceptors.ErrorInterceptor
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.TrwlCheckInRequest
import de.hbch.traewelling.api.models.status.TrwlCheckInResponse
import de.hbch.traewelling.api.models.status.TrwlCheckInUpdateRequest
import de.hbch.traewelling.providers.checkin.CheckInProvider
import de.hbch.traewelling.providers.checkin.CheckInRequest
import de.hbch.traewelling.providers.checkin.CheckInResponse
import de.hbch.traewelling.providers.checkin.CheckInResult
import de.hbch.traewelling.providers.checkin.CheckInUpdateRequest
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class TrwlCheckInProvider: CheckInProvider<TrwlCheckInResponse>() {
    private interface CheckInService {
        @POST("trains/checkin")
        suspend fun checkIn(
            @Body checkIn: TrwlCheckInRequest
        ): Response<Data<TrwlCheckInResponse>>

        @PUT("status/{statusId}")
        suspend fun update(
            @Path("statusId") statusId: Int,
            @Body updateRequest: TrwlCheckInUpdateRequest
        ): Response<Data<Status>>
    }

    override val client = httpClientBuilder
        .addInterceptor(ErrorInterceptor())
        .addInterceptor(AuthInterceptor())
        .build()
    override val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(ZonedDateTimeRetrofitConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .baseUrl(TRWL_BASE_URL)
        .client(client)
        .build()
    private val service: CheckInService by lazy {
        retrofit.create(CheckInService::class.java)
    }

    override suspend fun checkIn(request: CheckInRequest): CheckInResponse<TrwlCheckInResponse> {
        if (request is TrwlCheckInRequest) {
            try {
                val response = service.checkIn(request)
                val data = response.body()?.data
                return if (response.isSuccessful && data != null) {
                    CheckInResponse(
                        data,
                        CheckInResult.SUCCESSFUL
                    )
                } else if (response.code() == 409) {
                    CheckInResponse(
                        null,
                        CheckInResult.CONFLICTED
                    )
                } else {
                    return CheckInResponse(
                        null,
                        CheckInResult.ERROR
                    )
                }
            } catch (exception: Exception) {
                return CheckInResponse(
                    null,
                    CheckInResult.ERROR
                )
            }
        }
        error("WTF u doin?")
    }

    override suspend fun update(request: CheckInUpdateRequest): CheckInResponse<TrwlCheckInResponse> {
        if (request is TrwlCheckInUpdateRequest) {
            try {
                val response = service.update(0, request)
                val data = response.body()?.data
                return if (response.isSuccessful && data != null) {
                    CheckInResponse(
                        null,
                        CheckInResult.SUCCESSFUL
                    )
                } else {
                    CheckInResponse(
                        null,
                        CheckInResult.SUCCESSFUL
                    )
                }
            } catch (_: Exception) {
                return CheckInResponse(
                    null,
                    CheckInResult.ERROR
                )
            }
        } else {
            error("WTF u doin?")
        }
    }
}
