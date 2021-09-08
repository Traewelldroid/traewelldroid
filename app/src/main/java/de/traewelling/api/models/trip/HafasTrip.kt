package de.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.traewelling.api.models.station.Station
import java.util.*

data class HafasTrip(
    @SerializedName("tripId") val tripId: String,
    @SerializedName("when") val departure: Date,
    @SerializedName("plannedWhen") val plannedDeparture: Date,
    @SerializedName("delay") val delay: Int,
    @SerializedName("platform") val platform: String,
    @SerializedName("plannedPlatform") val plannedPlatform: String,
    @SerializedName("direction") val destination: String,
    @SerializedName("line") val line: HafasLine,
    @SerializedName("station") val station: Station
)
