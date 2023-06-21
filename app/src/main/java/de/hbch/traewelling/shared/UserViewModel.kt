package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
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

    val user: LiveData<User?> get() = _user

    val userId: LiveData<Int>
        get() = _user.map { user ->
            user?.id ?: -1
        }

    val username: LiveData<String>
        get() = _user.map { user ->
            user?.username ?: ""
        }

    val points: LiveData<Int>
        get() = _user.map { user ->
            user?.points ?: 0
        }

    val duration: LiveData<Int>
        get() = _user.map { user ->
            user?.duration ?: 0
        }
    val home: LiveData<Station?>
        get() = _user.map { user ->
            user?.home
        }

    val defaultStatusVisibility: StatusVisibility
        get() = _user.value?.defaultStatusVisibility ?: StatusVisibility.PUBLIC

    fun setHomelandStation(station: Station) {
        _user.value?.home = station
    }

    fun loadUserCallback(onSuccess: (Data<User>) -> Unit = {}) =
        object : Callback<Data<User>> {
            override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    _user.postValue(data?.data)
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

    fun handleFollowButton() {
        val successCallback: (User?) -> Unit = {
            _user.postValue(it)
        }
        val apiCallback = object : Callback<Data<Unit>> {
            override fun onResponse(call: Call<Data<Unit>>, response: Response<Data<Unit>>) {
                if (response.isSuccessful) {
                    successCallback(user.value?.let { it.copy(following = !it.following) })
                }
            }

            override fun onFailure(call: Call<Data<Unit>>, t: Throwable) {
                Sentry.captureException(t)
            }
        }

        if (user.value?.following == false) {
            followUser(apiCallback)
        } else {
            unfollowUser(apiCallback)
        }
    }

    private fun followUser(successCallback: Callback<Data<Unit>>) {
        TraewellingApi.userService.followUser(user.value?.id ?: 0)
            .enqueue(successCallback)
    }

    private fun unfollowUser(successCallback: Callback<Data<Unit>>) {
        TraewellingApi.userService.unfollowUser(user.value?.id ?: 0)
            .enqueue(successCallback)
    }

    fun handleMuteButton() {
        val successCallback: (User?) -> Unit = {
            _user.postValue(it)
        }
        val apiCallback = object : Callback<Data<Unit>> {
            override fun onResponse(call: Call<Data<Unit>>, response: Response<Data<Unit>>) {
                if (response.isSuccessful) {
                    successCallback(user.value?.let { it.copy(muted = !it.muted) })
                }
            }

            override fun onFailure(call: Call<Data<Unit>>, t: Throwable) {
                Sentry.captureException(t)
            }
        }

        if (user.value?.muted == false)
            muteUser(apiCallback)
        else
            unmuteUser(apiCallback)
    }

    private fun muteUser(apiCallback: Callback<Data<Unit>>) {
        TraewellingApi.userService.muteUser(user.value?.id ?: 0)
            .enqueue(apiCallback)
    }

    private fun unmuteUser(apiCallback: Callback<Data<Unit>>) {
        TraewellingApi.userService.unmuteUser(user.value?.id ?: 0)
            .enqueue(apiCallback)
    }
}
