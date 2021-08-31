package de.traewelling.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel

class UserFragmentViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _kilometres = MutableLiveData<Double>()
    val kilometres: LiveData<String> get() = map(_kilometres) { km ->
        "${"%.2f".format(km)} km"
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

    init {
        _username.value = "@gertrud"
        _kilometres.value = 123456.567
        _points.value = 42
        _travelTime.value = 317
        _averageSpeed.value = 186.645
    }
}