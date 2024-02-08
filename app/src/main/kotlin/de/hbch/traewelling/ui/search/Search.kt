package de.hbch.traewelling.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.util.getStationNameWithRL100
import de.hbch.traewelling.util.useDebounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    modifier: Modifier = Modifier,
    initQuery: String = "",
    queryStations: Boolean = true,
    queryUsers: Boolean = true,
    homelandStation: Station? = null,
    recentStations: List<Station>? = null,
    onStationSelected: (String) -> Unit = { },
    onUserSelected: (User) -> Unit = { }
) {
    val searchInstruction =
        if (queryStations && queryUsers)
            stringResource(id = R.string.search_stop_users)
        else if (queryStations)
            stringResource(id = R.string.search_stops)
        else
            stringResource(id = R.string.search_users)

    val viewModel: SearchViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var query by rememberSaveable { mutableStateOf(initQuery) }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }

    query.useDebounce(
        onChange = {
            debouncedQuery = it
        },
        delayMillis = 500L
    )

    var active by remember { mutableStateOf(false) }

    var usersLoading by remember { mutableStateOf(false) }
    var stationsLoading by remember { mutableStateOf(false) }
    val isLoading by remember { derivedStateOf { usersLoading || stationsLoading } }
    var isLocating by remember { mutableStateOf(false) }

    val userResults = remember { mutableStateListOf<User>() }
    val stationResults = remember { mutableStateListOf<Station>() }

    val stationSelected: (Station) -> Unit = {
        active = false
        onStationSelected(it.name)
    }
    val userSelected: (User) -> Unit = {
        active = false
        onUserSelected(it)
    }

    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.isNotBlank()) {
            if (queryUsers) {
                usersLoading = true
                coroutineScope.launch {
                    userResults.clear()
                    val users = viewModel.searchUsers(debouncedQuery)
                    if (users != null) {
                        userResults.addAll(users.take(5))
                    }
                    usersLoading = false
                }
            }

            if (queryStations) {
                stationsLoading = true
                coroutineScope.launch {
                    val stations = viewModel.searchStations(debouncedQuery)
                    stationResults.clear()
                    if (stations != null) {
                        stationResults.addAll(stations)
                    }
                    stationsLoading = false
                }
            }
        } else {
            userResults.clear()
            stationResults.clear()
        }
    }

    if (isLocating) {
        RequestLocationPermissionAndLocation(
            locationReceivedAction = { location ->
                isLocating = false
                location?.let { loc ->
                    coroutineScope.launch {
                        isLocating = true
                        val station = viewModel.searchNearbyStation(loc)
                        if (station != null) {
                            stationSelected(station)
                        }
                        isLocating = false
                    }
                }
            }
        )
    }

    DockedSearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = {
            active = false
            onStationSelected(it)
        },
        active = active,
        onActiveChange = { active = it },
        modifier = modifier,
        placeholder = {
            Text(searchInstruction)
        },
        trailingIcon = {
            if (queryStations) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(onClick = { isLocating = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_locate),
                            contentDescription = stringResource(id = R.string.locate)
                        )
                    }
                }
            }
        }
    ) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 4.dp)
        ) {
            if (queryStations && queryUsers) {
                Text(
                    text = stringResource(id = R.string.stops),
                    style = AppTypography.titleLarge,
                    modifier = Modifier.padding(4.dp)
                )
            }
            if (debouncedQuery.isBlank()) {
                homelandStation?.let {
                    SearchItem(
                        item = it,
                        text = getStationNameWithRL100(it),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_home),
                                contentDescription = null
                            )
                        },
                        onClick = stationSelected
                    )
                }
                recentStations?.forEach {
                    SearchItem(
                        item = it,
                        text = getStationNameWithRL100(it),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_history),
                                contentDescription = null
                            )
                        },
                        onClick = stationSelected
                    )
                }
            }
            stationResults.forEach {
                SearchItem(
                    item = it,
                    text = getStationNameWithRL100(it),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_train),
                            contentDescription = null
                        )
                    },
                    onClick = stationSelected
                )
            }
            if (debouncedQuery.isNotBlank() && queryUsers && userResults.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    stringResource(id = R.string.users),
                    style = AppTypography.titleLarge,
                    modifier = Modifier.padding(4.dp)
                )
                userResults.forEach {
                    SearchItem(
                        item = it,
                        text = "${it.name} (@${it.username})",
                        icon = {
                            ProfilePicture(
                                user = it,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = userSelected
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestLocationPermissionAndLocation(locationReceivedAction: (Location?) -> Unit) {
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val locationManager =
        LocalContext.current.getSystemService(Context.LOCATION_SERVICE)

    DisposableEffect(Unit) {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationReceivedAction(location)
                if (locationManager is LocationManager) {
                    locationManager.removeUpdates(this)
                }
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (locationPermissionState.allPermissionsGranted) {
            if (locationManager is LocationManager) {
                val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    LocationManager.FUSED_PROVIDER
                else
                    LocationManager.GPS_PROVIDER

                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    locationListener
                )
            }
        } else {
            locationReceivedAction(null)
            locationPermissionState.launchMultiplePermissionRequest()
        }

        onDispose {
            if (locationManager is LocationManager) {
                locationManager.removeUpdates(locationListener)
            }
        }
    }
}

@Composable
private fun <T> SearchItem(
    item: T,
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { },
    onClick: (T) -> Unit = { }
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(text)
    }
}
