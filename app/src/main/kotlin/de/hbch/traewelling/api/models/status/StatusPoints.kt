package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class StatusPoints(
    @SerializedName("points") val points: Int,
    @SerializedName("calculation") val calculation: StatusPointCalculation
)

data class StatusPointCalculation(
    @SerializedName("base") val base: Float,
    @SerializedName("distance") val distance: Float,
    @SerializedName("factor") val factor: Float,
    @SerializedName("reason") val reason: PointReason
)

@Suppress("unused")
enum class PointReason {
    @SerializedName("0")
    IN_TIME {
        override fun getExplanation() = null
    },
    @SerializedName("1")
    GOOD_ENOUGH {
        override fun getExplanation() = R.string.point_reason_good_enough
    },
    @SerializedName("2")
    NOT_SUFFICIENT {
        override fun getExplanation() = R.string.point_reason_not_sufficient
    },
    @SerializedName("3")
    FORCED {
        override fun getExplanation() = R.string.point_reason_forced
    };

    abstract fun getExplanation(): Int?
}
