package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.user.User
import java.util.*

data class CheckInRequest(
    @SerializedName("body") val body: String,
    @SerializedName("business") val business: StatusBusiness,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("eventId") val eventId: Int?,
    @SerializedName("tweet") val sendTweet: Boolean,
    @SerializedName("toot") val sendToot: Boolean,
    @SerializedName("tripId") val tripId: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("start") val startStationId: Int,
    @SerializedName("destination") val destinationStationId: Int,
    @SerializedName("departure") val departureTime: Date,
    @SerializedName("arrival") val arrivalTime: Date
)

data class CheckInResponse(
    @SerializedName("status") val status: Status,
    @SerializedName("alsoOnThisConnection") val coTravellers: List<Status>,
    @SerializedName("points") val points: StatusPoints
)