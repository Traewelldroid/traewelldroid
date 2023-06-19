package de.hbch.traewelling.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.onBottomReached
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import de.hbch.traewelling.util.publishStationShortcuts
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Date
import kotlin.math.log

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val dashboardFragmentViewModel: DashboardFragmentViewModel by viewModels()
    private val checkInCardViewModel: CheckInCardViewModel by viewModels()
    //private var currentPage = 1
    //private var checkIns = mutableStateListOf<Status>()

    private var checkInsLoading = MutableLiveData(false)

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                    dashboardFragmentViewModel.loadCheckIns(++currentPage)
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

                        checkIns.forEach { status ->
                            item {
                                CheckInCard(
                                    checkInCardViewModel = checkInCardViewModel,
                                    status = status.toStatusDto(),
                                    loggedInUserViewModel = loggedInUserViewModel,
                                    stationSelected = { station, date ->
                                        findNavController()
                                            .navigate(
                                                DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(
                                                    station,
                                                    date
                                                )
                                            )
                                    }
                                )
                            }
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
                                    .actionDashboardFragmentToProfile(userName)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Swipe to refresh
        /*checkInsLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshDashboardCheckIns.isRefreshing = loading
            binding.layoutDataLoading.visibility = when (loading) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            loggedInUserViewModel.getLoggedInUser()
            currentPage = 1
            loadCheckins(currentPage)
        }*/

        // Init recycler view
        /*val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter =
            CheckInAdapter(
                mutableListOf(),
                loggedInUserViewModel.userId
            ) { stationName, date ->
                findNavController()
                    .navigate(
                        DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(
                            stationName,
                            date
                        )
                    )
            }

        binding.nestedScrollViewDashboard.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val vw = v.getChildAt(v.childCount - 1)
            val diff = (vw?.bottom?.minus((v.height + v.scrollY)))
            if (diff!! == 0) {
                if (!checkInsLoading.value!!) {
                    currentPage++
                    loadCheckins(currentPage)
                    checkInsLoading.postValue(true)
                }
            }
        })*/

        //loadCheckins(currentPage)
    }
/*
    private fun loadCheckins(page: Int) {
        checkInsLoading.postValue(true)
        dashboardFragmentViewModel.loadCheckIns(
            page,
            { statuses ->
                // val checkInAdapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                if (page == 1) {
                    // checkInAdapter.clearAndAddCheckIns(statuses)
                    //checkIns.clear()
                    //checkIns.addAll(statuses)
                } else {
                    // checkInAdapter.concatCheckIns(statuses)
                    //checkIns.addAll(statuses)
                }
                checkInsLoading.postValue(false)
            },
            {
                checkInsLoading.postValue(false)
            }
        )
    }*/
}