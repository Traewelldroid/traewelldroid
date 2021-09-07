package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("id") val id: Int,
    @SerializedName("ibnr") val ibnr: Int,
    @SerializedName("rilIdentifier") val rilIdentifier: String,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
