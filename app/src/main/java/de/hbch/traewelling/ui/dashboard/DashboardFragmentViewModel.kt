package de.hbch.traewelling.ui.dashboard

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.util.fetchStopOvers
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragmentViewModel : ViewModel() {
    fun loadCheckIns(
        page: Int,
        successCallback: (List<Status>) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .getPersonalDashboard(page)
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        statusPage?.data?.fetchStopOvers(successCallback)
                        return
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}