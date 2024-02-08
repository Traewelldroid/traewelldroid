package de.hbch.traewelling.api.models.statistics

import android.content.Context
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.Exclude
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.trip.ProductType
import java.time.ZonedDateTime

data class PersonalStatistics(
    @SerializedName("categories") val categories: List<CategoryStatistics>,
    @SerializedName("operators") val operators: List<OperatorStatistics>,
    @SerializedName("purpose") val purposes: List<PurposeStatistics>
)

abstract class AbstractStatistics(
    @Exclude open val checkInCount: Int = 0,
    @Exclude open val duration: Int = 0
) {
    abstract fun getLabel(context: Context): String
}

data class CategoryStatistics(
    @SerializedName("name") val productType: ProductType,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    override fun getLabel(context: Context) = context.getString(productType.getString())
}

data class OperatorStatistics(
    @SerializedName("name") val operatorName: String?,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    override fun getLabel(context: Context) = operatorName ?: context.getString(R.string.other_operators)
}

data class PurposeStatistics(
    @SerializedName("name") val businessType: StatusBusiness,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    override fun getLabel(context: Context) = context.getString(businessType.title)
}

@Suppress("unused")
data class TimeStatistics(
    @SerializedName("date") val date: ZonedDateTime,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)
