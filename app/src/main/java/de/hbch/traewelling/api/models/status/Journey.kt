package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.api.models.trip.ProductType

data class Journey(
    @SerializedName("trip") val tripId: Int,
    @SerializedName("category") val category: ProductType,
    @SerializedName("number") val number: String,
    @SerializedName("lineName") val line: String,
    @SerializedName("distance") val distance: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("delay") val delay: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("speed") val averageSpeed: Double,
    @SerializedName("origin") val origin: HafasTrainTripStation,
    @SerializedName("destination") val destination: HafasTrainTripStation
)
