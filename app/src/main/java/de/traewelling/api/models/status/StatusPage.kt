package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

data class StatusPage(
    @SerializedName("first_page_url") val firstPageUrl: String,
    @SerializedName("next_page_url") val nextPageUrl: String,
    @SerializedName("path") val path: String,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("from") val from: Int,
    @SerializedName("to") val to: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val previousPageUrl: String,
    @SerializedName("data") val statuses: List<Status>
)