package de.hbch.traewelling.ui.user

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
import androidx.compose.runtime.LaunchedEffect
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
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import java.util.Date

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Profile(
    username: String?,
    loggedInUserViewModel: LoggedInUserViewModel,
    stationSelectedAction: (String, Date?) -> Unit = { _, _ -> },
    statusSelectedAction: (Int) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    statusEditAction: (Status) -> Unit = { }
) {
    val user = username ?: loggedInUserViewModel.loggedInUser.value?.username
    var currentPage by remember { mutableStateOf(1) }
    val userStatusViewModel: UserStatusViewModel = viewModel()
    val checkInCardViewModel: CheckInCardViewModel = viewModel()
    val refreshing by userStatusViewModel.isRefreshing.observeAsState(false)
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            currentPage = 1
            userStatusViewModel.loadUser(user)
        }
    )
    val listState = rememberLazyListState()
    var initialized by remember { mutableStateOf(false) }

    listState.onBottomReached {
        if (userStatusViewModel.checkIns.size > 0) {
            userStatusViewModel.loadStatusesForUser(page = ++currentPage)
        }
    }

    LaunchedEffect(Unit) {
        if (!initialized) {
            userStatusViewModel.loadUser(user)
            initialized = true
        }
    }

    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            userScrollEnabled = true,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState
        ) {
            item {
                UserCard(
                    userViewModel = userStatusViewModel,
                    loggedInUserViewModel = loggedInUserViewModel
                )
            }
            items(
                items = userStatusViewModel.checkIns
            ) { status ->
                CheckInCard(
                    checkInCardViewModel = checkInCardViewModel,
                    status = status.toStatusDto(),
                    loggedInUserViewModel = loggedInUserViewModel,
                    stationSelected = stationSelectedAction,
                    statusSelected = statusSelectedAction,
                    handleEditClicked = statusEditAction,
                    onDeleted = { statusValue ->
                        userStatusViewModel.checkIns.removeIf { it.id == statusValue.statusId }
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
