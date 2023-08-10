package de.hbch.traewelling.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.statistics.DailyStatistics
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.ui.include.status.getFormattedDistance
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.checkInList

@Composable
fun DailyStatistics(
    date: String,
    loggedInUserViewModel: LoggedInUserViewModel,
    statusSelectedAction: (Int) -> Unit,
    statusEditAction: (de.hbch.traewelling.api.dtos.Status) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: StatisticsViewModel = viewModel()
    var statsRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statistics by remember { mutableStateOf<DailyStatistics?>(null) }

    LaunchedEffect(statsRequested) {
        if (!statsRequested && statistics == null) {
            viewModel.getDailyStatistics(
                date,
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
            modifier = modifier,
            statusSelectedAction = statusSelectedAction,
            statusEditAction = statusEditAction
        )
    }
    if (isLoading) {
        DataLoading()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyStatisticsView(
    statistics: DailyStatistics,
    loggedInUserViewModel: LoggedInUserViewModel,
    modifier: Modifier = Modifier,
    statusSelectedAction: (Int) -> Unit = { },
    statusEditAction: (de.hbch.traewelling.api.dtos.Status) -> Unit = { }
) {
    val checkInCardViewModel: CheckInCardViewModel = viewModel()
    val checkIns = remember { mutableStateListOf<Status>() }
    checkIns.addAll(statistics.statuses)
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
        }
        checkInList(
            checkIns,
            checkInCardViewModel,
            loggedInUserViewModel,
            statusSelectedAction = statusSelectedAction,
            statusEditAction = statusEditAction,
            showDate = false
        )
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
    val statistics = de.hbch.traewelling.api.models.statistics.DailyStatistics(
        listOf(),
        4711,
        815,
        42
    )
    val loggedInUserViewModel = LoggedInUserViewModel()
    MainTheme {
        DailyStatisticsView(
            statistics = statistics,
            loggedInUserViewModel = loggedInUserViewModel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
