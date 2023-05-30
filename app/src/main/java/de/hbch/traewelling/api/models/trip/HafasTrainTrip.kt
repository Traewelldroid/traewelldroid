package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.api.models.station.Station

data class HafasTrainTrip(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: ProductType,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("origin") val origin: Station,
    @SerializedName("destination") val destination: Station,
    @SerializedName("stopovers") val stopovers: List<HafasTrainTripStation>
) {
    fun toTrip(): Trip {
        val tripStopovers = mutableListOf<TripStation>()

        stopovers.forEach { stopover ->
            tripStopovers.add(stopover.toTripStation())
        }

        return Trip(
            id,
            category,
            lineName,
            origin.name,
            destination.name,
            tripStopovers
        )
    }
}
