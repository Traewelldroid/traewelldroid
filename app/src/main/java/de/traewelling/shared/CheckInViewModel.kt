package de.traewelling.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class CheckInViewModel : ViewModel() {

    var lineName: String = ""
    var tripId: String = ""
    var startStationId: Int = 0
    var destinationStationId: Int = 0
    var arrivalTime: Date? = null
    var departureTime: Date? = null

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
    }
}