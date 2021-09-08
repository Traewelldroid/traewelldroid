package de.traewelling.api.models.meta

import com.google.gson.annotations.SerializedName
import java.util.*

data class Times(
    @SerializedName("now") val now: Date,
    @SerializedName("prev") val previous: Date,
    @SerializedName("next") val next: Date
)
