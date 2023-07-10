package de.hbch.traewelling.ui.searchConnection

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.getLastDestination
import de.hbch.traewelling.api.models.trip.HafasTrip
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.FilterChipGroup
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.selectDestination.getDelayColor
import de.hbch.traewelling.ui.selectDestination.getLocalTimeString
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@Composable
fun SearchConnection(
    loggedInUserViewModel: LoggedInUserViewModel,
    station: String,
    currentSearchDate: Date
) {
    val viewModel: SearchConnectionViewModel = viewModel()
    val searchStationCardViewModel: SearchStationCardViewModel = viewModel()
    var stationName by remember { mutableStateOf(station) }
    val scrollState = rememberScrollState()
    val trips = remember { mutableStateListOf<HafasTrip>() }
    val times by viewModel.pageTimes.observeAsState()
    var searchDate by remember { mutableStateOf(currentSearchDate) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(stationName, searchDate) {
        loading = true
        viewModel.searchConnections(
            stationName,
            searchDate,
            {
                loading = false
                trips.clear()
                trips.addAll(it.data)
            },
            { }
        )
    }

    Column(
        modifier = Modifier
            .animateContentSize()
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CardSearchStation(
            modifier = Modifier.padding(8.dp),
            searchAction = { station ->
                stationName = station
            },
            searchStationCardViewModel = searchStationCardViewModel,
            homelandStationData = loggedInUserViewModel.home,
            recentStationsData = loggedInUserViewModel.lastVisitedStations
        )
        ElevatedCard(
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    text = stringResource(id = R.string.departures_at, stationName),
                    style = AppTypography.headlineSmall
                )
                Divider(
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
                            }
                        },
                        onNextTime = {
                            val time = times?.next
                            time?.let {
                                searchDate = it
                            }
                        },
                        onTripSelection = { trip ->
                            /*TODO checkInViewModel.reset()
                            checkInViewModel.lineName = trip.line?.name ?: ""
                            checkInViewModel.tripId = trip.tripId
                            checkInViewModel.startStationId = trip.station?.id ?: -1
                            checkInViewModel.departureTime = trip.plannedDeparture

                            val action =
                                SearchConnectionFragmentDirections.actionSearchConnectionFragmentToSelectDestinationFragment(
                                    trip.tripId,
                                    trip.finalDestination
                                )
                            findNavController().navigate(action)*/
                        },
                        onTimeSelection = {
                            searchDate = it
                        },
                        onHomelandStationSelection = {
                            //TODO setHomelandStation(context, stationName)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchConnection(
    modifier: Modifier = Modifier,
    stationId: Int? = null,
    searchTime: Date = Date(),
    trips: List<HafasTrip>? = null,
    onPreviousTime: () -> Unit = { },
    onNextTime: () -> Unit = { },
    onTripSelection: (HafasTrip) -> Unit = { },
    onHomelandStationSelection: () -> Unit = { },
    onTimeSelection: (Date) -> Unit = { }
) {
    var selectedFilter by remember { mutableStateOf<FilterType?>(null) }
    val filteredTrips = remember { mutableStateListOf<HafasTrip>() }
    var datePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = searchTime.time)
    var timePickerVisible by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButtonWithIconAndText(
                        text = stringResource(id = R.string.ok),
                        onClick = {
                            timePickerVisible = false

                            val selectedDate = datePickerState.selectedDateMillis
                            val calendar = GregorianCalendar()
                            if (selectedDate != null) {
                                calendar.timeInMillis = selectedDate
                                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                calendar.set(Calendar.MINUTE, timePickerState.minute)

                                onTimeSelection(calendar.time)
                            }
                        }
                    )
                }
            }
        }
    }

    // Filter list on list or filter change
    LaunchedEffect(selectedFilter) {
        Log.d("Compose", "Data arrived, filtering")
        val filter = selectedFilter
        filteredTrips.clear()
        if (trips != null) {
            if (filter == null) {
                filteredTrips.addAll(trips)
            } else {
                filteredTrips.addAll(
                    trips.filter {
                        filter.matchesProduct(it.line?.product)
                    }
                )
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Time selection and home
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
        Divider(
            modifier = Modifier.fillMaxWidth()
        )

        // Filter chips
        FilterChipGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            chips = getFilterableProductTypes(trips),
            preSelection = null,
            selectionRequired = false,
            onSelectionChanged = { selectedFilter = it }
        )
        Divider(
            modifier = Modifier.fillMaxWidth()
        )

        // Previous/Next top
        PreviousNextButtons(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            nextSelected = onNextTime,
            previousSelected = onPreviousTime
        )
        Divider(
            modifier = Modifier.fillMaxWidth()
        )

        // Connections
        filteredTrips.forEach { trip ->
            ConnectionListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!trip.isCancelled) {
                            onTripSelection(trip)
                        }
                    }
                    .padding(8.dp),
                productType = trip.line?.product ?: ProductType.BUS,
                line = trip.line?.name ?: "",
                departurePlanned = trip.plannedDeparture ?: Date(),
                departureReal = trip.departure ?: trip.plannedDeparture,
                isCancelled = trip.isCancelled,
                destination = getLastDestination(trip),
                departureStation =
                    if (!trip.station?.name.isNullOrBlank() && stationId != null && trip.station?.id != stationId)
                        trip.station?.name
                    else
                        null
            )
            Divider(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Previous/Next bottom
        PreviousNextButtons(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            nextSelected = onNextTime,
            previousSelected = onPreviousTime
        )
    }
}

