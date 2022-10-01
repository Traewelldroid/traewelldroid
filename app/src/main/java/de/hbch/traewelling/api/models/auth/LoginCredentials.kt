package de.hbch.traewelling.api.models.auth

import com.google.gson.annotations.SerializedName

class LoginCredentials(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
    )