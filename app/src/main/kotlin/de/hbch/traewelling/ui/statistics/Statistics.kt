package de.hbch.traewelling.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ColumnChart
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.FilterChipGroup
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.getDateRangeString
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Statistics(
    modifier: Modifier = Modifier
) {
    val statisticsViewModel: StatisticsViewModel = viewModel()
    val statistics by statisticsViewModel.statistics.observeAsState()
    val selectedDateRange by statisticsViewModel.dateRange.observeAsState()
    var selectedUnit by remember { mutableStateOf(StatisticsUnit.CHECK_IN_COUNT) }
    var selectedType by remember { mutableStateOf(StatisticsType.TRANSPORT_TYPES) }
    var dateRangePickerVisible by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    val chartEntries = remember { mutableStateListOf<Pair<String, Int>>() }
    val chartInput = remember {
        mutableStateOf<Pair<List<Pair<String, Int>>, @Composable (Int) -> String>>(
            Pair(listOf()) { "" }
        )
    }
    val context = LocalContext.current

    LaunchedEffect(selectedDateRange) {
        val range = selectedDateRange
        if (range != null) {
            statisticsViewModel.getPersonalStatisticsForSelectedTimeRange()
            dateRangePickerState.setSelection(
                range.first.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                LocalDateTime.of(range.second, LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
    }

    LaunchedEffect(statistics, selectedUnit, selectedType) {
        chartEntries.clear()
        when (selectedType) {
            StatisticsType.TRANSPORT_TYPES -> statistics?.categories
            StatisticsType.OPERATORS -> statistics?.operators
            StatisticsType.TRAVEL_PURPOSE -> statistics?.purposes
        }?.forEach { stat ->
            chartEntries.add(
                Pair(stat.getLabel(context), when(selectedUnit) {
                    StatisticsUnit.CHECK_IN_COUNT -> stat.checkInCount
                    StatisticsUnit.TRAVEL_TIME -> stat.duration
                })
            )
        }
        chartInput.value = Pair(
            chartEntries.sortedByDescending { it.second }
        ) { value ->
            selectedUnit.formatValue(value)
        }
    }

    if (dateRangePickerVisible) {
        Dialog(
            onDismissRequest = { dateRangePickerVisible = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangePicker(
                    modifier = Modifier.weight(1f),
                    state = dateRangePickerState
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            statisticsViewModel.dateRange.postValue(
                                Pair(
                                    Instant
                                        .ofEpochMilli(dateRangePickerState.selectedStartDateMillis!!)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate(),
                                    Instant
                                        .ofEpochMilli((dateRangePickerState.selectedEndDateMillis!!))
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                )
                            )
                            dateRangePickerVisible = false
                        },
                        enabled = dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        Text(
                            text = stringResource(id = R.string.ok)
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        OutlinedButtonWithIconAndText(
            modifier = Modifier.fillMaxWidth(),
            text = getDateRangeString(range = selectedDateRange),
            drawableId = R.drawable.ic_calendar,
            onClick = {
                dateRangePickerVisible = true
            }
        )
        val unitMap = mutableMapOf<StatisticsUnit, String>()
        unitMap.putAll(
            StatisticsUnit.values().map {
                Pair(it, stringResource(id = it.getStringId()))
            }
        )
        val typeMap = mutableMapOf<StatisticsType, String>()
        typeMap.putAll(
            StatisticsType.values().map {
                Pair(it, stringResource(id = it.getStringId()))
            }
        )
        FilterChipGroup(
            modifier = Modifier.fillMaxWidth(),
            chips = unitMap,
            preSelection = StatisticsUnit.CHECK_IN_COUNT,
            onSelectionChanged = { selectedUnit = it!! },
            selectionRequired = true
        )
        FilterChipGroup(
            modifier = Modifier.fillMaxWidth(),
            chips = typeMap,
            preSelection = StatisticsType.TRANSPORT_TYPES,
            onSelectionChanged = { selectedType = it!! },
            selectionRequired = true
        )

        // Chart, weight 1f
        ColumnChart(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .weight(1f),
            input = chartInput.value
        )
    }
}

enum class StatisticsUnit {
    CHECK_IN_COUNT {
        override fun getStringId() = R.string.check_in_count
        @Composable
        override fun formatValue(value: Int) = "${value}x"
    },
    TRAVEL_TIME {
        override fun getStringId() = R.string.travel_time
        @Composable
        override fun formatValue(value: Int) = getDurationString(value)
    };

    abstract fun getStringId(): Int
    @Composable
    abstract fun formatValue(value: Int): String
}

enum class StatisticsType {
    TRANSPORT_TYPES {
        override fun getStringId() = R.string.transport_types
    },
    OPERATORS {
        override fun getStringId() = R.string.operators
    },
    TRAVEL_PURPOSE {
        override fun getStringId() = R.string.travel_purpose
    };

    abstract fun getStringId(): Int
}

@Preview
@Composable
private fun StatisticsPreview() {
    MainTheme {
        Statistics()
    }
}
