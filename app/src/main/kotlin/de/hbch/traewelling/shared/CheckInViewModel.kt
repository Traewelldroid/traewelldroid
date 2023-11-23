package de.hbch.traewelling.shared

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.TrwlCheckInRequest
import de.hbch.traewelling.api.models.status.TrwlCheckInResponse
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.status.TrwlCheckInUpdateRequest
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.providers.checkin.CheckInResult
import de.hbch.traewelling.providers.checkin.traewelling.TrwlCheckInProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime

class CheckInViewModel : ViewModel() {
    val trwlProvider: TrwlCheckInProvider

    var lineName: String = ""
    var lineId: String? = null
    var operatorCode: String? = null
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
    var trwlCheckInResponse: TrwlCheckInResponse? = null
    var forceCheckIn: Boolean = false
    var editStatusId: Int = 0

    init {
        trwlProvider = TrwlCheckInProvider()
        reset()
    }

    fun reset() {
        manualArrivalTime = null
        manualDepartureTime = null
        destinationStationId = 0
        arrivalTime = null
        tripId = ""
        lineName = ""
        operatorCode = null
        lineId = null
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
        trwlCheckInResponse = null
        forceCheckIn = false
        editStatusId = 0
        category = ProductType.ALL
    }

    suspend fun forceCheckIn(
        onCheckedIn: (Boolean) -> Unit = { }
    ) {
        forceCheckIn = true
        checkIn(onCheckedIn)
    }

    suspend fun checkIn(
        onCheckedIn: (Boolean) -> Unit = { }
    ) {
        val trwlCheckInRequest = TrwlCheckInRequest(
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

        val result = trwlProvider.checkIn(trwlCheckInRequest)
        checkInResult = result.result
        trwlCheckInResponse = result.data

        onCheckedIn(result.result == CheckInResult.SUCCESSFUL)
        /*TraewellingApi.checkInService.checkIn(trwlCheckInRequest)
            .enqueue(object: Callback<Data<TrwlCheckInResponse>> {
                override fun onResponse(
                    call: Call<Data<TrwlCheckInResponse>>,
                    response: Response<Data<TrwlCheckInResponse>>
                ) {
                    if (response.isSuccessful) {
                        checkInResult = CheckInResult.SUCCESSFUL
                        trwlCheckInResponse = response.body()?.data
                    } else {
                        checkInResult = when (response.code()) {
                            409 -> CheckInResult.CONFLICTED
                            else -> CheckInResult.ERROR
                        }
                    }
                    onCheckedIn(checkInResult == CheckInResult.SUCCESSFUL)
                }
                override fun onFailure(call: Call<Data<TrwlCheckInResponse>>, t: Throwable) {
                    Logger.captureException(t)
                    checkInResult = CheckInResult.ERROR
                    onCheckedIn(false)
                }
            })*/
    }

    fun updateCheckIn(successfulCallback: (Status) -> Unit) {
        TraewellingApi.checkInService.updateCheckIn(
            editStatusId,
            TrwlCheckInUpdateRequest(
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
