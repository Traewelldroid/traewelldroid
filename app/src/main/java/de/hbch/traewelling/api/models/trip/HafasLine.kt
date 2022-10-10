package de.hbch.traewelling.api.models.trip

import android.content.res.Resources
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val travelId: String,
    @SerializedName("name") val name: String,
    @SerializedName("product") val product: ProductType,
    @SerializedName("operator") val operator: HafasOperator
)

enum class ProductType {
    @SerializedName("all")
    ALL,
    @SerializedName("ferry")
    FERRY,
    @SerializedName("bus")
    BUS,
    @SerializedName("suburban")
    SUBURBAN,
    @SerializedName("subway")
    SUBWAY,
    @SerializedName("tram")
    TRAM,
    // RE, RB, RS
    @SerializedName("regional")
    REGIONAL,
    // IRE, IR
    @SerializedName("regionalExp")
    REGIONAL_EXPRESS,
    // ICE, ECE
    @SerializedName("nationalExpress")
    NATIONAL_EXPRESS,
    // IC, EC
    @SerializedName("national")
    NATIONAL,
    LONG_DISTANCE;

    companion object {
        fun toString(resources: Resources, productType: ProductType): String {
            return resources.getString(when (productType) {
                BUS -> R.string.product_type_bus
                FERRY -> R.string.product_type_ferry
                SUBURBAN -> R.string.product_type_suburban
                SUBWAY -> R.string.product_type_subway
                TRAM -> R.string.product_type_tram
                REGIONAL -> R.string.product_type_regional
                REGIONAL_EXPRESS -> R.string.product_type_regional_express
                NATIONAL_EXPRESS -> R.string.product_type_national_express
                NATIONAL -> R.string.product_type_national
                else -> R.string.product_type_bus
            })
        }
    }
}