package de.traewelling.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginActivityViewModel : ViewModel() {
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    fun login(): Boolean {
        Log.d("LoginActivityViewModel", "${username.value} ${password.value}")
        return true
    }
}