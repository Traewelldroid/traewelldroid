package de.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val travelId: String,
    @SerializedName("name") val name: String,
    @SerializedName("product") val product: String,
    @SerializedName("operator") val operator: HafasOperator
)
