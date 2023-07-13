package de.hbch.traewelling.api.models.notifications

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.meta.PaginationData
import java.util.Date

data class NotificationPage(
    val data: List<Notification>,
    val meta: PaginationData
)

data class Notification(
    val id: String,
    val type: NotificationType,
    var readAt: Date?,
    val createdAt: Date,
    val data: Any
)

data class NotificationStation(
    val name: String
)
data class NotificationUser(
    val id: Int,
    val username: String,
    val name: String
)
data class NotificationTrip<T>(
    val origin: T,
    val destination: T,
    @SerializedName("lineName", alternate = [ "linename" ]) val lineName: String
)
data class NotificationStatus(
    val id: Int
)

data class StatusLikedNotificationData(
    val trip: NotificationTrip<NotificationStation>,
    val liker: NotificationUser,
    val status: NotificationStatus
)

data class EventSuggestionProcessedData(
    val accepted: Boolean,
    val suggestedName: String
)

data class UserFollowedData(
    val follower: NotificationUser
)

data class UserJoinedConnectionData(
    val checkin: NotificationTrip<String>,
    val user: NotificationUser,
    val status: NotificationStatus
)

data class FollowRequestData(
    val user: NotificationUser
)
