package de.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName

data class HafasTripPage(
    @SerializedName("data") val data: List<HafasTrip>,
    @SerializedName("meta") val meta: HafasMeta
)
