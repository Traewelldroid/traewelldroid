package de.hbch.traewelling.api.models.station

import com.google.gson.annotations.SerializedName

data class StationData(
    @SerializedName("data") val data: Station
)

data class Station(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("ibnr") val ibnr: String,
    @SerializedName("rilIdentifier") val ds100: String?
)
