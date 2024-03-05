package de.hbch.traewelling.ui.searchConnection

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasLine
import de.hbch.traewelling.api.models.trip.HafasTrip
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.BottomSearchViewModel
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.FilterChipGroup
import de.hbch.traewelling.ui.composables.LineIcon
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearch
import de.hbch.traewelling.util.getDelayColor
import de.hbch.traewelling.util.getLastDestination
import de.hbch.traewelling.util.getLocalTimeString
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId

@Composable
fun SearchConnection(
    loggedInUserViewModel: LoggedInUserViewModel,
    checkInViewModel: CheckInViewModel,
    bottomSearchViewModel: BottomSearchViewModel,
    station: String,
    currentSearchDate: ZonedDateTime,
    onTripSelected: () -> Unit = { },
    onHomelandSelected: (Station) -> Unit = { }
) {
    val viewModel: SearchConnectionViewModel = viewModel()
    var stationName by remember { mutableStateOf(station) }
    val scrollState = rememberScrollState()
    val trips = remember { mutableStateListOf<HafasTrip>() }
    val times by viewModel.pageTimes.observeAsState()
    var searchDate by remember { mutableStateOf(currentSearchDate) }
    var loading by remember { mutableStateOf(false) }
    var searchConnections by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf<FilterType?>(null) }

    LaunchedEffect(searchConnections, selectedFilter) {
        if (searchConnections) {
            loading = true
            viewModel.searchConnections(
                stationName,
                searchDate,
                selectedFilter,
                {
                    loading = false
                    searchConnections = false
                    trips.clear()
                    trips.addAll(it.data)
                    stationName = it.meta.station.name
                },
                { }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CardSearch(
            onStationSelected = { station ->
                stationName = station
                searchConnections = true
            },
            homelandStationData = loggedInUserViewModel.home,
            recentStationsData = loggedInUserViewModel.lastVisitedStations,
            queryUsers = false,
            bottomSearchViewModel = bottomSearchViewModel
        )
        ElevatedCard {
            Column {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.departures_at, stationName),
                    style = AppTypography.headlineSmall
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth()
                )
                if (loading) {
                    DataLoading()
                } else {
                    SearchConnection(
                        searchTime = searchDate,
                        trips = trips,
                        onPreviousTime = {
                            val time = times?.previous
                            time?.let {
                                searchDate = it
                                searchConnections = true
                            }
                        },
                        onNextTime = {
                            val time = times?.next
                            time?.let {
                                searchDate = it
                                searchConnections = true
                            }
                        },
                        onTripSelection = { trip ->
                            checkInViewModel.reset()
                            checkInViewModel.lineName = trip.line?.name ?: trip.line?.journeyNumber?.toString() ?: ""
                            checkInViewModel.lineId = trip.line?.id
                            checkInViewModel.operatorCode = trip.line?.operator?.id
                            checkInViewModel.tripId = trip.tripId
                            checkInViewModel.startStationId = trip.station?.id ?: -1
                            checkInViewModel.departureTime = trip.plannedDeparture
                            checkInViewModel.category = trip.line?.product ?: ProductType.ALL
                            checkInViewModel.origin = trip.station?.name ?: ""

                            onTripSelected()
                        },
                        onTimeSelection = {
                            searchDate = it
                            searchConnections = true
                        },
                        onHomelandStationSelection = {
                            viewModel.setUserHomelandStation(
                                station,
                                { s ->
                                    loggedInUserViewModel.setHomelandStation(s)
                                    onHomelandSelected(s)
                                },
                                {}
                            )
                        },
                        appliedFilter = selectedFilter,
                        onFilter = {
                            selectedFilter = it
                            searchConnections = true
                        }
                    )
                }
            }
        }
        Box { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchConnection(
    modifier: Modifier = Modifier,
    stationId: Int? = null,
    searchTime: ZonedDateTime = ZonedDateTime.now(),
    trips: List<HafasTrip>? = null,
    onPreviousTime: () -> Unit = { },
    onNextTime: () -> Unit = { },
    appliedFilter: FilterType? = null,
    onFilter: (FilterType?) -> Unit = { },
    onTripSelection: (HafasTrip) -> Unit = { },
    onHomelandStationSelection: () -> Unit = { },
    onTimeSelection: (ZonedDateTime) -> Unit = { }
) {
    var datePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = searchTime.toInstant().toEpochMilli()
    )
    var timePickerVisible by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = searchTime.hour,
        initialMinute = searchTime.minute
    )

    if (datePickerVisible) {
        Dialog(
            modifier = Modifier.fillMaxWidth(0.85f),
            onDismissRequest = { datePickerVisible = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(state = datePickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButtonWithIconAndText(
                        text = stringResource(id = R.string.ok),
                        onClick = {
                            datePickerVisible = false
                            timePickerVisible = true
                        }
                    )
                }
            }
        }
    }

    if (timePickerVisible) {
        Dialog(
            modifier = Modifier.fillMaxWidth(0.85f),
            onDismissRequest = { timePickerVisible = false }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButtonWithIconAndText(
                        text = stringResource(id = R.string.ok),
                        onClick = {
                            timePickerVisible = false

                            val selectedDate = datePickerState.selectedDateMillis
                            if (selectedDate != null) {
                                var dateTime = Instant
                                    .ofEpochMilli(selectedDate)
                                    .atZone(ZoneId.systemDefault())

                                dateTime = dateTime.withHour(timePickerState.hour)
                                dateTime = dateTime.withMinute(timePickerState.minute)

                                onTimeSelection(dateTime)
                            }
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val itemModifier = Modifier.padding(horizontal = 8.dp)
        // Time selection and home
        Row(
            modifier = itemModifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonWithIconAndText(
                drawableId = R.drawable.ic_time,
                text = getLocalTimeString(searchTime),
                onClick = {
                    datePickerVisible = true
                }
            )
            IconButton(onClick = onHomelandStationSelection) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = null
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )

        // Filter chips
        FilterChipGroup(
            modifier = itemModifier
                .fillMaxWidth(),
            chips = FilterType.entries.associateWith {
                 stringResource(id = it.stringId)
            },
            preSelection = appliedFilter,
            selectionRequired = false,
            onSelectionChanged = onFilter
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )

        // Previous/Next top
        PreviousNextButtons(
            modifier = itemModifier
                .fillMaxWidth(),
            nextSelected = onNextTime,
            previousSelected = onPreviousTime
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )

        // Connections
        trips?.forEach { trip ->
            ConnectionListItem(
                modifier = itemModifier
                    .fillMaxWidth()
                    .clickable {
                        if (!trip.isCancelled) {
                            onTripSelection(trip)
                        }
                    }
                    .padding(vertical = 8.dp),
                productType = trip.line?.product ?: ProductType.BUS,
                departurePlanned = trip.plannedDeparture ?: ZonedDateTime.now(),
                departureReal = trip.departure ?: trip.plannedDeparture,
                isCancelled = trip.isCancelled,
                destination = getLastDestination(trip),
                departureStation =
                    if (!trip.station?.name.isNullOrBlank() && stationId != null && trip.station?.id != stationId)
                        trip.station?.name
                    else
                        null,
                hafasLine = trip.line
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (trips.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.no_departures),
                modifier = itemModifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Previous/Next bottom
        PreviousNextButtons(
            modifier = itemModifier
                .fillMaxWidth(),
            nextSelected = onNextTime,
            previousSelected = onPreviousTime
        )
    }
}

@Composable
fun ConnectionListItem(
    productType: ProductType,
    departurePlanned: ZonedDateTime,
    departureReal: ZonedDateTime?,
    isCancelled: Boolean,
    destination: String,
    departureStation: String?,
    hafasLine: HafasLine?,
    modifier: Modifier = Modifier
) {
    val journeyNumber = hafasLine?.journeyNumber
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Product image, line and time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = productType.getIcon()),
                    contentDescription = stringResource(id = productType.getString())
                )
                LineIcon(
                    lineName = hafasLine?.name ?: "",
                    operatorCode = hafasLine?.operator?.id,
                    lineId = hafasLine?.id
                )

                if (journeyNumber != null && hafasLine.name?.contains(journeyNumber.toString()) == false) {
                    Text(
                        text = "($journeyNumber)",
                        style = AppTypography.bodySmall
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text =
                    if (isCancelled) stringResource(id = R.string.cancelled)
                    else getLocalTimeString(departureReal ?: departurePlanned),
                    color =
                    if (isCancelled) Color.Red
                    else getDelayColor(planned = departurePlanned, real = departureReal)
                )
                if (isCancelled) {
                    Text(
                        text = getLocalTimeString(
                            date = departurePlanned
                        ),
                        textDecoration = TextDecoration.LineThrough,
                        style = AppTypography.labelMedium
                    )
                }
            }
        }

        // Direction and departure from different station
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = destination,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (departureStation != null) {
                    Text(
                        text = stringResource(id = R.string.from_station, departureStation),
                        style = AppTypography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviousNextButtons(
    modifier: Modifier = Modifier,
    previousSelected: () -> Unit = { },
    nextSelected: () -> Unit = { }
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButtonWithIconAndText(
            onClick = previousSelected,
            text = stringResource(id = R.string.previous),
            drawableId = R.drawable.ic_previous
        )
        OutlinedButtonWithIconAndText(
            onClick = nextSelected,
            text = stringResource(id = R.string.next),
            drawableId = R.drawable.ic_next,
            drawableOnStart = false
        )
    }
}

enum class FilterType {
    EXPRESS {
        override val stringId = R.string.product_type_express
        override val filterQuery = "express"
    },
    REGIONAL {
        override val stringId = R.string.product_type_regional
        override val filterQuery = "regional"
    },
    SUBURBAN {
        override val stringId = R.string.product_type_suburban
        override val filterQuery = "suburban"
    },
    SUBWAY {
        override val stringId = R.string.product_type_subway
        override val filterQuery = "subway"
    },
    TRAM {
        override val stringId = R.string.product_type_tram
        override val filterQuery = "tram"
    },
    BUS {
        override val stringId = R.string.product_type_bus
        override val filterQuery = "bus"
    },
    FERRY {
        override val stringId = R.string.product_type_ferry
        override val filterQuery = "ferry"
    };

    abstract val stringId: Int
    abstract val filterQuery: String
}

@Preview
@Composable
fun SearchConnectionPreview() {
    MainTheme {
        SearchConnection()
    }
}

@Preview
@Composable
fun ConnectionListItemPreview() {
    MainTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ConnectionListItem(
                productType = ProductType.BUS,
                departurePlanned = ZonedDateTime.now(),
                departureReal = ZonedDateTime.now(),
                isCancelled = false,
                destination = "Memmingen",
                departureStation = null,
                hafasLine = null
            )
            ConnectionListItem(
                productType = ProductType.TRAM,
                departurePlanned = ZonedDateTime.now(),
                departureReal = ZonedDateTime.now(),
                isCancelled = true,
                destination = "S-Vaihingen über Dachswald, Panoramabahn etc pp",
                departureStation = "Hauptbahnhof, Arnulf-Klett-Platz, einmal über den Fernwanderweg, rechts abbiegen, Treppe runter, dritter Bahnsteig rechts",
                hafasLine = null
            )
        }
    }
}
