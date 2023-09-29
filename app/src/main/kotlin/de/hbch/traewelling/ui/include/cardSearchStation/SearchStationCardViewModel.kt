package de.hbch.traewelling.ui.include.cardSearchStation

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.station.StationData
import de.hbch.traewelling.logging.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchStationCardViewModel: ViewModel() {
    fun autoCompleteStationSearch(
        query: String,
        successCallback: (List<String>) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.travelService.autoCompleteStationSearch(query)
            .enqueue(object: Callback<Data<List<Station>>> {
                override fun onResponse(
                    call: Call<Data<List<Station>>>,
                    response: Response<Data<List<Station>>>
                ) {
                    if (response.isSuccessful) {
                        successCallback(response.body()?.data?.map {station -> station.name} ?: listOf())
                        return
                    }
                    failureCallback()
                }
                override fun onFailure(call: Call<Data<List<Station>>>, t: Throwable) {
                    failureCallback()
                    Logger.captureException(t)
                }
            })
    }

    fun getNearbyStation(
        latitude: Double,
        longitude: Double,
        successCallback: (String) -> Unit,
        failureCallback: () -> Unit
    ) {
        TraewellingApi.travelService.getNearbyStation(latitude, longitude)
            .enqueue(object: Callback<StationData> {
                override fun onResponse(call: Call<StationData>, response: Response<StationData>) {
                    if (response.isSuccessful) {
                        val station = response.body()?.data?.name
                        if (station != null) {
                            successCallback(station)
                            return
                        }
                    }
                    failureCallback()
                }

                override fun onFailure(call: Call<StationData>, t: Throwable) {
                    failureCallback()
                    Logger.captureException(t)
                }
            })
    }
}
