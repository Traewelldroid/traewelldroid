package de.hbch.traewelling.shared

import androidx.lifecycle.LiveData
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : UserViewModel() {

    val loggedInUser: LiveData<User?> get() = _user

    fun login() =
        TraewellingApi.authService.getLoggedInUser().enqueue(loadUserCallback())

    fun logout(successCallback: () -> Unit, failureCallback: () -> Unit) {
        TraewellingApi.authService.logout()
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful)
                        successCallback()
                    else {
                        failureCallback()
                        Sentry.captureMessage(response.errorBody()?.string() ?: "")
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}