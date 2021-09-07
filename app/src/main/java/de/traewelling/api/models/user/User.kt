package de.traewelling.api.models.user

import com.google.gson.annotations.SerializedName
import de.traewelling.api.models.status.StatusVisibility

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("private_profile") val privateProfile: Boolean,
    @SerializedName("default_status_visibility") val defaultStatusVisibility: StatusVisibility,
    @SerializedName("prevent_index") val preventIndex: Boolean,
    @SerializedName("language") val language: String,
    @SerializedName("last_login") val lastLogin: String,
    @SerializedName("averageSpeed") val averageSpeed: Double,
    @SerializedName("points") val points: Int,
    @SerializedName("twitterUrl") val twitterUrl: String,
    @SerializedName("mastodonUrl") val mastodonUrl: String,
    @SerializedName("train_distance") val travelledDistance: Int,
    @SerializedName("train_duration") val travelledTime: Int,
    @SerializedName("following") val following: Boolean,
    @SerializedName("followPending") val followPending: Boolean,
    // TODO SOCIAL,
    // TODO FOLLOWERS,
    // TODO FOLLOWREQUESTS
)