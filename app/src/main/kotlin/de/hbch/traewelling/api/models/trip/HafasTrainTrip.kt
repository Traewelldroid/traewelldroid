package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station

data class HafasTrainTrip(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: ProductType,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("origin") val origin: Station,
    @SerializedName("destination") val destination: Station,
    @SerializedName("stopovers") var stopovers: List<HafasTrainTripStation>,
    @SerializedName("number") val lineId: String
)
