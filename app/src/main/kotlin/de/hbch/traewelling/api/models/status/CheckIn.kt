package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.providers.checkin.CheckInRequest
import java.time.ZonedDateTime

data class TrwlCheckInRequest(
    @SerializedName("body") val body: String,
    @SerializedName("business") val business: StatusBusiness,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("eventId") val eventId: Int?,
    @SerializedName("toot") val sendToot: Boolean,
    @SerializedName("chainPost") val shouldChainToot: Boolean,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("start") val startStationId: Int,
    @SerializedName("destination") val destinationStationId: Int,
    @SerializedName("departure") val departureTime: ZonedDateTime,
    @SerializedName("arrival") val arrivalTime: ZonedDateTime,
    @SerializedName("force") val force: Boolean = false
): CheckInRequest()

data class TrwlCheckInResponse(
    @SerializedName("status") val status: Status,
    @SerializedName("alsoOnThisConnection") val coTravellers: List<Status>,
    @SerializedName("points") val points: StatusPoints
)
