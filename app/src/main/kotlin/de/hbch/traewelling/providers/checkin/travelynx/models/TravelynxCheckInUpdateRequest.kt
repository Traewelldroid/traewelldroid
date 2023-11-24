package de.hbch.traewelling.providers.checkin.travelynx.models

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.providers.checkin.CheckInUpdateRequest

data class TravelynxCheckInUpdateRequest(
    val token: String,
    val action: String,
    val force: Boolean,
    @SerializedName("toStation") val destination: String,
    @SerializedName("comment") val message: String
): CheckInUpdateRequest()
