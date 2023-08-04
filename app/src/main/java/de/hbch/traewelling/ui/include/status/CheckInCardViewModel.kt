package de.hbch.traewelling.ui.include.status

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInCardViewModel : ViewModel() {
    fun createFavorite(statusId: Int, successCallback: () -> Unit) {
        TraewellingApi.checkInService.createFavorite(statusId)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        successCallback()
                        return
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }

    fun deleteFavorite(statusId: Int, successCallback: () -> Unit) {
        TraewellingApi.checkInService.deleteFavorite(statusId)
            .enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        successCallback()
                        return
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }

    fun deleteStatus(
        statusId: Int,
        successCallback: () -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.checkInService.deleteStatus(statusId)
            .enqueue(object: Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.isSuccessful) {
                        successCallback()
                        return
                    }
                    failureCallback()
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}