package de.hbch.traewelling.api.models.notifications

import de.hbch.traewelling.api.models.meta.PaginationData
import java.time.LocalDateTime
import java.util.Date

data class NotificationPage(
    val data: List<Notification>,
    val meta: PaginationData
)

data class Notification(
    val id: String,
    val type: NotificationType,
    val readAt: Date?,
    val createdAt: Date,
    val data: Object
)

data class NotificationStation(
    val name: String
)
data class NotificationUser(
    val id: Int,
    val username: String,
    val name: String
)
data class NotificationTrip(
    val origin: NotificationStation,
    val destination: NotificationStation,
    val plannedDeparture: LocalDateTime,
    val plannedArrival: LocalDateTime,
    val lineName: String
)

data class StatusLikedNotificationData(
    val trip: NotificationTrip,
    val liker: NotificationUser
)
