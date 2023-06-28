package de.hbch.traewelling.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.FilterChipGroup
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.selectDestination.getLocalDateString
import java.util.Date

@Composable
fun Statistics(
    modifier: Modifier = Modifier,
    statisticsViewModel: StatisticsViewModel
) {
    val statistics by statisticsViewModel.statistics.observeAsState()
    val selectedDateRange by statisticsViewModel.dateRange.observeAsState()
    var selectedUnit by remember { mutableStateOf(StatisticsUnit.CHECK_IN_COUNT) }
    var selectedType by remember { mutableStateOf(StatisticsType.TRANSPORT_TYPES) }
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    val chartEntries = remember { mutableStateListOf<ChartEntry>() }

    LaunchedEffect(statistics) {
        chartEntries.clear()
        when (selectedType) {
            StatisticsType.TRANSPORT_TYPES -> statistics?.categories
            StatisticsType.OPERATORS -> statistics?.operators
            StatisticsType.TRAVEL_PURPOSE -> statistics?.purposes
        }?.forEachIndexed { i, stat ->
            chartEntries.add(
                entryOf(i, when(selectedUnit) {
                    StatisticsUnit.CHECK_IN_COUNT -> stat.checkInCount
                    StatisticsUnit.TRAVEL_TIME -> stat.duration
                })
            )
        }
        chartEntryModelProducer.setEntries(chartEntries)
    }

    Column(
        modifier = modifier
    ) {
        OutlinedButtonWithIconAndText(
            modifier = Modifier.fillMaxWidth(),
            text = getDateRangeString(range = selectedDateRange),
            drawableId = R.drawable.ic_calendar,
            onClick = {}
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
            chips = unitMap,
            preSelection = StatisticsUnit.CHECK_IN_COUNT,
            onSelectionChanged = { selectedUnit = it }
        )
        FilterChipGroup(
            chips = typeMap,
            preSelection = StatisticsType.TRANSPORT_TYPES,
            onSelectionChanged = { selectedType = it }
        )

        // Chart, weight 1f
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chart = columnChart(),
            chartModelProducer = chartEntryModelProducer,
            //legend = verticalLegend()
        )
    }
}

@Composable
fun getDateRangeString(range: Pair<Date, Date>?): String {
    if (range == null)
        return ""
    return stringResource(
        id = R.string.date_range,
        getLocalDateString(date = range.first),
        getLocalDateString(date = range.second)
    )
}

enum class StatisticsUnit {
    CHECK_IN_COUNT {
        override fun getStringId() = R.string.check_in_count
    },
    TRAVEL_TIME {
        override fun getStringId() = R.string.travel_time
    };

    abstract fun getStringId(): Int
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
    val statisticsViewModel = StatisticsViewModel()
    MainTheme {
        Statistics(
            statisticsViewModel = statisticsViewModel
        )
    }
}
