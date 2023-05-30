package de.hbch.traewelling.api.dtos

import de.hbch.traewelling.api.models.trip.ProductType

data class Trip(
    val id: Int,
    val category: ProductType,
    val lineName: String,
    val origin: String,
    val destination: String,
    var stopovers: List<TripStation>
) {
}