package de.hbch.traewelling.api.models.polyline

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("type") val type: String?,
    @SerializedName("coordinates") val coordinates: List<List<Double>>?
)

data class Properties(
    @SerializedName("statusId") val statusId: Int
)

data class Feature(
    @SerializedName("geometry") val geometry: Geometry?,
    @SerializedName("properties") val properties: Properties?,
    @SerializedName("type") val type: String?
)

data class FeatureCollection(
    @SerializedName("features") val features: List<Feature>?,
    @SerializedName("type") val type: String?
)
