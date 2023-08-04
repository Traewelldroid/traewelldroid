package de.hbch.traewelling.ui.activeCheckins

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActiveCheckinsViewModel : ViewModel() {

    val isRefreshing = MutableLiveData(false)
    val checkIns = mutableStateListOf<Status>()

    init {
        getActiveCheckins()
    }

    fun getActiveCheckins() {
        isRefreshing.postValue(true)
        TraewellingApi.checkInService.getStatuses()
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    isRefreshing.postValue(false)
                    if (response.isSuccessful) {
                        val statuses = response.body()
                        if (statuses != null) {
                            checkIns.clear()
                            checkIns.addAll(statuses.data)
                            return
                        }
                    }
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    isRefreshing.postValue(false)
                    Sentry.captureException(t)
                }
            })
    }
}