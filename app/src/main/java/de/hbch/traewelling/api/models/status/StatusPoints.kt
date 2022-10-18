package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName

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

enum class PointReason(val pointReason: Int) {
    @SerializedName("0")
    IN_TIME(0),
    @SerializedName("1")
    GOOD_ENOUGH(1),
    @SerializedName("2")
    NOT_SUFFICIENT(2),
    @SerializedName("3")
    FORCED(3)
}
