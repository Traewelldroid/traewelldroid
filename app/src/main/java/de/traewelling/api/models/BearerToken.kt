package de.traewelling.api.models

import com.google.gson.annotations.SerializedName

class BearerToken(
    @SerializedName("token") val jwt: String,
    @SerializedName("expires_at") val expiresAt: String)
{
}