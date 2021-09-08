package de.traewelling.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.auth.BearerToken
import de.traewelling.api.models.auth.LoginCredentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivityViewModel : ViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginSuccessful = MutableLiveData<Boolean>()
    val loginSuccessful: LiveData<Boolean> get() = _loginSuccessful
    private val _jwt = MutableLiveData<String>()
    val jwt: LiveData<String> get() = _jwt

    fun login() {
        TraewellingApi.authService.login(LoginCredentials(email.value!!, password.value!!))
            .enqueue(object: Callback<BearerToken> {
                override fun onResponse(call: Call<BearerToken>, response: Response<BearerToken>) {
                    if (response.isSuccessful) {
                        _jwt.value = response.body()?.jwt
                        Log.d("Login", jwt.value!!)
                        _loginSuccessful.value = true
                    } else {
                        _loginSuccessful.value = false
                        Log.e("LoginActivityViewModel", response.toString())
                    }
                }
                override fun onFailure(call: Call<BearerToken>, t: Throwable) {
                    _loginSuccessful.value = false
                    Log.e("LoginActivityViewModel", t.stackTraceToString())
                }
            })
    }
}