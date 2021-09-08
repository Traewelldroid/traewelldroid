package de.traewelling.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.Data
import de.traewelling.api.models.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragmentViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _kilometres = MutableLiveData<Int>()
    val kilometres: LiveData<String> get() = map(_kilometres) { km ->
        "$km km"
    }

    private val _points = MutableLiveData<Int>()
    val points: LiveData<Int> get() = _points

    private val _travelTime = MutableLiveData<Int>()
    val travelHours: LiveData<Int> get() = map(_travelTime) { time ->
        time / 60
    }
    val travelMinutes: LiveData<Int> get() = map(_travelTime) {time ->
        time % 60
    }

    private val _averageSpeed = MutableLiveData<Double>()
    val averageSpeed: LiveData<Double> get() = _averageSpeed

    fun getUserInfo() {
        TraewellingApi.authService.getLoggedInUser()
            .enqueue(object: Callback<Data<User>> {
                override fun onResponse(call: Call<Data<User>>, response: Response<Data<User>>) {
                    if (response.isSuccessful) {
                        val user = response.body()?.data
                        if (user != null) {
                            _username.value = user.username
                            _kilometres.value = user.distance
                            _points.value = user.points
                            _travelTime.value = user.duration
                            _averageSpeed.value = user.averageSpeed
                        }
                    }
                }
                override fun onFailure(call: Call<Data<User>>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
    }
}