package de.hbch.traewelling.ui.search

import android.location.Location
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.user.User

class SearchViewModel : ViewModel() {
    suspend fun searchUsers(
        query: String,
        page: Int = 1
    ): List<User>? {
        return try {
            TraewellingApi.userService.searchUsers(query, page).data
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchStations(
        query: String
    ): List<Station>? {
        return try {
            val stations = TraewellingApi.travelService.autoCompleteStationSearch(query).data
            stations.sortedWith(compareBy(nullsLast()) { it.ds100 }).take(5)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchNearbyStation(
        location: Location
    ): Station? {
        return try {
            TraewellingApi.travelService.getNearbyStation(location.latitude, location.longitude).data
        } catch (_: Exception) {
            null
        }
    }
}
