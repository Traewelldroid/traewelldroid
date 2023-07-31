package de.hbch.traewelling.ui.searchConnection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.meta.Times
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasTripPage
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchConnectionViewModel: ViewModel() {

    private val _pageTimes = MutableLiveData<Times>()
    val pageTimes: LiveData<Times> get() = _pageTimes

    fun searchConnections(
        stationName: String,
        departureTime: Date,
        filterType: FilterType?,
        successCallback: (HafasTripPage) -> Unit,
        failureCallback: () -> Unit
    ) {
        val requestStationName = stationName.replace('/', ' ')
        TraewellingApi.travelService.getDeparturesAtStation(
            requestStationName,
            departureTime,
            filterType?.filterQuery ?: ""
        ).enqueue(object: Callback<HafasTripPage> {
                override fun onResponse(
                    call: Call<HafasTripPage>,
                    response: Response<HafasTripPage>
                ) {
                    if (response.isSuccessful) {
                        val trip = response.body()
                        if (trip != null) {
                            successCallback(trip)
                            _pageTimes.postValue(trip.meta.times)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }
                override fun onFailure(call: Call<HafasTripPage>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }

    fun setUserHomelandStation(
        stationName: String,
        successCallback: (Station) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.authService.setUserHomelandStation(stationName)
            .enqueue(object: Callback<Data<Station>> {
                override fun onResponse(
                    call: Call<Data<Station>>,
                    response: Response<Data<Station>>
                ) {
                    if (response.isSuccessful) {
                        val station = response.body()?.data
                        if (station != null) {
                            successCallback(station)
                            return
                        }
                    }
                    failureCallback()
                    Sentry.captureMessage(response.errorBody()?.string() ?: "")
                }

                override fun onFailure(call: Call<Data<Station>>, t: Throwable) {
                    failureCallback()
                    Sentry.captureException(t)
                }
            })
    }
}