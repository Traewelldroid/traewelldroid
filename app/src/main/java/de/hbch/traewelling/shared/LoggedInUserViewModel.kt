package de.hbch.traewelling.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.user.User
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : UserViewModel() {

    val loggedInUser: LiveData<User?> get() = _user

    private val _lastVisitedStations = MutableLiveData<List<Station>?>(null)
    val lastVisitedStations: LiveData<List<Station>?> get() = _lastVisitedStations

    fun getLoggedInUser() =
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

    fun getLastVisitedStations(onDone: (List<Station>) -> Unit) {
        TraewellingApi.authService.getLastVisitedStations()
            .enqueue(object : Callback<Data<List<Station>>> {
                override fun onResponse(
                    call: Call<Data<List<Station>>>,
                    response: Response<Data<List<Station>>>
                ) {
                    if (response.isSuccessful) {
                        val stations = response.body()
                        if (stations != null) {
                            _lastVisitedStations.postValue(stations.data)
                            onDone(stations.data)
                        }
                    }
                }

                override fun onFailure(call: Call<Data<List<Station>>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}