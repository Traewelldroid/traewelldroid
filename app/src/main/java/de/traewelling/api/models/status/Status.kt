package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import java.util.*

class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String?,
    @SerializedName("type") val type: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("user") val user: Int,
    @SerializedName("username") val username: String,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("business") val business: Int,
    @SerializedName("likes") val likes: Int,
    @SerializedName("liked") var liked: Boolean,
    @SerializedName("train") val journey: Journey
    // TODO Event
) {

}

enum class StatusVisibility(val visibility: Int) {
    @SerializedName("0")
    PUBLIC(0),
    @SerializedName("1")
    UNLISTED(1),
    @SerializedName("2")
    FOLLOWERS(2),
    @SerializedName("3")
    PRIVATE(3)
}