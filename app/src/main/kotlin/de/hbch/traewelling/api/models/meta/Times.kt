package de.hbch.traewelling.api.models.meta

import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime

data class Times(
    @SerializedName("now") val now: ZonedDateTime?,
    @SerializedName("prev") val previous: ZonedDateTime?,
    @SerializedName("next") val next: ZonedDateTime?
)