@Composable
fun ConnectionListItem(
    productType: ProductType,
    line: String,
    departurePlanned: Date,
    departureReal: Date?,
    isCancelled: Boolean,
    destination: String,
    departureStation: String?,
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = line,
                    fontWeight = FontWeight.ExtraBold
                )
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

@Composable
private fun getFilterableProductTypes(connections: List<HafasTrip>? = null): Map<FilterType, String> {
    val types = connections
        ?.mapNotNull {
            FilterType.values().find { filterType -> filterType.matchesProduct(it.line?.product) }
        }
        ?.distinct() ?: FilterType.values().toList()

    val typeMap = mutableMapOf<FilterType, String>()
    typeMap.putAll(
        types.map {
            Pair(it, stringResource(id = it.getStringId()))
        }
    )

    return typeMap
}

private enum class FilterType {
    EXPRESS {
        override val productMatches = listOf(
            ProductType.NATIONAL,
            ProductType.NATIONAL_EXPRESS
        )
        override fun getStringId() = R.string.product_type_express
    },
    REGIONAL {
        override val productMatches = listOf(
            ProductType.REGIONAL,
            ProductType.REGIONAL_EXPRESS
        )
        override fun getStringId() = R.string.product_type_regional
    },
    SUBURBAN {
        override val productMatches = listOf(
            ProductType.SUBURBAN
        )
        override fun getStringId() = R.string.product_type_suburban
    },
    SUBWAY {
        override val productMatches = listOf(
            ProductType.SUBWAY
        )
        override fun getStringId() = R.string.product_type_subway
    },
    TRAM {
        override val productMatches = listOf(
            ProductType.TRAM
        )
        override fun getStringId() = R.string.product_type_tram
    },
    BUS {
        override val productMatches = listOf(
            ProductType.BUS
        )
        override fun getStringId() = R.string.product_type_bus
    },
    FERRY {
        override val productMatches = listOf(
            ProductType.FERRY
        )
        override fun getStringId() = R.string.product_type_ferry
    };

    abstract val productMatches: List<ProductType>
    abstract fun getStringId(): Int

    fun matchesProduct(productType: ProductType?): Boolean = productMatches.contains(productType)
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
                line = "RE 75",
                departurePlanned = Date(),
                departureReal = Date(),
                isCancelled = false,
                destination = "Memmingen",
                departureStation = null
            )
            ConnectionListItem(
                productType = ProductType.TRAM,
                line = "STB U3",
                departurePlanned = Date(),
                departureReal = Date(),
                isCancelled = true,
                destination = "S-Vaihingen über Dachswald, Panoramabahn etc pp",
                departureStation = "Hauptbahnhof, Arnulf-Klett-Platz, einmal über den Fernwanderweg, rechts abbiegen, Treppe runter, dritter Bahnsteig rechts"
            )
        }
    }
}
