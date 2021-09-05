package de.traewelling.ui.include.cardSearchStation

class CardSearchStationViewModel {

    private lateinit var _requestLocationListener: () -> Unit

    fun setRequestLocationListener(listener: () -> Unit) {
        _requestLocationListener = listener
    }

    fun removeRequestLocationListener() {
        _requestLocationListener = {}
    }

    fun findNearbyStations() {
        _requestLocationListener()
    }
}