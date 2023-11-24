package de.hbch.traewelling.providers.checkin.travelynx

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.GSON
import de.hbch.traewelling.providers.checkin.CheckInProvider
import de.hbch.traewelling.providers.checkin.CheckInRequest
import de.hbch.traewelling.providers.checkin.CheckInResponse
import de.hbch.traewelling.providers.checkin.CheckInResult
import de.hbch.traewelling.providers.checkin.CheckInUpdateRequest
import de.hbch.traewelling.providers.checkin.travelynx.models.TravelynxCheckInRequest
import de.hbch.traewelling.providers.checkin.travelynx.models.TravelynxCheckInUpdateRequest
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class TravelynxCheckInProvider: CheckInProvider<Unit>() {

    private interface CheckInService {
        @POST("travel")
        suspend fun checkIn(
            @Body checkInData: CheckInData
        ): Response<CheckInState>
        @POST("travel")
        suspend fun update(
            @Body updateRequest: TravelynxCheckInUpdateRequest
        ): Response<Unit>
    }

    private data class CheckInDataTrain(
        @SerializedName("journeyID") val journeyId: String
    )

    private data class CheckInData(
        val token: String,
        val action: String,
        @SerializedName("train") val journey: CheckInDataTrain,
        @SerializedName("fromStation") val origin: String,
        @SerializedName("toStation") val destination: String,
        @SerializedName("comment") val message: String
    )

    private data class CheckInState(
        val success: Boolean
    )

    override val client = httpClientBuilder.build()

    override val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .baseUrl("https://travelynx.de/api/v1/")
        .client(client)
        .build()

    private val service: CheckInService by lazy {
        retrofit.create(CheckInService::class.java)
    }

    override suspend fun checkIn(request: CheckInRequest): CheckInResponse<Unit> {
        if (request is TravelynxCheckInRequest) {
            val checkInData = CheckInData(
                token = request.token,
                action = "checkin",
                journey = CheckInDataTrain(
                    journeyId = request.journeyId
                ),
                origin = request.origin,
                destination = request.destination,
                message = request.message
            )

            try {
                val response = service.checkIn(checkInData)

                return if (response.isSuccessful && response.body()?.success == true) {
                    CheckInResponse(
                        null,
                        CheckInResult.SUCCESSFUL
                    )
                } else {
                    CheckInResponse(
                        null,
                        CheckInResult.ERROR
                    )
                }
            } catch (_: Exception) {
                return CheckInResponse(
                    null,
                    CheckInResult.ERROR
                )
            }
        }
        error("WTF u doin")
    }

    override suspend fun update(request: CheckInUpdateRequest): CheckInResponse<Unit> {
        if (request is TravelynxCheckInUpdateRequest) {
            try {
                val response = service.update(request)

                return if (response.isSuccessful) {
                    CheckInResponse(
                        null,
                        CheckInResult.SUCCESSFUL
                    )
                } else {
                    CheckInResponse(
                        null,
                        CheckInResult.ERROR
                    )
                }
            } catch (_: Exception) {
                return CheckInResponse(
                    null,
                    CheckInResult.ERROR
                )
            }
        }
        error("WTF u doin")
    }
}
