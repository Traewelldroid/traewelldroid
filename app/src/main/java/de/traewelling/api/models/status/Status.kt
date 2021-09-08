package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import java.util.*

class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String?,
    @SerializedName("type") val type: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("user") val user: Int,
    @SerializedName("username") val username: String,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("business") val business: Int,
    @SerializedName("likes") val likes: Int,
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("train") val journey: Journey
    // TODO Event
) {

}

enum class StatusVisibility(val visibility: Int) {
    PUBLIC(0),
    UNLISTED(1),
    FOLLOWERS(2),
    PRIVATE(3)
}