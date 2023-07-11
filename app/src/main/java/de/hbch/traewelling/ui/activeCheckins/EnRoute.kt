package de.hbch.traewelling.ui.activeCheckins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EnRoute(
    loggedInUserViewModel: LoggedInUserViewModel,
    userSelectedAction: (String) -> Unit = { },
    statusSelectedAction: (Int) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    statusEditAction: (Status) -> Unit = { }
) {
    val viewModel: ActiveCheckinsViewModel = viewModel()
    val checkInCardViewModel: CheckInCardViewModel = viewModel()

    val refreshing by viewModel.isRefreshing.observeAsState(false)
    val checkIns = remember { viewModel.checkIns }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            viewModel.getActiveCheckins()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = true
        ) {
            items(
                items = checkIns
            ) { status ->
                CheckInCard(
                    checkInCardViewModel = checkInCardViewModel,
                    status = status.toStatusDto(),
                    loggedInUserViewModel = loggedInUserViewModel,
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
