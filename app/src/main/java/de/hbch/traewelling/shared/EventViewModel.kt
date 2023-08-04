package de.hbch.traewelling.shared

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

    val activeEvents: MutableLiveData<List<Event>> = MutableLiveData(listOf())

    fun activeEvents() {
        TraewellingApi
            .checkInService
            .getActiveEvents()
            .enqueue(object : Callback<Data<List<Event>>> {
                override fun onResponse(
                    call: Call<Data<List<Event>>>,
                    response: Response<Data<List<Event>>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data ?: return
                        activeEvents.postValue(data)
                        return
                    }
                }

                override fun onFailure(call: Call<Data<List<Event>>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}