package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventViewModel : ViewModel() {

    private val _activeEvents: MutableLiveData<List<Event>> = MutableLiveData(listOf())
    val activeEvents: LiveData<List<Event>> get() = _activeEvents

    fun activeEvents() {
        TraewellingApi
            .checkInService
            .getActiveEvents()
            .enqueue(object: Callback<Data<List<Event>>> {
                override fun onResponse(
                    call: Call<Data<List<Event>>>,
                    response: Response<Data<List<Event>>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null)
                            _activeEvents.postValue(data!!)
                        return
                    }
                    Sentry.captureMessage(response.errorBody()?.string() ?: "getActiveEvents Error")
                }

                override fun onFailure(call: Call<Data<List<Event>>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}