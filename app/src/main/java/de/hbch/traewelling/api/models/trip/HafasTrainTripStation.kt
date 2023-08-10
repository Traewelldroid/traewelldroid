package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.dtos.TripStation
import java.time.ZonedDateTime

data class HafasTrainTripStation(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("rilIdentifier") val ds100: String?,
    @SerializedName("arrival") val arrival: ZonedDateTime?,
    @SerializedName("arrivalPlanned") val arrivalPlanned: ZonedDateTime,
    @SerializedName("arrivalReal") val arrivalReal: ZonedDateTime?,
    @SerializedName("arrivalPlatformPlanned") val arrivalPlatformPlanned: String?,
    @SerializedName("arrivalPlatformReal") val arrivalPlatformReal: String?,
    @SerializedName("departure") val departure: ZonedDateTime?,
    @SerializedName("departurePlanned") val departurePlanned: ZonedDateTime,
    @SerializedName("departureReal") val departureReal: ZonedDateTime?,
    @SerializedName("departurePlatformPlanned") val departurePlatformPlanned: String,
    @SerializedName("departurePlatformReal") val departurePlatformReal: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("isArrivalDelayed") val isArrivalDelayed: Boolean,
    @SerializedName("isDepartureDelayed") val isDepartureDelayed: Boolean,
    @SerializedName("cancelled") val cancelled: Boolean
) {
    val isCancelled: Boolean
        // DB Rest also shows last stop as cancelled
        get() = cancelled && arrivalPlatformPlanned == null

    fun toTripStation(): TripStation {
        return TripStation(
            id,
            name,
            ds100,
            departurePlanned,
            departureReal,
            arrivalPlanned,
            arrivalReal,
            isCancelled
        )
    }
}
