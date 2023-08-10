package de.hbch.traewelling.api.models.status

import java.time.ZonedDateTime

data class UpdateStatusRequest(
    val body: String?,
    val business: StatusBusiness,
    val visibility: StatusVisibility,
    val destinationId: Int? = null,
    val destinationArrivalPlanned: ZonedDateTime? = null
)
