package de.hbch.traewelling.ui.statusDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import de.hbch.traewelling.api.models.status.Status
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusDetailViewModel: ViewModel() {
    fun getStatusById(
        statusId: Int,
        successfulCallback: (Status) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi
            .checkInService
            .getStatusById(statusId)
            .enqueue(object: Callback<Data<Status>> {
                override fun onResponse(
                    call: Call<Data<Status>>,
                    response: Response<Data<Status>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            successfulCallback(data)
                            return
                        }
                    }
                    failureCallback()
                }
                override fun onFailure(call: Call<Data<Status>>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }

    fun getPolylineForStatus(
        statusId: Int,
        successfulCallback: (FeatureCollection) -> Unit,
        failureCallback: () -> Unit
    ) {
        Log.d("MapEvents", "Loading polyline!")
        TraewellingApi
            .checkInService
            .getPolylinesForStatuses(listOf(statusId).joinToString(","))
            .enqueue(
                object: Callback<Data<FeatureCollection>> {
                    override fun onResponse(
                        call: Call<Data<FeatureCollection>>,
                        response: Response<Data<FeatureCollection>>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            if (data != null) {
                                successfulCallback(data)
                                return
                            }
                        }
                        failureCallback()
                    }
                    override fun onFailure(call: Call<Data<FeatureCollection>>, t: Throwable) {
                        failureCallback()
                        Sentry.captureException(t)
                    }
                }
            )
    }
}
