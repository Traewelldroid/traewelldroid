package de.hbch.traewelling.api.models.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hbch.traewelling.R

@Suppress("unused")
enum class NotificationType {
    EventSuggestionProcessed {
        override val icon = R.drawable.ic_calendar
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return stringResource(id = R.string.event_suggestion_processed)
        }
        @Composable
        override fun getBody(notification: Notification): String {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<EventSuggestionProcessedData>() {}.type
            val obj = gson.fromJson<Any>(gson.toJson(data), targetType) as? EventSuggestionProcessedData
            var body = ""
            if (obj != null) {
                val stringRes =
                    if (obj.accepted)
                        R.string.event_suggestion_accepted
                    else
                        R.string.event_suggestion_rejected

                body = stringResource(id = stringRes, obj.suggestedName)
            }
            return body
        }
    },
    FollowRequestIssued {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.follow_request_issued, obj.user.username)
            }
            return headline
        }
        override fun getOnClick(notification: Notification): (NavHostController) -> Unit {
            val data = getData(notification)
            var onClick: (NavHostController) -> Unit = { }
            if (data != null) {
                onClick = {
                    it.navigate("personal-profile/?username=${data.user.username}")
                }
            }
            return onClick
        }

        private fun getData(notification: Notification): FollowRequestData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<FollowRequestData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? FollowRequestData
        }
    },
    FollowRequestApproved {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.follow_request_approved, obj.user.username)
            }
            return headline
        }
        override fun getOnClick(notification: Notification): (NavHostController) -> Unit {
            val data = getData(notification)
            var onClick: (NavHostController) -> Unit = { }
            if (data != null) {
                onClick = {
                    it.navigate("personal-profile/?username=${data.user.username}")
                }
            }
            return onClick
        }

        private fun getData(notification: Notification): FollowRequestData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<FollowRequestData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? FollowRequestData
        }
    },
    StatusLiked {
        override val icon = R.drawable.ic_faved
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.user_likes_status, obj.liker.username)
            }
            return headline
        }
        @Composable
        override fun getBody(notification: Notification): String  {
            val obj = getData(notification)
            var body = ""
            if (obj != null) {
                body = stringResource(id = R.string.travelling_with_from_to, obj.trip.lineName, obj.trip.origin.name, obj.trip.destination.name)
            }
            return body
        }
        override fun getOnClick(notification: Notification): (NavHostController) -> Unit {
            val data = getData(notification)
            var onClick: (NavHostController) -> Unit = { }
            if (data != null) {
                onClick = {
                    it.navigate("status-details/${data.status.id}")
                }
            }
            return onClick
        }

        private fun getData(notification: Notification): StatusLikedNotificationData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<StatusLikedNotificationData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? StatusLikedNotificationData
        }
    },
    UserFollowed {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.user_followed, obj.follower.username)
            }
            return headline
        }
        override fun getOnClick(notification: Notification): (NavHostController) -> Unit {
            val data = getData(notification)
            var onClick: (NavHostController) -> Unit = { }
            if (data != null) {
                onClick = {
                    it.navigate("personal-profile/?username=${data.follower.username}")
                }
            }
            return onClick
        }

        private fun getData(notification: Notification): UserFollowedData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<UserFollowedData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? UserFollowedData
        }
    },
    UserJoinedConnection {
        override val icon = R.drawable.ic_also_check_in
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.user_also_on_connection, obj.user.username)
            }
            return headline
        }
        @Composable
        override fun getBody(notification: Notification): String  {
            var body = ""
            val obj = getData(notification)
            if (obj != null) {
                body = stringResource(id = R.string.travelling_with_from_to, obj.checkin.lineName, obj.checkin.origin, obj.checkin.destination)
            }
            return body
        }
        override fun getOnClick(notification: Notification): (NavHostController) -> Unit {
            val data = getData(notification)
            var onClick: (NavHostController) -> Unit = { }
            if (data != null) {
                onClick = {
                    it.navigate("status-details/${data.status.id}")
                }
            }
            return onClick
        }

        private fun getData(notification: Notification): UserJoinedConnectionData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<UserJoinedConnectionData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? UserJoinedConnectionData
        }
    };

    abstract val icon: Int
    @Composable
    abstract fun getHeadline(notification: Notification): String
    @Composable
    open fun getBody(notification: Notification): String? = null
    open fun getOnClick(notification: Notification): (NavHostController) -> Unit = { }
}