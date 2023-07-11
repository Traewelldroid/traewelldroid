package de.hbch.traewelling.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import java.util.Date

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Dashboard(
    loggedInUserViewModel: LoggedInUserViewModel,
    searchConnectionsAction: (String, Date?) -> Unit = { _, _ -> },
    userSelectedAction: (String) -> Unit = { },
    statusSelectedAction: (Int) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    statusEditAction: (Status) -> Unit = { }
) {
    val dashboardViewModel: DashboardFragmentViewModel = viewModel()
    val searchStationCardViewModel: SearchStationCardViewModel = viewModel()
    val checkInCardViewModel : CheckInCardViewModel = viewModel()
    val refreshing by dashboardViewModel.isRefreshing.observeAsState(false)
    val checkIns = remember { dashboardViewModel.checkIns }
    var currentPage by remember { mutableStateOf(1) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            currentPage = 1
            dashboardViewModel.refresh()
        }
    )
    val checkInListState = rememberLazyListState()

    checkInListState.onBottomReached {
        if (dashboardViewModel.checkIns.size > 0) {
            dashboardViewModel.loadCheckIns(++currentPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            userScrollEnabled = true,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = checkInListState
        ) {
            item {
                CardSearchStation(
                    searchAction = { station ->
                        searchConnectionsAction(station, null)
                    },
                    searchStationCardViewModel = searchStationCardViewModel,
                    homelandStationData = loggedInUserViewModel.home,
                    recentStationsData = loggedInUserViewModel.lastVisitedStations
                )
            }

            items(
                items = checkIns
            ) { status ->
                CheckInCard(
                    checkInCardViewModel = checkInCardViewModel,
                    status = status.toStatusDto(),
                    loggedInUserViewModel = loggedInUserViewModel,
                    stationSelected = searchConnectionsAction,
                    userSelected = userSelectedAction,
                    statusSelected = statusSelectedAction,
                    handleEditClicked = statusEditAction,
                    onDeleted = { statusValue ->
                        checkIns.removeIf { it.id == statusValue.statusId }
                        statusDeletedAction()
                    }
                )
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState
        )
    }
}
