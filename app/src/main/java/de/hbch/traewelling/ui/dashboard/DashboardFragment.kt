package de.hbch.traewelling.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.util.publishStationShortcuts
import java.util.Date

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val dashboardFragmentViewModel: DashboardFragmentViewModel by viewModels()
    private var currentPage = 1

    private var checkInsLoading = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.searchCard.setContent {
            Mdc3Theme(
                setDefaultFontFamily = true
            ) {
                CardSearchStation(
                    searchAction = { station ->
                        searchConnections(station)
                    },
                    searchStationCardViewModel = searchStationCardViewModel,
                    homelandStationData = loggedInUserViewModel.home,
                    recentStationsData = loggedInUserViewModel.lastVisitedStations
                )
            }
        }

        loggedInUserViewModel.getLoggedInUser()
        loggedInUserViewModel.getLastVisitedStations {
            publishStationShortcuts(requireContext(), it)
        }
        eventViewModel.activeEvents()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            layoutDataLoading.setContent {
                Mdc3Theme(
                    setTextColors = true,
                    setDefaultFontFamily = true
                ) {
                    DataLoading()
                }
            }
        }

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

    private fun searchConnections(station: String, date: Date = Date()) {
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
        checkInsLoading.observe(viewLifecycleOwner) { loading ->
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
        }

        // Init recycler view
        val recyclerView = binding.recyclerViewCheckIn
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
        })

        loadCheckins(currentPage)
    }

    private fun loadCheckins(page: Int) {
        checkInsLoading.postValue(true)
        dashboardFragmentViewModel.loadCheckIns(
            page,
            { statuses ->
                val checkInAdapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                if (page == 1) {
                    checkInAdapter.clearAndAddCheckIns(statuses)
                } else {
                    checkInAdapter.concatCheckIns(statuses)
                }
                checkInsLoading.postValue(false)
            },
            {
                checkInsLoading.postValue(false)
            }
        )
    }
}