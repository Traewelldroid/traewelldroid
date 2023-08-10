package de.hbch.traewelling.api.dtos

import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.ProductType
import java.time.ZonedDateTime

data class Status(
    val statusId: Int,
    val origin: String,
    val originId: Int,
    val departurePlanned: ZonedDateTime,
    val departureReal: ZonedDateTime?,
    val departureManual: ZonedDateTime?,
    val destination: String,
    val destinationId: Int,
    val arrivalPlanned: ZonedDateTime,
    val arrivalReal: ZonedDateTime?,
    val arrivalManual: ZonedDateTime?,
    val productType: ProductType,
    val hafasTripId: String,
    val line: String,
    val journeyNumber: Int?,
    val distance: Int,
    val duration: Int,
    val business: StatusBusiness,
    val message: String,
    val liked: Boolean?,
    val likeCount: Int?,
    val userId: Int,
    val username: String,
    val createdAt: ZonedDateTime,
    val visibility: StatusVisibility,
    val eventName: String?
)
