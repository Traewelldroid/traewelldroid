package de.hbch.traewelling.shared

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration

object SharedValues {
    const val SS_JWT = "JWT"
    const val SS_REFRESH_TOKEN = "REFRESH_TOKEN"
    const val SS_HASHTAG = "HASHTAG"
    const val SS_ORM_LAYER = "ORM_LAYER"
    const val SS_UP_ENDPOINT = "UP_ENDPOINT"
    const val SS_WEBHOOK_USER_ID = "WEBHOOK_USER_ID"
    const val SS_TRWL_WEBHOOK_ID = "TRWL_WEBHOOK_ID"
    const val SS_NOTIFICATIONS_ENABLED = "NOTIFICATIONS_ENABLED"
    const val SS_CHECK_IN_COUNT = "CHECK_IN_COUNT"

    val AUTH_SCOPES = listOf(
        "read-statuses",
        "read-notifications",
        "read-statistics",
        "read-search",
        "write-statuses",
        "write-likes",
        "write-notifications",
        "write-exports",
        "write-follows",
        "write-followers",
        "write-blocks",
        "write-event-suggestions",
        "write-support-tickets",
        "read-settings",
        "write-settings-profile",
        "read-settings-profile",
        "write-settings-mail",
        "write-settings-profile-picture",
        "write-settings-privacy",
        "read-settings-followers",
        "write-settings-calendar"
    )

    const val URL_AUTHORIZATION = "https://traewelling.de/oauth/authorize"
    const val URL_TOKEN_EXCHANGE = "https://traewelling.de/oauth/token"

    val AUTH_SERVICE_CONFIG = AuthorizationServiceConfiguration(
        Uri.parse(URL_AUTHORIZATION),
        Uri.parse(URL_TOKEN_EXCHANGE)
    )
}
