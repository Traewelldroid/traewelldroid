package de.hbch.traewelling.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.polyline.FeatureCollection
import de.hbch.traewelling.api.models.statistics.DailyStatistics
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.theme.PolylineColor
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.OpenRailwayMapView
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.getBoundingBoxFromPolyLines
import de.hbch.traewelling.ui.composables.getPolyLinesFromFeatureCollection
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.ui.include.status.getFormattedDistance
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.getLongLocalDateString
import org.osmdroid.views.MapView
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DailyStatistics(
    date: LocalDate,
    loggedInUserViewModel: LoggedInUserViewModel,
    statusSelectedAction: (Int) -> Unit,
    statusEditAction: (de.hbch.traewelling.api.dtos.Status) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: StatisticsViewModel = viewModel()
    var statsRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statistics by remember { mutableStateOf<DailyStatistics?>(null) }

    var localDate by rememberSaveable { mutableStateOf(date) }

    LaunchedEffect(localDate) {
        if (statsRequested) {
            statistics = null
            statsRequested = false
        }
    }

    LaunchedEffect(statsRequested) {
        if (!statsRequested && statistics == null) {
            viewModel.getDailyStatistics(
                localDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                {
                    statistics = it
                    isLoading = false
                },
                {
                    isLoading = false
                }
            )
            statsRequested = true
            isLoading = true
        }
    }

    statistics?.let {
        DailyStatisticsView(
            statistics = it,
            loggedInUserViewModel = loggedInUserViewModel,
            date = localDate,
            modifier = modifier,
            statusSelectedAction = statusSelectedAction,
            statusEditAction = statusEditAction,
            dateSelectedAction = { date ->
                localDate = date
            }
        )
    }
    if (isLoading) {
        DataLoading()
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DailyStatisticsView(
    statistics: DailyStatistics,
    loggedInUserViewModel: LoggedInUserViewModel,
    date: LocalDate,
    modifier: Modifier = Modifier,
    statusSelectedAction: (Int) -> Unit = { },
    statusEditAction: (de.hbch.traewelling.api.dtos.Status) -> Unit = { },
    dateSelectedAction: (LocalDate) -> Unit = { }
) {
    val checkInCardViewModel: CheckInCardViewModel = viewModel()
    val checkIns = remember { mutableStateListOf<Status>().also {
        it.addAll(statistics.statuses)
    } }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var datePickerVisible by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val ms = date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    datePickerState.setSelection(ms)

    if (datePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerVisible = false
                        val millis = datePickerState.selectedDateMillis ?: Instant.now().toEpochMilli()
                        val zdt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                        dateSelectedAction(
                            LocalDate.from(zdt.toLocalDate())
                        )
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    var columnModifier = modifier
    if (selectedTab == 0)
        columnModifier = columnModifier.verticalScroll(rememberScrollState())

    Column(
        modifier = columnModifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ButtonWithIconAndText(
            text = getLongLocalDateString(date.atStartOfDay(ZoneId.systemDefault())),
            drawableId = R.drawable.ic_calendar_checked,
            onClick = {
                datePickerVisible = true
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButtonWithIconAndText(
                text = stringResource(id = R.string.previous),
                drawableId = R.drawable.ic_previous,
                onClick = {
                    dateSelectedAction(date.minusDays(1))
                }
            )
            if (date != LocalDate.now()) {
                OutlinedButtonWithIconAndText(
                    text = stringResource(id = R.string.next),
                    drawableId = R.drawable.ic_next,
                    drawableOnStart = false,
                    onClick = {
                        dateSelectedAction(date.plusDays(1))
                    }
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = stringResource(id = R.string.check_ins)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = stringResource(id = R.string.map_view)
                    )
                },
                enabled = (statistics.featureCollection.features?.size?.compareTo(0) ?: -1) > 0
            )
        }
        if (selectedTab == 0) {
            FlowRow(
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Fact(
                    icon = R.drawable.ic_train,
                    text = pluralStringResource(
                        id = R.plurals.journeys,
                        count = statistics.count,
                        statistics.count
                    )
                )
                Fact(
                    icon = R.drawable.ic_score,
                    text = stringResource(id = R.string.display_points, statistics.points),
                    iconEnd = true
                )
                Fact(
                    icon = R.drawable.ic_travel_time,
                    text = getDurationString(duration = statistics.duration)
                )
                Fact(
                    icon = R.drawable.ic_navigation,
                    text = getFormattedDistance(distance = statistics.distance),
                    iconEnd = true
                )
            }
            checkIns.forEach { checkIn ->
                CheckInCard(
                    checkInCardViewModel = checkInCardViewModel,
                    status = checkIn.toStatusDto(),
                    loggedInUserViewModel = loggedInUserViewModel,
                    statusSelected = statusSelectedAction,
                    handleEditClicked = statusEditAction
                )
            }
        } else {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                var map: MapView? = remember { null }

                val polyLines = getPolyLinesFromFeatureCollection(
                    statistics.featureCollection,
                    PolylineColor.toArgb()
                )
                val bounds = getBoundingBoxFromPolyLines(polyLines)

                LaunchedEffect(true) {
                    map?.overlayManager?.overlays()?.addAll(polyLines)
                    map?.zoomToBoundingBox(bounds.increaseByScale(1.1f), false)
                }
                OpenRailwayMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    onInit = {
                        map = it
                    }
                )
            }
        }
        Box { }
    }
}

@Composable
private fun Fact(
    icon: Int,
    text: String,
    iconEnd: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        if (!iconEnd) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        Text(
            text = text,
            style = AppTypography.titleLarge
        )
        if (iconEnd) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Preview
@Composable
private fun DailyStatisticsPreview() {
    val statistics = DailyStatistics(
        listOf(),
        4711,
        815,
        42,
        FeatureCollection(null, null)
    )
    val loggedInUserViewModel = LoggedInUserViewModel()
    MainTheme {
        DailyStatisticsView(
            statistics = statistics,
            loggedInUserViewModel = loggedInUserViewModel,
            modifier = Modifier.fillMaxWidth(),
            date = LocalDate.now()
        )
    }
}
