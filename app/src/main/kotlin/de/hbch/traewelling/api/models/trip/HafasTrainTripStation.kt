package de.hbch.traewelling.api.models.trip

import de.hbch.traewelling.api.dtos.TripStation
import java.time.ZonedDateTime

data class HafasTrainTripStation(
    val id: Int,
    val name: String,
    val rilIdentifier: String?,
    val evaIdentifier: Int,
    val arrival: ZonedDateTime?,
    val arrivalPlanned: ZonedDateTime,
    val arrivalReal: ZonedDateTime?,
    val arrivalPlatformPlanned: String?,
    val arrivalPlatformReal: String?,
    val departure: ZonedDateTime?,
    val departurePlanned: ZonedDateTime,
    val departureReal: ZonedDateTime?,
    val departurePlatformPlanned: String?,
    val departurePlatformReal: String?,
    val platform: String?,
    val cancelled: Boolean
) {
    val isCancelled: Boolean
        // DB Rest also shows last stop as cancelled
        get() = cancelled && arrivalPlatformPlanned == null

    fun toTripStation(): TripStation {
        return TripStation(
            id,
            name,
            rilIdentifier,
            departurePlanned,
            departureReal,
            arrivalPlanned,
            arrivalReal,
            isCancelled
        )
    }
}
