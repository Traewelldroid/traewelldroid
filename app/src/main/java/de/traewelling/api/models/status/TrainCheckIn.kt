package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

data class TrainCheckIn(
    @SerializedName("id") val id: Int,
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("origin") val origin: Station,
    @SerializedName("destination") val destination: Station,
    @SerializedName("distance") val distance: Int,
    @SerializedName("departure") val departureTime: String,
    @SerializedName("arrival") val arrivalTime: String,
    @SerializedName("points") val points: Int,
    @SerializedName("delay") val delay: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("hafas_trip") val hafasTrip: HafasTrip
)