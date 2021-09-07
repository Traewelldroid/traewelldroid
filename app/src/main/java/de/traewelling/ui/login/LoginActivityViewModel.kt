package de.traewelling.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.BearerToken
import de.traewelling.api.models.LoginCredentials
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivityViewModel : ViewModel() {
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginSuccessful = MutableLiveData<Boolean>()
    val loginSuccessful: LiveData<Boolean> get() = _loginSuccessful
    private val _jwt = MutableLiveData<String>()
    val jwt: LiveData<String> get() = _jwt

    fun login() {
        TraewellingApi.authService.login(LoginCredentials(username.value!!, password.value!!))
            .enqueue(object: Callback<BearerToken> {
                override fun onResponse(call: Call<BearerToken>, response: Response<BearerToken>) {
                    if (response.isSuccessful) {
                        _jwt.value = response.body()?.jwt
                        _loginSuccessful.value = true
                    } else {
                        _loginSuccessful.value = false
                    }
                }
                override fun onFailure(call: Call<BearerToken>, t: Throwable) {
                    _loginSuccessful.value = false
                }
            })
    }
}