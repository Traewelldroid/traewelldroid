package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName

data class HafasOperator(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
