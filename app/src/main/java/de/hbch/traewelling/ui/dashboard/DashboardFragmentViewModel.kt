package de.hbch.traewelling.ui.dashboard

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

class DashboardFragmentViewModel : ViewModel() {

    val checkIns = mutableStateListOf<Status>()
    var isRefreshing = MutableLiveData(false)

    init {
        loadCheckIns(1)
    }

    fun loadCheckIns(
        page: Int
    ) {
        isRefreshing.postValue(true)
        TraewellingApi
            .checkInService
            .getPersonalDashboard(page)
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    isRefreshing.postValue(false)
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        if (statusPage != null) {
                            checkIns.addAll(statusPage.data)
                        }
                        return
                    }
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    isRefreshing.postValue(false)
                    Sentry.captureException(t)
                }
            })
    }

    fun refresh() {
        checkIns.clear()
        loadCheckIns(1)
    }
}