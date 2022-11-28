package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserViewModel : ViewModel() {
    protected val _user = MutableLiveData<User?>()

    val userId: LiveData<Int>
        get() = Transformations.map(_user) { user ->
            user?.id ?: -1
        }

    val username: LiveData<String>
        get() = Transformations.map(_user) { user ->
            user?.username ?: ""
        }

    val kilometres: LiveData<String>
        get() = Transformations.map(_user) { user ->
            when (user != null) {
                true -> "${user.distance / 1000} km"
                false -> ""
            }
        }

    val points: LiveData<Int>
        get() = Transformations.map(_user) { user ->
            user?.points ?: 0
        }

    val duration: LiveData<Int>
        get() = Transformations.map(_user) { user ->
            user?.duration ?: 0
        }

    val averageSpeed: LiveData<Double>
        get() = Transformations.map(_user) { user ->
            (user?.averageSpeed?.div(1000)) ?: 0.0
        }

    val homelandStation: LiveData<String>
        get() = Transformations.map(_user) { user ->
            user?.home?.name ?: ""
        }
    val defaultStatusVisibility: StatusVisibility
        get() = _user.value?.defaultStatusVisibility ?: StatusVisibility.PUBLIC

    val profilePictureSrc: LiveData<String>
        get() = Transformations.map(_user) { user ->
            "https://traewelling.de/@${user?.username ?: ""}/picture"
        }

    fun setHomelandStation(station: Station) {
        _user.value?.home = station
    }

    fun loadUserCallback(onSuccess: (Data<User>) -> Unit = {}) =
        object : Callback<Data<User>> {
            override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    _user.value = data?.data
                    data?.let { onSuccess(it) }
                } else {
                    Log.e("UserViewModel", response.toString())
                }
            }

            override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                Log.e("UserViewModel", t.stackTraceToString())
            }
        }


    fun getPersonalCheckIns(
        page: Int,
        successCallback: (StatusPage) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.checkInService.getStatusesForUser(_user.value?.username ?: "", page)
            .enqueue(object : Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        if (statusPage != null) {
                            successCallback(statusPage)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }

                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }

            })
    }

    fun loadUser(name: String, onSuccess: (Data<User>) -> Unit = {}) =
        TraewellingApi.userService.getUser(name).enqueue(loadUserCallback(onSuccess))
}
