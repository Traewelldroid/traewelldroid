package de.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.Data
import de.traewelling.api.models.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : ViewModel() {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> get() = _loggedInUser

    val username: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        user?.username ?: ""
    }

    val kilometres: LiveData<String> get() = Transformations.map(_loggedInUser) { user ->
        when (user != null) {
            true -> "${user.distance} km"
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
        user?.averageSpeed ?: 0.0
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
}