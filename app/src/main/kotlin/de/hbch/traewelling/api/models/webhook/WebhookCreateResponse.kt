package de.hbch.traewelling.api.models.webhook

data class WebhookCreateResponse(
    val id: Int,
    val secret: String,
    val url: String
)

data class WebhookUserCreateRequest(
    val webhookId: Int,
    val webhookSecret: String,
    val notificationEndpoint: String
)
