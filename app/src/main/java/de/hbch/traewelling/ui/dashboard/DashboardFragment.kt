package de.hbch.traewelling.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.deleteStatus.DeleteStatusBottomSheet
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.util.publishStationShortcuts
import java.util.Date

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val dashboardFragmentViewModel: DashboardFragmentViewModel by viewModels()
    private val checkInCardViewModel: CheckInCardViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)


        binding.dashboardContent.setContent {
            MainTheme {
                val refreshing by dashboardFragmentViewModel.isRefreshing.observeAsState(false)
                val checkIns = remember { dashboardFragmentViewModel.checkIns }
                var currentPage by remember { mutableStateOf(1) }
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = {
                        currentPage = 1
                        dashboardFragmentViewModel.refresh()
                    }
                )
                val checkInListState = rememberLazyListState()

                checkInListState.onBottomReached {
                    if (dashboardFragmentViewModel.checkIns.size > 0) {
                        dashboardFragmentViewModel.loadCheckIns(++currentPage)
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
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        userScrollEnabled = true,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = checkInListState
                    ) {
                        item {
                            CardSearchStation(
                                searchAction = { station ->
                                    searchConnections(station)
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
                                stationSelected = { station, date ->
                                    findNavController()
                                        .navigate(
                                            DashboardFragmentDirections
                                                .actionDashboardFragmentToSearchConnectionFragment(
                                                    station,
                                                    date
                                                )
                                        )
                                },
                                userSelected = { username ->
                                    findNavController()
                                        .navigate(
                                            DashboardFragmentDirections
                                                .actionDashboardFragmentToUserProfileFragment(
                                                    username
                                                )
                                        )
                                },
                                statusSelected = { statusId, userId ->
                                    findNavController()
                                        .navigate(
                                            DashboardFragmentDirections
                                                .actionDashboardFragmentToStatusDetailFragment(
                                                    statusId,
                                                    userId
                                                )
                                        )
                                },
                                handleEditClicked = { statusValue ->
                                    findNavController().navigate(
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
                                    )
                                },
                                handleDeleteClicked = { statusValue ->
                                    val bottomSheet = DeleteStatusBottomSheet { bottomSheet ->
                                        bottomSheet.dismiss()
                                        checkInCardViewModel.deleteStatus(statusValue.statusId, {
                                            dashboardFragmentViewModel.checkIns.removeIf { it.id == statusValue.statusId }
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
                                    }
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
        }

        loggedInUserViewModel.getLoggedInUser()
        loggedInUserViewModel.getLastVisitedStations {
            publishStationShortcuts(requireContext(), it)
        }
        eventViewModel.activeEvents()

        val intent = activity?.intent
        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                if (it.hasExtra(SharedValues.EXTRA_STATUS_ID)) {
                    val statusId = it.getStringExtra(SharedValues.EXTRA_STATUS_ID)
                    if (statusId != null) {
                        intent.action = ""
                        findNavController()
                            .navigate(
                                DashboardFragmentDirections
                                    .actionDashboardFragmentToStatusDetailFragment(
                                        statusId.toInt(),
                                        0
                                    )
                            )
                    }
                } else if (it.hasExtra(SharedValues.EXTRA_USER_NAME)) {
                    val userName = it.getStringExtra(SharedValues.EXTRA_USER_NAME)
                    if (userName != null) {
                        intent.action = ""
                        findNavController()
                            .navigate(
                                DashboardFragmentDirections
                                    .actionDashboardFragmentToUserProfileFragment(userName)
                            )
                    }
                } else if (it.hasExtra(SharedValues.EXTRA_STATION_ID)) {
                    val stationId = it.getStringExtra(SharedValues.EXTRA_STATION_ID)
                    val travelType = it.getStringExtra(SharedValues.EXTRA_TRAVEL_TYPE)
                        ?.let { enumValueOf<ProductType>(it.uppercase()) }
                    if (stationId != null) {
                        intent.action = ""
                        findNavController()
                            .navigate(
                                DashboardFragmentDirections
                                    .actionDashboardFragmentToSearchConnectionFragment(
                                        stationId,
                                        null,
                                        travelType ?: ProductType.ALL
                                    )
                            )
                    }
                }
            }
        }

        return binding.root
    }

    private fun searchConnections(station: String, date: Date? = null) {
        findNavController()
            .navigate(
                DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(
                    station,
                    date
                )
            )
    }
}