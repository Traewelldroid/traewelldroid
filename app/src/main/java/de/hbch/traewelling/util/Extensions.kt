package de.hbch.traewelling.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.FeatureFlags
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
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
    userSelectedAction: (String) -> Unit = { },
    showDailyStatisticsLink: Boolean = false,
    dailyStatisticsSelectedAction: (Date) -> Unit = { },
    showDate: Boolean = true
) {
    @Suppress("UNUSED_VARIABLE") val featureFlags = FeatureFlags.getInstance()

    itemsIndexed(
        items = checkIns
    ) { index, status ->
        val previousStatus = checkIns.getOrNull(index - 1)
        if (
            showDate &&
            (
                previousStatus == null ||
                !isSameDay(
                    previousStatus.journey.origin.departurePlanned,
                    status.journey.origin.departurePlanned
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLongLocalDateString(status.journey.origin.departurePlanned),
                    modifier = Modifier
                        .weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTypography.titleLarge
                )
                if (showDailyStatisticsLink) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_score),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            dailyStatisticsSelectedAction(status.journey.origin.departurePlanned)
                        })
                    )
                }
            }
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
        if (checkIns.size == (index + 1)) {
            Box(Modifier.height(16.dp))
        }
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
