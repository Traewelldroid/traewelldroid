package de.traewelling.api.models.meta

import com.google.gson.annotations.SerializedName

data class Links(
    @SerializedName("first") val first: String,
    @SerializedName("last") val last: String,
    @SerializedName("prev") val previous: String,
    @SerializedName("next") val next: String,
)