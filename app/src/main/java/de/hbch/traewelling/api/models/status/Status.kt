package de.hbch.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.event.Event
import java.util.*

data class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String?,
    @SerializedName("type") val type: String,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("user") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("preventIndex") val preventIndex: Boolean,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("business") val business: StatusBusiness,
    @SerializedName("likes") var likes: Int,
    @SerializedName("liked") var liked: Boolean,
    @SerializedName("train") val journey: Journey,
    @SerializedName("event") val event: Event?
)

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

enum class StatusBusiness(val business: Int) {
    @SerializedName("0")
    PRIVATE(0),
    @SerializedName("1")
    BUSINESS(1),
    @SerializedName("2")
    COMMUTE(2)
}