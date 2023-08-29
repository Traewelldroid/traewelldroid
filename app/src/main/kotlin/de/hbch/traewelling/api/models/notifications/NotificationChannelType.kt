package de.hbch.traewelling.api.models.notifications

import android.app.NotificationManager
import de.hbch.traewelling.R

enum class NotificationChannelType {
    Likes {
        override val title = R.string.channel_likes
        override val description = R.string.channel_description_likes
    },
    Follows {
        override val title = R.string.channel_follows
        override val description = R.string.channel_description_follows
    },
    JoinedUsers {
        override val importance = NotificationManager.IMPORTANCE_HIGH
        override val title = R.string.channel_joined_users
        override val description = R.string.channel_description_joined_users
    },
    EventSuggestion {
        override val importance = NotificationManager.IMPORTANCE_LOW
        override val title = R.string.channel_event_suggestions
        override val description = R.string.channel_description_event_suggestions
    },
    MastodonError {
        override val importance = NotificationManager.IMPORTANCE_HIGH
        override val title = R.string.channel_mastodon_errors
        override val description = R.string.channel_description_mastodon_errors
    };
    abstract val title: Int
    abstract val description: Int
    open val importance = NotificationManager.IMPORTANCE_DEFAULT
}
