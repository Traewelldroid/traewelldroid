package de.hbch.traewelling.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.WebhookRelayApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.util.removeDynamicShortcuts
import io.sentry.Sentry
import org.unifiedpush.android.connector.UnifiedPush
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoggedInUserViewModel : ViewModel() {

    protected val _user = MutableLiveData<User?>()

    val user: LiveData<User?> get() = _user

    val username: LiveData<String>
        get() = _user.map { user ->
            user?.username ?: ""
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

    val loggedInUser: LiveData<User?> get() = _user

    private val _lastVisitedStations = MutableLiveData<List<Station>?>(null)
    val lastVisitedStations: LiveData<List<Station>?> get() = _lastVisitedStations

    fun getLoggedInUser() =
        TraewellingApi.authService.getLoggedInUser().enqueue(loadUserCallback())

    fun logout(successCallback: () -> Unit, failureCallback: () -> Unit) {
        TraewellingApi.authService.logout()
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful)
                        successCallback()
                    else {
                        failureCallback()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }

    fun logoutWithRestart(context: Context) {
        val secureStorage = SecureStorage(context)
        val webhookUserId = secureStorage.getObject(SharedValues.SS_WEBHOOK_USER_ID, String::class.java)
        val webhookId = secureStorage.getObject(SharedValues.SS_TRWL_WEBHOOK_ID, Int::class.java)

        deleteWebhookUser(webhookUserId ?: "") {
            deleteWebhook(webhookId ?: -1) {
                logout(
                    {
                        resetApplication(context)
                    },
                    {
                        resetApplication(context)
                    }
                )
            }
        }
    }

    fun resetApplication(context: Context) {
        val secureStorage = SecureStorage(context)
        secureStorage.removeObject(SharedValues.SS_UP_ENDPOINT)
        secureStorage.removeObject(SharedValues.SS_WEBHOOK_USER_ID)
        secureStorage.removeObject(SharedValues.SS_JWT)
        secureStorage.removeObject(SharedValues.SS_NOTIFICATIONS_ENABLED)
        secureStorage.removeObject(SharedValues.SS_REFRESH_TOKEN)
        secureStorage.removeObject(SharedValues.SS_TRWL_WEBHOOK_ID)
        UnifiedPush.unregisterApp(context)
        context.removeDynamicShortcuts()
        context.startActivity(Intent(context, LoginActivity::class.java))
        (context as? Activity)?.finish()
    }

    private fun deleteWebhookUser(
        id: String,
        callback: () -> Unit
    ) {
        WebhookRelayApi.service.deleteWebhookUser(id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    callback()
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback()
                }
            })
    }

    private fun deleteWebhook(
        id: Int,
        callback: () -> Unit
    ) {
        TraewellingApi.authService.deleteWebhook(id)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    callback()
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback()
                }
            })
    }

    fun getLastVisitedStations(onDone: (List<Station>) -> Unit) {
        TraewellingApi.authService.getLastVisitedStations()
            .enqueue(object : Callback<Data<List<Station>>> {
                override fun onResponse(
                    call: Call<Data<List<Station>>>,
                    response: Response<Data<List<Station>>>
                ) {
                    if (response.isSuccessful) {
                        val stations = response.body()
                        if (stations != null) {
                            _lastVisitedStations.postValue(stations.data)
                            onDone(stations.data)
                        }
                    }
                }

                override fun onFailure(call: Call<Data<List<Station>>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}
