package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.meta.Times
import de.hbch.traewelling.api.models.station.Station

data class HafasMeta(
    @SerializedName("times") val times: Times,
    @SerializedName("station") val station: Station
)
