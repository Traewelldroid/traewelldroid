package de.hbch.traewelling.ui.include.cardSearchStation

import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSearchStation(
    modifier: Modifier = Modifier,
    homelandStationData: LiveData<Station?>,
    recentStationsData: LiveData<List<Station>?>,
    searchAction: (String) -> Unit = { },
    searchStationCardViewModel: SearchStationCardViewModel? = null
) {
    var shortcutsVisible by rememberSaveable { mutableStateOf(false) }
    var isLocating by rememberSaveable { mutableStateOf(false) }
    val recentStations by recentStationsData.observeAsState()
    val homelandStation by homelandStationData.observeAsState()
    var text by rememberSaveable { mutableStateOf("") }
    var autocompleteVisible by remember { mutableStateOf(false) }
    var autocompleteOptions by remember { mutableStateOf(listOf<String>()) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.where_are_you),
                style = AppTypography.headlineSmall
            )
            ExposedDropdownMenuBox(
                expanded = autocompleteVisible && autocompleteOptions.isNotEmpty(),
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = {
                        text = it
                        if (it.length > 3) {
                            searchStationCardViewModel?.autoCompleteStationSearch(
                                it,
                                { options ->
                                    autocompleteOptions = options
                                    autocompleteVisible = true
                                },
                                {}
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.input_stop)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            autocompleteVisible = false
                            // TODO remove delay after compose migration
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                shortcutsVisible = false
                                searchAction(text)
                            }, 500)
                        }
                    ),
                    trailingIcon = {
                        if (!recentStations.isNullOrEmpty() || homelandStation != null) {
                            IconButton(onClick = { shortcutsVisible = !shortcutsVisible }) {
                                AnimatedContent(shortcutsVisible) {
                                    val icon =
                                        if (it)
                                            R.drawable.ic_expand_less
                                        else
                                            R.drawable.ic_expand_more
                                    Icon(
                                        painterResource(id = icon),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                if (autocompleteVisible && autocompleteOptions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = autocompleteVisible,
                        onDismissRequest = { autocompleteVisible = false }
                    ) {
                        autocompleteOptions.take(5).forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option
                                    )
                                },
                                onClick = {
                                    autocompleteVisible = false
                                    shortcutsVisible = false
                                    // TODO remove delay after compose migration
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        searchAction(option)
                                    }, 500)
                                }
                            )
                        }
                    }
                }
            }
            AnimatedVisibility (shortcutsVisible && (!recentStations.isNullOrEmpty() || homelandStation != null)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                        if (homelandStation != null) {
                            HomeStationShortcut(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = {
                                        shortcutsVisible = false
                                        searchAction(homelandStation!!.name)
                                    })
                                    .padding(8.dp),
                                station = homelandStation!!
                            )
                        }
                        recentStations?.forEach { s ->
                            val shortcutModifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    shortcutsVisible = false
                                    searchAction(s.name)
                                })
                                .padding(8.dp)
                            LastVisitedStationShortcut(
                                modifier = shortcutModifier,
                                station = s
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButtonWithIconAndText(
                    stringId = R.string.locate,
                    drawableId = R.drawable.ic_locate,
                    isLoading = isLocating,
                    onClick = {
                        isLocating = true
                    }
                )
                if (isLocating) {
                    RequestLocationPermissionAndLocation(
                        locationReceivedAction = { location ->
                            isLocating = false
                            location?.let {
                                searchStationCardViewModel?.getNearbyStation(
                                    location.latitude,
                                    location.longitude,
                                    {
                                        searchAction(it)
                                    },
                                    {}
                                )
                            }
                        }
                    )
                }
                ButtonWithIconAndText(
                    stringId = R.string.search,
                    drawableId = R.drawable.ic_search,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    onClick = {
                        shortcutsVisible = false
                        searchAction(text)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestLocationPermissionAndLocation(locationReceivedAction: (Location?) -> Unit) {
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val locationManager =
        LocalContext.current.getSystemService(LOCATION_SERVICE)

    if (locationPermissionState.allPermissionsGranted) {
        if (locationManager is LocationManager) {
            val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                LocationManager.FUSED_PROVIDER
            else
                LocationManager.GPS_PROVIDER

            locationManager.getCurrentLocation(
                provider,
                null,
                LocalContext.current.mainExecutor
            ) { location ->
                locationReceivedAction(location)
            }
        }
    } else {
        locationReceivedAction(null)
        LaunchedEffect(true) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }
}

@Composable
private fun HomeStationShortcut(
    modifier: Modifier = Modifier,
    station: Station
) {
    StationShortcut(
        modifier = modifier,
        station = station,
        drawable = R.drawable.ic_home
    )
}

@Composable
private fun LastVisitedStationShortcut(
    modifier: Modifier = Modifier,
    station: Station
) {
    StationShortcut(
        modifier = modifier,
        station = station,
        drawable = R.drawable.ic_history
    )
}

@Composable
private fun StationShortcut(
    modifier: Modifier = Modifier,
    station: Station,
    @DrawableRes drawable: Int,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = drawable),
            contentDescription = null
        )
        var displayedName = station.name
        if (station.ds100 != null)
            displayedName = displayedName.plus(" [${station.ds100}]")
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = displayedName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(locale="de")
@Composable
private fun CardSearchStationPreview() {
    MainTheme {
        var search by remember { mutableStateOf("") }

        val searchAction: (String) -> Unit = {
            search = it
        }
        val stations = listOf(
            Station(
                42,
                "Memmingen",
                47.0,
                10.0,
                "123456",
                "MM"
            ),
            Station(
                43,
                "Kempten(Allgäu)Hbf",
                47.0,
                10.0,
                "123456",
                "MKP"
            ),
            Station(
                43,
                "Stuttgart Hbf",
                47.0,
                10.0,
                "123456",
                "TS"
            ),
            Station(
                43,
                "Mannheim Hbf",
                47.0,
                10.0,
                "123456",
                "RM"
            ),
            Station(
                43,
                "Frankfurt (Main) Flughafen Regionalbahnhof",
                47.0,
                10.0,
                "123456",
                "FFLU"
            ),
            Station(
                43,
                "Berlin Gesundbrunnen",
                47.0,
                10.0,
                "123456",
                "BGS"
            )
        )

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            CardSearchStation(
                searchAction = searchAction,
                homelandStationData = MutableLiveData(stations[0]),
                recentStationsData = MutableLiveData(stations)
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = search
            )
        }
    }
}

@Preview
@Composable
private fun StationShortcutPreview() {
    MainTheme {
        val station = Station(
            42,
            "Station Hbf",
            47.0,
            10.0,
            "123456",
            "ZSHB"
        )

        StationShortcut(
            modifier = Modifier.fillMaxWidth(),
            station = station,
            drawable = R.drawable.ic_home
        )
    }
}
