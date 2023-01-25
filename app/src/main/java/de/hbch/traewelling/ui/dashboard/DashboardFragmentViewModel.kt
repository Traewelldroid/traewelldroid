package de.hbch.traewelling.ui.dashboard

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.ui.include.status.CheckInListViewModel
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragmentViewModel : ViewModel(), CheckInListViewModel {
    override fun loadCheckIns(
        page: Int,
        successCallback: (List<Status>) -> Unit,
        failureCallback: (Throwable) -> Unit
    ) {
        TraewellingApi
            .checkInService
            .getPersonalDashboard(page)
            .enqueue(object: Callback<StatusPage> {
                override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                    if (response.isSuccessful) {
                        val statusPage = response.body()
                        if (statusPage != null) {
                            successCallback(statusPage.data)
                        }
                        return
                    }
                    val errorString = response.errorBody()?.string() ?: ""
                    failureCallback(RuntimeException(errorString))
                    Sentry.captureMessage(errorString)
                }
                override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                    failureCallback(t)
                    Sentry.captureException(t)
                }
            })
    }
}