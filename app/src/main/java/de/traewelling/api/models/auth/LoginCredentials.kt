package de.traewelling.api.models.auth

import com.google.gson.annotations.SerializedName

class LoginCredentials(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String)
{
}