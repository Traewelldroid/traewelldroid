package de.hbch.traewelling.api.models.status

data class UpdateStatusRequest(
    val body: String?,
    val business: StatusBusiness,
    val visibility: StatusVisibility
)
