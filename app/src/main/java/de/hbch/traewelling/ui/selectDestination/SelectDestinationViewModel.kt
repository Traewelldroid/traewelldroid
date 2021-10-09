package de.hbch.traewelling.ui.selectDestination

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.trip.HafasTrainTrip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectDestinationViewModel : ViewModel() {
    private val _trip = MutableLiveData<HafasTrainTrip>()
    val trip: LiveData<HafasTrainTrip> get() = _trip

    fun getTrip(tripId: String, lineName: String, start: Int) {
        TraewellingApi.travelService.getTrip(tripId, lineName, start)
            .enqueue(object: Callback<Data<HafasTrainTrip>> {
                override fun onResponse(
                    call: Call<Data<HafasTrainTrip>>,
                    response: Response<Data<HafasTrainTrip>>
                ) {
                    if (response.isSuccessful) {
                        _trip.value = response.body()?.data!!
                    } else {
                        Log.e("SelectDestinationViewModel", response.toString())
                    }
                }
                override fun onFailure(call: Call<Data<HafasTrainTrip>>, t: Throwable) {
                    Log.e("SelectDestinationViewModel", t.stackTraceToString())
                }
            })
    }
}