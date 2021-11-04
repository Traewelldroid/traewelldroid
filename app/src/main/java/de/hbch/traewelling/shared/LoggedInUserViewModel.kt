package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : ViewModel() {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    val userId: LiveData<Int> get() = Transformations.map(_loggedInUser) { user ->
        user?.id ?: -1
    }

    val username: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        user?.username ?: ""
    }

    val kilometres: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        when (user != null) {
            true -> "${user.distance / 1000} km"
            false -> ""
        }
    }

    val points: LiveData<Int> get() = Transformations.map(_loggedInUser) { user ->
        user?.points ?: 0
    }

    val travelHours: LiveData<Int> get() = Transformations.map(_loggedInUser) { user ->
        (user?.duration ?: 0) / 60
    }
    val travelMinutes: LiveData<Int> get() = Transformations.map(_loggedInUser) { user ->
        (user?.duration ?: 0) % 60
    }

    val averageSpeed: LiveData<Double> get() = Transformations.map(_loggedInUser) { user ->
        (user?.averageSpeed?.div(1000)) ?: 0.0
    }

    fun setHomelandStation(station: Station) {
        _loggedInUser.value?.home = station
    }

    fun getLoggedInUser() {
        TraewellingApi.authService.getLoggedInUser()
            .enqueue(object: Callback<Data<User>> {
                override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                    if (response.isSuccessful) {
                        _loggedInUser.value = response.body()?.data
                    } else {
                        Log.e("LoggedInUserViewModel", response.toString())
                    }
                }
                override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                    Log.e("LoggedInUserViewModel", t.stackTraceToString())
                }
            })
    }

    fun logout(successCallback: () -> Unit, failureCallback: () -> Unit) {
        TraewellingApi.authService.logout()
            .enqueue(object: Callback<Unit> {
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