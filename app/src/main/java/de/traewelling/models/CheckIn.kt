package de.traewelling.models

class CheckIn(
    val start: String,
    val end: String,
    val trainLine: String,
    val distance: String,
    val travelTime: String,
    val departureTime: String,
    val nextStation: String,
    val destinationTime: String,
    val username: String,
    val checkInTime: String,
    val faved: Boolean
) {
}