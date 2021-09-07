package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

data class HafasTrip(
    @SerializedName("id") val id: Int,
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("category") val category: String,
    @SerializedName("number") val trainNumber: String,
    @SerializedName("linename") val lineName: String,
    @SerializedName("delay") val delay: Int
)
