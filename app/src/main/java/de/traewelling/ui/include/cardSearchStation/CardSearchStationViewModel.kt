package de.traewelling.ui.include.cardSearchStation

class CardSearchStationViewModel {

    private var _requestLocationListener: () -> Unit = {}
    private var _searchConnectionsListener: () -> Unit = {}

    fun setRequestLocationListener(listener: () -> Unit) {
        _requestLocationListener = listener
    }

    fun setSearchConnectionsListener(listener: () -> Unit) {
        _searchConnectionsListener = listener
    }

    fun removeRequestLocationListener() {
        _requestLocationListener = {}
    }

    fun removeSearchConnectionsListener() {
        _searchConnectionsListener = {}
    }

    fun findNearbyStations() {
        _requestLocationListener()
    }

    fun searchConnections() {
        _searchConnectionsListener()
    }
}