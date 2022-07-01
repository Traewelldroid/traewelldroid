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
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : ViewModel() {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    private val _lastVisitedStations = MutableLiveData<List<Station>?>(null)
    val lastVisitedStations: LiveData<List<Station>?> get() = _lastVisitedStations

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

    val duration: LiveData<Int> get() = Transformations.map(_loggedInUser) { user ->
        user?.duration ?: 0
    }

    val averageSpeed: LiveData<Double> get() = Transformations.map(_loggedInUser) { user ->
        (user?.averageSpeed?.div(1000)) ?: 0.0
    }

    val homelandStation: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        user?.home?.name ?: ""
    }

    val profilePictureSrc: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        "https://traewelling.de/@${user?.username ?: ""}/picture"
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

    fun getLastVisitedStations() {
        TraewellingApi.authService.getLastVisitedStations()
            .enqueue(object: Callback<Data<List<Station>>> {
                override fun onResponse(
                    call: Call<Data<List<Station>>>,
                    response: Response<Data<List<Station>>>
                ) {
                    if (response.isSuccessful) {
                        val stations = response.body()
                        if (stations != null) {
                            _lastVisitedStations.postValue(stations.data)
                        }
                    }
                }

                override fun onFailure(call: Call<Data<List<Station>>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }

    fun getPersonalCheckIns(page: Int, successCallback: (StatusPage) -> Unit, failureCallback: () -> Unit) {
        TraewellingApi.checkInService.getStatusesForUser(_loggedInUser.value?.username ?: "", page)
            .enqueue(object: Callback<StatusPage> {
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