package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val travelId: String,
    @SerializedName("name") val name: String,
    @SerializedName("product") val product: ProductType,
    @SerializedName("operator") val operator: HafasOperator?
)

@Suppress("unused")
enum class ProductType {
    @SerializedName("all")
    ALL,
    @SerializedName("ferry")
    FERRY {
        override fun getIcon() = R.drawable.ic_ferry
        override fun getString() = R.string.product_type_ferry
    },
    @SerializedName("bus")
    BUS {
        override fun getIcon() = R.drawable.ic_bus
        override fun getString() = R.string.product_type_bus
    },
    @SerializedName("suburban")
    SUBURBAN {
        override val isTrain = true
        override fun getIcon() = R.drawable.ic_suburban
        override fun getString() = R.string.product_type_suburban
    },
    @SerializedName("subway")
    SUBWAY {
        override fun getIcon() = R.drawable.ic_subway
        override fun getString() = R.string.product_type_subway
    },
    @SerializedName("tram")
    TRAM {
        override fun getIcon() = R.drawable.ic_tram
        override fun getString() = R.string.product_type_tram
    },
    // RE, RB, RS
    @SerializedName("regional")
    REGIONAL {
        override val isTrain = true
        override fun getString() = R.string.product_type_regional
    },
    // IRE, IR
    @SerializedName("regionalExp")
    REGIONAL_EXPRESS {
        override val isTrain = true
        override fun getString() = R.string.product_type_regional_express
    },
    // ICE, ECE
    @SerializedName("nationalExpress")
    NATIONAL_EXPRESS {
        override val isTrain = true
        override fun getString() = R.string.product_type_national_express
    },
    // IC, EC
    @SerializedName("national")
    NATIONAL {
        override val isTrain = true
        override fun getString() = R.string.product_type_national
    },
    LONG_DISTANCE {
        override val isTrain = true
        override fun getString() = R.string.product_type_national_express
    };

    open val isTrain = false
    open fun getIcon() = R.drawable.ic_train
    open fun getString() = R.string.product_type_bus
}