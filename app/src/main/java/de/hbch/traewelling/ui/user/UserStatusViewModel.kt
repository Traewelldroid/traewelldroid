package de.hbch.traewelling.ui.user

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserStatusViewModel : ViewModel() {

    val checkIns = mutableStateListOf<Status>()
    val user = MutableLiveData<User?>(null)
    var isRefreshing = MutableLiveData(false)

    fun loadUser(username: String) {
        TraewellingApi.userService.getUser(username)
            .enqueue(object: Callback<Data<User>> {
                override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                    if (response.isSuccessful) {
                        val respUser = response.body()
                        if (respUser != null) {
                            user.postValue(respUser.data)
                        }
                        return
                    }
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }

                override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }

    fun loadStatusesForUser(
        username: String,
        page: Int = 1
    ) {
        isRefreshing.postValue(true)
        TraewellingApi.checkInService.getStatusesForUser(username, page)
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    isRefreshing.postValue(false)
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        if (statusPage != null) {
                            checkIns.addAll(statusPage.data)
                            return
                        }
                        Sentry.captureMessage(response.errorBody()?.string() ?: "")
                    }
                }

                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    isRefreshing.postValue(false)
                    Sentry.captureException(t)
                }

            })
    }
}