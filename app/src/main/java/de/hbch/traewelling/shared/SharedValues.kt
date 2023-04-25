package de.hbch.traewelling.shared

object SharedValues {
    val SS_JWT: String = "JWT"
    val SS_VERIFY_DOMAINS = "VERIFY_DOMAINS"
    val SS_HASHTAG = "HASHTAG"
    val EXTRA_STATUS_ID = "STATUS_ID"
    val EXTRA_USER_NAME = "USER_NAME"
    val EXTRA_STATION_ID = "EXTRA_STATION_ID"
    val EXTRA_TRAVEL_TYPE = "EXTRA_TRAVEL_TYPE"

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
        "write-settings-calendar",
        "extra-write-password",
        "extra-terminate-sessions",
        "extra-delete"
    )

    val URL_AUTHORIZATION = "https://traewelling.de/oauth/authorize"
    val URL_TOKEN_EXCHANGE = "https://traewelling.de/oauth/token"
}