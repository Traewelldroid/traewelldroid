package de.hbch.traewelling.providers.checkin.travelynx.models

import de.hbch.traewelling.providers.checkin.CheckInRequest

data class TravelynxCheckInRequest(
    val token: String,
    val journeyId: String,
    val origin: String,
    val destination: String,
    val message: String
): CheckInRequest()
