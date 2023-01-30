package de.hbch.traewelling.api.models.auth

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_at")
    val expiresAt: Instant,
    @SerializedName("token_type")
    val tokenType: String
)