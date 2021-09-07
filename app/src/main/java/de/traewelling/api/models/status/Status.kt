package de.traewelling.api.models.status

import com.google.gson.annotations.SerializedName
import de.traewelling.api.models.user.User
import java.util.*

class Status(
    @SerializedName("id") val id: Int,
    @SerializedName("body") val body: String,
    @SerializedName("visibility") val visibility: StatusVisibility,
    @SerializedName("type") val type: String,
    @SerializedName("event_id") val eventId: Int,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("favorited") val favorited: Boolean,
    @SerializedName("socialText") val socialText: String,
    @SerializedName("statusInvisibleToMe") val statusInvisibleToMe: Boolean,
    @SerializedName("user") val user: User,
    @SerializedName("train_checkin") val trainCheckIn: TrainCheckIn
    // TODO Event
) {

}

enum class StatusVisibility(val visibility: Int) {
    PUBLIC(0),
    UNLISTED(1),
    FOLLOWERS(2),
    PRIVATE(3)
}