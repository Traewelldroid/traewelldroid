package de.hbch.traewelling.ui.searchConnection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasTripPage
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchConnectionViewModel: ViewModel() {

    private val _departures = MutableLiveData<HafasTripPage>()
    val departures: LiveData<HafasTripPage> get() = _departures

    fun searchConnections(stationName: String, departureTime: Date) {
        TraewellingApi.travelService.getDeparturesAtStation(stationName, departureTime)
            .enqueue(object: Callback<HafasTripPage> {
                override fun onResponse(
                    call: Call<HafasTripPage>,
                    response: Response<HafasTripPage>
                ) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        trip?.let {
                            _departures.value = it
                        }
                    } else {
                        Sentry.captureMessage(response.errorBody()?.string() ?: "SearchConnectionViewModel:searchConnections error")
                    }
                }
                override fun onFailure(call: Call<HafasTripPage>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }

    fun setUserHomelandStation(stationName: String, callback: (Station?) -> Unit) {
        TraewellingApi.authService.setUserHomelandStation(stationName)
            .enqueue(object: Callback<Data<Station>> {
                override fun onResponse(
                    call: Call<Data<Station>>,
                    response: Response<Data<Station>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.data);
                    } else {
                        Sentry.captureMessage(response.errorBody()?.toString() ?: "")
                    }
                }

                override fun onFailure(call: Call<Data<Station>>, t: Throwable) {
                    Sentry.captureException(t)
                }

            })
    }
}