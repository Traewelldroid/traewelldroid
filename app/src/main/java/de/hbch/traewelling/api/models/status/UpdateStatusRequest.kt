package de.hbch.traewelling.api.models.status

import java.util.*

data class UpdateStatusRequest(
    val body: String?,
    val business: StatusBusiness,
    val visibility: StatusVisibility,
    val destinationId: Int? = null,
    val destinationArrivalPlanned: Date? = null
)
