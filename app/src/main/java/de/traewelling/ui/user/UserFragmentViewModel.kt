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

    init {
        _username.value = "@gertrud"
        _kilometres.value = 1234.567
        _points.value = 42
    }
}