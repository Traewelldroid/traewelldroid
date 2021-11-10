package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val travelId: String,
    @SerializedName("name") val name: String,
    @SerializedName("product") val product: ProductType,
    @SerializedName("operator") val operator: HafasOperator
)

enum class ProductType {
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
    LONG_DISTANCE
}