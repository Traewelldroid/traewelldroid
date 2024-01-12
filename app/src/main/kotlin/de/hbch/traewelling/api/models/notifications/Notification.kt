package de.hbch.traewelling.api.models.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hbch.traewelling.R
import de.hbch.traewelling.util.TraewelldroidUriBuilder

@Suppress("unused")
enum class NotificationType {
    EventSuggestionProcessed {
        override val icon = R.drawable.ic_calendar
        override val channel = NotificationChannelType.EventSuggestion
        override fun getHeadline(context: Context, notification: Notification): String  {
            return context.getString(R.string.event_suggestion_processed)
        }
        override fun getBody(context: Context, notification: Notification): String {
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

                body = context.getString(stringRes, obj.suggestedName)
            }
            return body
        }
    },
    FollowRequestIssued {
        override val icon = R.drawable.ic_add_person
        override val channel = NotificationChannelType.Follows
        override fun getHeadline(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = context.getString(R.string.follow_request_issued, obj.user.username)
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.Builder()
                        .scheme("https")
                        .authority("traewelling.de")
                        .appendPath("settings")
                        .appendPath("follower")
                        .build()
                )
            }
            return intent
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
        override val channel = NotificationChannelType.Follows
        override fun getHeadline(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = context.getString(
                    R.string.follow_request_approved,
                    obj.user.username
                )
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    TraewelldroidUriBuilder()
                        .appendPath("@${data.user.username}")
                        .build()
                )
            }
            return intent
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
        override val channel = NotificationChannelType.Likes
        override fun getHeadline(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = context.getString(
                    R.string.user_likes_status,
                    obj.liker.username
                )
            }
            return headline
        }
        override fun getBody(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var body = ""
            if (obj != null) {
                body = context.getString(
                    R.string.travelling_with_from_to,
                    obj.trip.lineName,
                    obj.trip.origin.name,
                    obj.trip.destination.name
                )
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    TraewelldroidUriBuilder()
                        .appendPath("status")
                        .appendPath(data.status.id.toString())
                        .build()
                )
            }
            return intent
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
        override val channel = NotificationChannelType.Follows
        override fun getHeadline(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = context.getString(R.string.user_followed, obj.follower.username)
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    TraewelldroidUriBuilder()
                        .appendPath("@${data.follower.username}")
                        .build()
                )
            }
            return intent
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
        override val channel = NotificationChannelType.JoinedUsers
        override fun getHeadline(context: Context, notification: Notification): String  {
            val obj = getData(notification)
            var headline = ""
            if (obj != null) {
                headline = context.getString(R.string.user_also_on_connection, obj.user.username)
            }
            return headline
        }
        override fun getBody(context: Context, notification: Notification): String  {
            var body = ""
            val obj = getData(notification)
            if (obj != null) {
                body = context.getString(
                    R.string.travelling_with_from_to,
                    obj.checkin.lineName,
                    obj.checkin.origin,
                    obj.checkin.destination
                )
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    TraewelldroidUriBuilder()
                        .appendPath("status")
                        .appendPath(data.status.id.toString())
                        .build()
                )
            }
            return intent
        }

        private fun getData(notification: Notification): UserJoinedConnectionData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<UserJoinedConnectionData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? UserJoinedConnectionData
        }
    },
    MastodonNotSent {
        override val icon = R.drawable.ic_error
        override val channel = NotificationChannelType.MastodonError
        override val category = android.app.Notification.CATEGORY_ERROR
        override fun getHeadline(context: Context, notification: Notification): String  {
            return context.getString(R.string.mastodon_share_error)
        }
        override fun getBody(context: Context, notification: Notification): String {
            val obj = getData(notification)
            var body = ""
            if (obj != null) {
                body = context.getString(R.string.status_not_shared_on_mastodon, obj.httpResponseCode)
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
        override fun getIntent(context: Context, notification: Notification): Intent? {
            val data = getData(notification)
            var intent: Intent? = null
            if (data != null) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    TraewelldroidUriBuilder()
                        .appendPath("status")
                        .appendPath(data.status.id.toString())
                        .build()
                )
            }
            return intent
        }

        private fun getData(notification: Notification): MastodonNotSentData? {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<MastodonNotSentData>() {}.type
            return gson.fromJson<Any>(gson.toJson(data), targetType) as? MastodonNotSentData
        }
    };

    abstract val icon: Int
    abstract val channel: NotificationChannelType
    open val category: String = android.app.Notification.CATEGORY_SOCIAL
    abstract fun getHeadline(context: Context, notification: Notification): String
    open fun getBody(context: Context, notification: Notification): String? = null
    open fun getOnClick(notification: Notification): (NavHostController) -> Unit = { }
    open fun getIntent(context: Context, notification: Notification): Intent? = null
}
