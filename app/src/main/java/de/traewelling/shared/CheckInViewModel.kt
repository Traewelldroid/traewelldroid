package de.traewelling.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.Data
import de.traewelling.api.models.status.CheckInRequest
import de.traewelling.api.models.status.CheckInResponse
import de.traewelling.api.models.status.StatusVisibility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CheckInViewModel : ViewModel() {

    var lineName: String = ""
    var tripId: String = ""
    var startStationId: Int = 0
    var destinationStationId: Int = 0
    var arrivalTime: Date? = null
    var departureTime: Date? = null
    val message = MutableLiveData<String>()

    private val _checkInResponse = MutableLiveData<CheckInResponse?>()
    val checkInResponse: LiveData<CheckInResponse?> get() = _checkInResponse

    init {
        reset()
    }

    fun reset() {
        lineName = ""
        tripId = ""
        startStationId = 0
        destinationStationId = 0
        arrivalTime = null
        departureTime = null
        message.value = ""
        _checkInResponse.value = null
    }

    fun checkIn() {
        val checkInRequest = CheckInRequest(
            message.value!!,
            0,
            StatusVisibility.PUBLIC,
            0,
            false,
            false,
            tripId,
            lineName,
            startStationId,
            destinationStationId,
            departureTime!!,
            arrivalTime!!
        )
        TraewellingApi.checkInService.checkIn(checkInRequest)
            .enqueue(object: Callback<Data<CheckInResponse>> {
                override fun onResponse(
                    call: Call<Data<CheckInResponse>>,
                    response: Response<Data<CheckInResponse>>
                ) {
                    if (response.isSuccessful) {
                        _checkInResponse.value = response.body()?.data
                    } else {
                        _checkInResponse.value = null
                        Log.e("CheckInViewModel", response.toString())
                        Log.e("CheckInViewModel", response.errorBody()?.charStream()?.readText()!!)
                    }
                    reset()
                }
                override fun onFailure(call: Call<Data<CheckInResponse>>, t: Throwable) {
                    _checkInResponse.value = null
                    Log.e("CheckInViewModel", t.stackTraceToString())
                    reset()
                }
            })
    }
}