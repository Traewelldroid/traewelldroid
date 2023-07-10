package de.hbch.traewelling.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import java.util.Date

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Profile(
    username: String?,
    loggedInUserViewModel: LoggedInUserViewModel,
    stationSelectedAction: (String, Date?) -> Unit = { _, _ -> }
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
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
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
                    statusSelected = { statusId, userId ->
                        /*TODO findNavController()
                            .navigate(
                                UserFragmentDirections
                                    .actionUserFragmentToStatusDetailFragment(
                                        statusId,
                                        userId
                                    )
                            )*/
                    },
                    handleEditClicked = { statusValue ->
                        /*TODO findNavController().navigate(
                            R.id.editStatusFragment,
                            bundleOf(
                                "transitionName" to statusValue.origin,
                                "destination" to statusValue.destination,
                                "body" to statusValue.message,
                                "departureTime" to statusValue.departurePlanned,
                                "business" to statusValue.business.ordinal,
                                "visibility" to statusValue.visibility.ordinal,
                                "line" to statusValue.line,
                                "statusId" to statusValue.statusId,
                                "tripId" to statusValue.hafasTripId,
                                "startStationId" to statusValue.originId,
                                "category" to statusValue.productType
                            )
                        )*/
                    },
                    handleDeleteClicked = { statusValue ->
                        /*TODO val bottomSheet = DeleteStatusBottomSheet { bottomSheet ->
                            bottomSheet.dismiss()
                            checkInCardViewModel.deleteStatus(statusValue.statusId, {
                                userStatusViewModel.checkIns.removeIf { it.id == statusValue.statusId }
                                val alertBottomSheet = AlertBottomSheet(
                                    AlertType.SUCCESS,
                                    requireContext().resources.getString(R.string.status_delete_success),
                                    3000
                                )
                                alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
                            }, {
                                val alertBottomSheet = AlertBottomSheet(
                                    AlertType.ERROR,
                                    requireContext().resources.getString(R.string.status_delete_failure),
                                    3000
                                )
                                alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
                            })
                        }
                        if (context is FragmentActivity) {
                            bottomSheet.show(parentFragmentManager, DeleteStatusBottomSheet.TAG)
                        }*/
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
