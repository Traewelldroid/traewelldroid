package de.hbch.traewelling.ui.login

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.auth.BearerToken
import de.hbch.traewelling.api.models.auth.LoginCredentials
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivityViewModel : ViewModel() {
    fun login(login: String, password: String, successCallback: (String?) -> Unit, failureCallback: () -> Unit) {
        TraewellingApi.authService.login(LoginCredentials(login, password))
            .enqueue(object: Callback<Data<BearerToken>> {
                override fun onResponse(call: Call<Data<BearerToken>>, response: Response<Data<BearerToken>>) {
                    if (response.isSuccessful) {
                        successCallback(response.body()?.data?.jwt)
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