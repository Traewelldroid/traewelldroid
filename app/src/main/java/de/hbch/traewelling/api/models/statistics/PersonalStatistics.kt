package de.hbch.traewelling.api.models.statistics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.trip.ProductType
import java.util.*

data class PersonalStatistics(
    @SerializedName("categories") val categories: List<CategoryStatistics>,
    @SerializedName("operators") val operators: List<OperatorStatistics>,
    @SerializedName("purpose") val purposes: List<PurposeStatistics>
)

abstract class AbstractStatistics(
    @SerializedName("count") open val checkInCount: Int = 0,
    @SerializedName("duration") open val duration: Int = 0
) {
    @Composable
    abstract fun getLabel(): String
}

data class CategoryStatistics(
    @SerializedName("name") val productType: ProductType,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    @Composable
    override fun getLabel() = stringResource(productType.getString())
}

data class OperatorStatistics(
    @SerializedName("name") val operatorName: String?,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    @Composable
    override fun getLabel() = operatorName ?: stringResource(R.string.other_operators)
}

data class PurposeStatistics(
    @SerializedName("name") val businessType: StatusBusiness,
    @SerializedName("count") override val checkInCount: Int,
    @SerializedName("duration") override val duration: Int
) : AbstractStatistics() {
    @Composable
    override fun getLabel() = stringResource(businessType.getTitle())
}

data class TimeStatistics(
    @SerializedName("date") val date: Date,
    @SerializedName("count") val checkInCount: Int,
    @SerializedName("duration") val duration: Int
)