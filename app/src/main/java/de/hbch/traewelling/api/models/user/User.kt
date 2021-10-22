package de.hbch.traewelling.api.models.user

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.StatusVisibility

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("trainDistance") val distance: Int,
    @SerializedName("trainDuration") val duration: Int,
    @SerializedName("trainSpeed") val averageSpeed: Double,
    @SerializedName("points") val points: Int,
    @SerializedName("twitterUrl") val twitterUrl: String,
    @SerializedName("mastodonUrl") val mastodonUrl: String,
    @SerializedName("privateProfile") val privateProfile: Boolean,
    @SerializedName("home") val home: Station?,
    @SerializedName("prevent_index") val preventIndex: Boolean,
    @SerializedName("language") val language: String
)

data class ForeignUser(
    @SerializedName("id") val id: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("username") val username: String,
)