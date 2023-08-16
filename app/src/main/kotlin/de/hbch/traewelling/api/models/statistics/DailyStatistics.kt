package de.hbch.traewelling.api.models.statistics

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.status.Status

data class DailyStatistics(
    val statuses: List<Status>,
    @SerializedName("totalDistance") val distance: Int,
    @SerializedName("totalDuration") val duration: Int,
    @SerializedName("totalPoints") val points: Int
) {
    val count: Int get() = statuses.size
}
