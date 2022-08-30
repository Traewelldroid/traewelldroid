package de.hbch.traewelling.ui.settings

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.auth.BearerToken
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsViewModel : ViewModel() {
    fun renewLogin(
        successCallback: (BearerToken) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .authService
            .refreshToken()
            .enqueue(object: Callback<Data<BearerToken>> {
                override fun onResponse(
                    call: Call<Data<BearerToken>>,
                    response: Response<Data<BearerToken>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data?.data != null)
                            successCallback(data.data)
                        return
                    }
                    failureCallback()
                }
                override fun onFailure(call: Call<Data<BearerToken>>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}