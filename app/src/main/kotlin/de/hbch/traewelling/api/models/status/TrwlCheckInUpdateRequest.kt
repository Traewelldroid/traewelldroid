package de.hbch.traewelling.api.models.status

import de.hbch.traewelling.providers.checkin.CheckInUpdateRequest
import java.time.ZonedDateTime

data class TrwlCheckInUpdateRequest(
    val body: String?,
    val business: StatusBusiness,
    val visibility: StatusVisibility,
    val destinationId: Int? = null,
    val destinationArrivalPlanned: ZonedDateTime? = null,
    val manualDeparture: ZonedDateTime? = null,
    val manualArrival: ZonedDateTime? = null
): CheckInUpdateRequest()
