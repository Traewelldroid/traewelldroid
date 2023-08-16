package de.hbch.traewelling.api.dtos

import java.time.ZonedDateTime


data class TripStation(
    val id: Int,
    val name: String,
    val rilIdentifier: String?,
    val departurePlanned: ZonedDateTime,
    val departureReal: ZonedDateTime?,
    val arrivalPlanned: ZonedDateTime,
    val arrivalReal: ZonedDateTime?,
    val isCancelled: Boolean
)
