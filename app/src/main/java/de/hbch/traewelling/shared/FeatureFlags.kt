package de.hbch.traewelling.shared

import io.getunleash.UnleashClient

class FeatureFlags private constructor() {
    companion object {
        private var instance: FeatureFlags? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FeatureFlags().also { instance = it }
            }
    }

    private var unleashClient: UnleashClient? = null

    val profilePicInNavBar
        get() = unleashClient?.isEnabled("ProfilePicInNavBar") ?: false

    fun init(client: UnleashClient) {
        unleashClient = client
    }
}