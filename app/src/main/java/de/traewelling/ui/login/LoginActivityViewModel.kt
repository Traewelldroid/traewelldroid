package de.traewelling.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginActivityViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    init {
        _username.value = "Gertrud"
    }
}