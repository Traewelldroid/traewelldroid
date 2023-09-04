package de.hbch.traewelling.ui.selectDestination

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import de.hbch.traewelling.logging.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectDestinationViewModel : ViewModel() {

    fun getTrip(
        tripId: String,
        lineName: String,
        start: Int,
        successfulCallback: (Trip) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.travelService.getTrip(tripId, lineName, start)
            .enqueue(object: Callback<Data<HafasTrainTrip>> {
                override fun onResponse(
                    call: Call<Data<HafasTrainTrip>>,
                    response: Response<Data<HafasTrainTrip>>
                ) {
                    if (response.isSuccessful) {
                        val trip = response.body()?.data
                        if (trip != null) {
                            successfulCallback(trip.toTrip())
                            return
                        }
                    }
                    failureCallback()
                }
                override fun onFailure(call: Call<Data<HafasTrainTrip>>, t: Throwable) {
                    Logger.captureException(t)
                }
            })
    }
}
