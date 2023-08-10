package de.hbch.traewelling.api.models.event

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station
import java.time.ZonedDateTime

data class Event(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("hashtag") val hashtag: String,
    @SerializedName("host") val host: String,
    @SerializedName("url") val url: String,
    @SerializedName("begin") val begin: ZonedDateTime,
    @SerializedName("end") val end: ZonedDateTime,
    @SerializedName("station") val station: Station?
)
