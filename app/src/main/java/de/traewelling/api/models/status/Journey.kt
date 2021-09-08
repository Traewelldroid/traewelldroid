package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

data class Journey(
    @SerializedName("trip") val tripId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("number") val number: String,
    @SerializedName("lineName") val line: String,
    @SerializedName("distance") val distance: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("delay") val delay: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("speed") val averageSpeed: Double,
    @SerializedName("origin") val origin: JourneyStation,
    @SerializedName("destination") val destination: JourneyStation
)
