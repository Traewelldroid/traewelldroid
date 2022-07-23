package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station
import java.util.*

data class HafasTrip(
    @SerializedName("tripId") val tripId: String,
    @SerializedName("when") val departure: Date?,
    @SerializedName("plannedWhen") val plannedDeparture: Date?,
    @SerializedName("delay") val delay: Int?,
    @SerializedName("platform") val platform: String?,
    @SerializedName("plannedPlatform") val plannedPlatform: String?,
    @SerializedName("direction") val direction: String?,
    @SerializedName("line") val line: HafasLine?,
    @SerializedName("station") val station: Station?,
    @SerializedName("cancelled") val isCancelled: Boolean,
    @SerializedName("destination") val destination: HafasStation?
) {
    val finalDestination get() = direction ?: (destination?.name ?: "")
}

data class HafasStation(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)