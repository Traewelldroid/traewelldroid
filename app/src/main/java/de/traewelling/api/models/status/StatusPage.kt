package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.traewelling.api.models.meta.Links
import de.traewelling.api.models.meta.PaginationData

data class StatusPage(
    @SerializedName("data") val data: List<Status>,
    @SerializedName("links") val links: Links,
    @SerializedName("meta") val pagination: PaginationData
)
