package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.api.models.trip.ProductType
import java.time.ZonedDateTime

data class Journey(
    @SerializedName("trip") val tripId: Int,
    @SerializedName("hafasId") val hafasTripId: String,
    @SerializedName("category") val category: ProductType,
    @SerializedName("number") val number: String,
    @SerializedName("lineName") val line: String,
    @SerializedName("journeyNumber") val journeyNumber: Int?,
    @SerializedName("distance") val distance: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("delay") val delay: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("speed") val averageSpeed: Double,
    @SerializedName("origin") val origin: HafasTrainTripStation,
    @SerializedName("destination") val destination: HafasTrainTripStation,
    @SerializedName("manualDeparture") val departureManual: ZonedDateTime?,
    @SerializedName("manualArrival") val arrivalManual: ZonedDateTime?
)
