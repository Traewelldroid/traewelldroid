package de.hbch.traewelling.api.models.statistics

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.trip.ProductType
import java.util.*

data class PersonalStatistics(
    @SerializedName("categories") val categories: List<CategoryStatistics>,
    @SerializedName("operators") val operators: List<OperatorStatistics>,
    @SerializedName("purpose") val purposes: List<PurposeStatistics>
)

data class CategoryStatistics(
    @SerializedName("name") val productType: ProductType,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)

data class OperatorStatistics(
    @SerializedName("name") val operatorName: String?,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)

data class PurposeStatistics(
    @SerializedName("name") val businessType: StatusBusiness,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)

data class TimeStatistics(
    @SerializedName("date") val date: Date,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)