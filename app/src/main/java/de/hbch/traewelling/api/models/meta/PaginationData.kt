package de.hbch.traewelling.api.models.meta

import com.google.gson.annotations.SerializedName

data class PaginationData(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("from") val fromPage: Int,
    @SerializedName("path") val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("to") val toPage: Int
)