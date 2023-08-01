package de.hbch.traewelling.util

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.ui.selectDestination.getLongLocalDateString
import de.hbch.traewelling.ui.user.isSameDay
import java.util.Date

fun NavHostController.popBackStackAndNavigate(
    route: String,
    launchSingleTop: Boolean = true,
    popUpToInclusive: Boolean = true
) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = popUpToInclusive
        }
        this.launchSingleTop = launchSingleTop
    }
}

fun LazyListScope.checkInList(
    checkIns: SnapshotStateList<Status>,
    checkInCardViewModel: CheckInCardViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    stationSelectedAction: (String, Date?) -> Unit = { _, _ -> },
    statusSelectedAction: (Int) -> Unit = { },
    statusEditAction: (de.hbch.traewelling.api.dtos.Status) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    userSelectedAction: (String) -> Unit = { }
) {
    itemsIndexed(
        items = checkIns
    ) { index, status ->
        val previousStatus = checkIns.getOrNull(index - 1)
        if (
            previousStatus == null ||
            !isSameDay(
                previousStatus.journey.origin.departurePlanned,
                status.journey.origin.departurePlanned
            )
        ) {
            Text(
                text = getLongLocalDateString(status.journey.origin.departurePlanned),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        CheckInCard(
            checkInCardViewModel = checkInCardViewModel,
            status = status.toStatusDto(),
            loggedInUserViewModel = loggedInUserViewModel,
            stationSelected = stationSelectedAction,
            statusSelected = statusSelectedAction,
            handleEditClicked = statusEditAction,
            onDeleted = { statusValue ->
                checkIns.removeIf { it.id == statusValue.statusId }
                statusDeletedAction()
            },
            userSelected = userSelectedAction
        )
    }
}


@Composable
fun LazyListState.OnBottomReached(
    loadMore : () -> Unit
){
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore){
        snapshotFlow { shouldLoadMore.value }
            .collect {
                if (it) loadMore()
            }
    }
}
