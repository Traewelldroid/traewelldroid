package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.ui.include.status.CheckInListViewModel
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserViewModel : ViewModel(), CheckInListViewModel {
    protected val _user = MutableLiveData<User?>()

    val user: LiveData<User?> get() = _user

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
            user?.avatarUrl ?: ""
        }

    val privateProfile: LiveData<Boolean>
        get() = Transformations.map(_user) { user ->
            user?.privateProfile ?: false
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


    override fun loadCheckIns(
        page: Int,
        successCallback: (List<Status>) -> Unit,
        failureCallback: (Throwable) -> Unit
    ) {
        TraewellingApi.checkInService.getStatusesForUser(_user.value?.username ?: "", page)
            .enqueue(object : Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        if (statusPage != null) {
                            successCallback(statusPage.data)
                            return
                        }
                    }
                    val errorString = response.errorBody()?.string() ?: ""
                    failureCallback(RuntimeException(errorString))
                    Sentry.captureMessage(errorString)
                }

                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    failureCallback(t)
                    Sentry.captureException(t)
                }
            })
    }

    fun loadUser(name: String, onSuccess: (Data<User>) -> Unit = {}) =
        TraewellingApi.userService.getUser(name).enqueue(loadUserCallback(onSuccess))

    fun handleFollowButton() {
        val successCallback: (User?) -> Unit = {
            _user.postValue(it)
        }
        val apiCallback = object: Callback<Data<User>> {
            override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                if (response.isSuccessful) {
                    successCallback(response.body()?.data)
                }
            }

            override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                Sentry.captureException(t)
            }
        }

        if (user.value?.following == false) {
            followUser(apiCallback)
        } else {
            unfollowUser(apiCallback)
        }
    }

    private fun followUser(successCallback: Callback<Data<User>>) {
        TraewellingApi.userService.followUser(user.value?.id ?: 0)
            .enqueue(successCallback)
    }

    private fun unfollowUser(successCallback: Callback<Data<User>>) {
        TraewellingApi.userService.unfollowUser(user.value?.id ?: 0)
            .enqueue(successCallback)
    }

    fun handleMuteButton() {
        val successCallback: (User?) -> Unit = {
            _user.postValue(it)
        }
        val apiCallback = object: Callback<Data<User>> {
            override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                if (response.isSuccessful) {
                    successCallback(response.body()?.data)
                }
            }
            override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                Sentry.captureException(t)
            }
        }

        if (user.value?.muted == false)
            muteUser(apiCallback)
        else
            unmuteUser(apiCallback)
    }

    private fun muteUser(apiCallback: Callback<Data<User>>) {
        TraewellingApi.userService.muteUser(user.value?.id ?: 0)
            .enqueue(apiCallback)
    }

    private fun unmuteUser(apiCallback: Callback<Data<User>>) {
        TraewellingApi.userService.unmuteUser(user.value?.id ?: 0)
            .enqueue(apiCallback)
    }
}
