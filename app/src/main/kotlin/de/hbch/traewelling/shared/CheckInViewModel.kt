package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.CheckInRequest
import de.hbch.traewelling.api.models.status.CheckInResponse
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.status.UpdateStatusRequest
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.logging.Logger
import de.hbch.traewelling.ui.checkInResult.CheckInResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime

class CheckInViewModel : ViewModel() {
    var lineName: String = ""
    var tripId: String = ""
    var startStationId: Int = 0
    var destinationStationId: Int = 0
    var departureTime: ZonedDateTime? = null
    var manualDepartureTime: ZonedDateTime? = null
    var arrivalTime: ZonedDateTime? = null
    var manualArrivalTime: ZonedDateTime? = null
    val message = MutableLiveData<String>()
    val toot = MutableLiveData(false)
    val chainToot = MutableLiveData(false)
    val statusVisibility = MutableLiveData(StatusVisibility.PUBLIC)
    val statusBusiness = MutableLiveData(StatusBusiness.PRIVATE)
    val event = MutableLiveData<Event?>()
    var category: ProductType = ProductType.ALL
    var destination: String = ""
    var checkInResult: CheckInResult? = null
    var checkInResponse: CheckInResponse? = null
    var forceCheckIn: Boolean = false
    var editStatusId: Int = 0

    init {
        reset()
    }

    fun reset() {
        manualArrivalTime = null
        manualDepartureTime = null
        destinationStationId = 0
        arrivalTime = null
        tripId = ""
        lineName = ""
        startStationId = 0
        departureTime = null
        message.value = ""
        destination = ""
        toot.value = false
        chainToot.value = false
        statusVisibility.postValue(StatusVisibility.PUBLIC)
        statusBusiness.postValue(StatusBusiness.PRIVATE)
        event.postValue(null)
        checkInResult = null
        checkInResponse = null
        forceCheckIn = false
        editStatusId = 0
        category = ProductType.ALL
    }

    fun forceCheckIn(
        onCheckedIn: (Boolean) -> Unit = { }
    ) {
        forceCheckIn = true
        checkIn(onCheckedIn)
    }

    fun checkIn(
        onCheckedIn: (Boolean) -> Unit = { }
    ) {
        val checkInRequest = CheckInRequest(
            message.value ?: "",
            statusBusiness.value ?: StatusBusiness.PRIVATE,
            statusVisibility.value ?: StatusVisibility.PUBLIC,
            event.value?.id,
            toot.value ?: false,
            chainToot.value ?: false,
            tripId,
            lineName,
            startStationId,
            destinationStationId,
            departureTime ?: ZonedDateTime.now(),
             arrivalTime ?: ZonedDateTime.now(),
            forceCheckIn
        )
        TraewellingApi.checkInService.checkIn(checkInRequest)
            .enqueue(object: Callback<Data<CheckInResponse>> {
                override fun onResponse(
                    call: Call<Data<CheckInResponse>>,
                    response: Response<Data<CheckInResponse>>
                ) {
                    if (response.isSuccessful) {
                        checkInResult = CheckInResult.SUCCESSFUL
                        checkInResponse = response.body()?.data
                    } else {
                        checkInResult = when (response.code()) {
                            409 -> CheckInResult.CONFLICTED
                            else -> CheckInResult.ERROR
                        }
                    }
                    onCheckedIn(checkInResult == CheckInResult.SUCCESSFUL)
                }
                override fun onFailure(call: Call<Data<CheckInResponse>>, t: Throwable) {
                    Logger.captureException(t)
                    checkInResult = CheckInResult.ERROR
                    onCheckedIn(false)
                }
            })
    }

    fun updateCheckIn(successfulCallback: (Status) -> Unit) {
        TraewellingApi.checkInService.updateCheckIn(
            editStatusId,
            UpdateStatusRequest(
                message.value,
                statusBusiness.value ?: error("Invalid data"),
                statusVisibility.value ?: error("Invalid data"),
                destinationStationId,
                arrivalTime,
                manualDepartureTime,
                manualArrivalTime
            )
        ).enqueue(object : Callback<Data<Status>> {
            override fun onResponse(call: Call<Data<Status>>, response: Response<Data<Status>>) {
                val body = response.body()
                if (body != null) {
                    successfulCallback(body.data)
                    reset()
                }
            }

            override fun onFailure(call: Call<Data<Status>>, t: Throwable) {
                Log.e("CheckInViewModel", t.stackTraceToString())
            }
        })
    }
}
