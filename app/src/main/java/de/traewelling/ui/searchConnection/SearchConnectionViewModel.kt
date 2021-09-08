package de.traewelling.ui.searchConnection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import de.traewelling.adapters.ConnectionAdapter
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.trip.HafasTrip
import de.traewelling.api.models.trip.HafasTripPage
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
                        if (trip != null) {
                            _departures.value = trip!!
                        }
                    }
                }
                override fun onFailure(call: Call<HafasTripPage>, t: Throwable) {

                }
            })
    }
}