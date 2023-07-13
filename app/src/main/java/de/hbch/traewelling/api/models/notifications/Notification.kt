package de.hbch.traewelling.api.models.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import de.hbch.traewelling.R

enum class NotificationType {
    EventSuggestionProcessed {
        override val icon = R.drawable.ic_calendar
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return ""
        }
    },
    FollowRequestIssued {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return ""
        }
    },
    FollowRequestApproved {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return ""
        }
    },
    StatusLiked {
        override val icon = R.drawable.ic_faved
        @Composable
        override fun getHeadline(notification: Notification): String  {
            val gson = Gson()
            val data = notification.data
            val targetType = object: TypeToken<StatusLikedNotificationData>() {}.type
            val obj = gson.fromJson<Any>(gson.toJson(data), targetType) as StatusLikedNotificationData
            var headline = ""
            if (obj != null) {
                headline = stringResource(id = R.string.user_likes_status, obj.liker.username)
            }
            return headline
        }
        @Composable
        override fun getBody(notification: Notification): String  {
            val data = notification.data as? StatusLikedNotificationData
            var body = ""
            if (data != null) {
                body = stringResource(id = R.string.user_likes_status, data.liker.username)
            }
            return body
        }
    },
    UserFollowed {
        override val icon = R.drawable.ic_add_person
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return ""
        }
    },
    UserJoinedConnection {
        override val icon = R.drawable.ic_also_check_in
        @Composable
        override fun getHeadline(notification: Notification): String  {
            return ""
        }
    };

    abstract val icon: Int
    @Composable
    abstract fun getHeadline(notification: Notification): String
    @Composable
    open fun getBody(notification: Notification): String? = null
}