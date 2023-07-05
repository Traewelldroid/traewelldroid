package de.hbch.traewelling.api.dtos

import java.util.Date

data class TripStation(
    val id: Int,
    val name: String,
    val rilIdentifier: String?,
    val departurePlanned: Date,
    val departureReal: Date?,
    val arrivalPlanned: Date,
    val arrivalReal: Date?,
    val isCancelled: Boolean
)
