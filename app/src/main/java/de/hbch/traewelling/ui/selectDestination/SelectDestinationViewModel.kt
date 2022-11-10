package de.hbch.traewelling.ui.selectDestination

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectDestinationViewModel : ViewModel() {

    fun getTrip(
        tripId: String,
        lineName: String,
        start: Int,
        successfulCallback: (HafasTrainTrip) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.travelService.getTrip(tripId, tripId, lineName, start)
            .enqueue(object: Callback<Data<HafasTrainTrip>> {
                override fun onResponse(
                    call: Call<Data<HafasTrainTrip>>,
                    response: Response<Data<HafasTrainTrip>>
                ) {
                    if (response.isSuccessful) {
                        val trip = response.body()?.data
                        if (trip != null) {
                            successfulCallback(trip)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }
                override fun onFailure(call: Call<Data<HafasTrainTrip>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}