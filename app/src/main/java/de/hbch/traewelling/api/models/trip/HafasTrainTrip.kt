package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station

data class HafasTrainTrip(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: ProductType,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("journeyNumber") val journeyNumber: String,
    @SerializedName("origin") val origin: Station,
    @SerializedName("destination") val destination: Station,
    @SerializedName("stopovers") val stopovers: List<HafasTrainTripStation>
)
