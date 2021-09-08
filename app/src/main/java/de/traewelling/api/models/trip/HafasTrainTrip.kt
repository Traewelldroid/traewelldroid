package de.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.traewelling.api.models.station.Station

data class HafasTrainTrip(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("origin") val origin: Station,
    @SerializedName("destination") val destination: Station,
    @SerializedName("stopovers") val stopovers: List<HafasTrainTripStation>
)
