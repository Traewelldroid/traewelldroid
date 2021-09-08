package de.traewelling.ui.activeCheckins

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.status.StatusPage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActiveCheckinsViewModel : ViewModel() {
    private val _statuses = MutableLiveData<StatusPage>()
    val statuses: LiveData<StatusPage> get() = _statuses

    fun getActiveCheckins() {
        TraewellingApi.checkInService.getStatuses()
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        _statuses.value = response.body()
                    }
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    Log.e("ActiveCheckinsViewModel", t.stackTraceToString())
                }
            })
    }
}