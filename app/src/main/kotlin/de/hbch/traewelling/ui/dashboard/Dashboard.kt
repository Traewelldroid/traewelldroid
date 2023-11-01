package de.hbch.traewelling.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.composables.NotificationsAvailableHint
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearch
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.util.OnBottomReached
import de.hbch.traewelling.util.checkInList
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Dashboard(
    loggedInUserViewModel: LoggedInUserViewModel,
    searchConnectionsAction: (String, ZonedDateTime?) -> Unit = { _, _ -> },
    userSelectedAction: (String) -> Unit = { },
    statusSelectedAction: (Int) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    statusEditAction: (Status) -> Unit = { },
    knowsAboutNotifications: Boolean = true,
    notificationHintClosed: () -> Unit = { }
) {
    val dashboardViewModel: DashboardFragmentViewModel = viewModel()
    val checkInCardViewModel : CheckInCardViewModel = viewModel()
    val refreshing by dashboardViewModel.isRefreshing.observeAsState(false)
    val checkIns = remember { dashboardViewModel.checkIns }
    var currentPage by remember { mutableIntStateOf(1) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            currentPage = 1
            dashboardViewModel.refresh()
        }
    )
    val checkInListState = rememberLazyListState()

    checkInListState.OnBottomReached {
        if (dashboardViewModel.checkIns.size > 0) {
            dashboardViewModel.loadCheckIns(++currentPage)
        } else {
            loggedInUserViewModel.getLoggedInUser()
            loggedInUserViewModel.getLastVisitedStations {  }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            userScrollEnabled = true,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = checkInListState
        ) {
            item {
                CardSearch(
                    onStationSelected = { station ->
                        searchConnectionsAction(station, null)
                    },
                    homelandStationData = loggedInUserViewModel.home,
                    recentStationsData = loggedInUserViewModel.lastVisitedStations,
                    onUserSelected = {
                        userSelectedAction(it.username)
                    }
                )
            }
            if (!knowsAboutNotifications) {
                item {
                    NotificationsAvailableHint(
                        loggedInUserViewModel = loggedInUserViewModel,
                        onClose = notificationHintClosed
                    )
                }
            }
            checkInList(
                checkIns,
                checkInCardViewModel,
                loggedInUserViewModel,
                searchConnectionsAction,
                statusSelectedAction,
                statusEditAction,
                statusDeletedAction,
                userSelectedAction
            )
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState
        )
    }
}
