package de.hbch.traewelling.ui.login

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.WebhookRelayApi
import de.hbch.traewelling.api.models.webhook.WebhookUserCreateRequest
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    fun createWebhookUser(
        webhookUserCreateRequest: WebhookUserCreateRequest,
        successfulCallback: (String) -> Unit,
        failureCallback: () -> Unit
    ) {
        WebhookRelayApi.service
            .createWebhookUser(webhookUserCreateRequest)
            .enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val id = response.body()
                        if (id != null) {
                            successfulCallback(id)
                            return
                        }
                    }
                    failureCallback()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}
