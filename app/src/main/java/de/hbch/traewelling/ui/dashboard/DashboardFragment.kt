package de.hbch.traewelling.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCard
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var searchStationCard: SearchStationCard
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val dashboardFragmentViewModel: DashboardFragmentViewModel by viewModels()
    private var currentPage = 1
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
            isGranted ->
            run {
                searchStationCard.onPermissionResult(isGranted)
            }
    }

    private var checkInsLoading = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        searchStationCard = binding.searchCard
        searchStationCard.viewModel = searchStationCardViewModel
        searchStationCard.loggedInUserViewModel = loggedInUserViewModel
        searchStationCard.binding.card = searchStationCard
        searchStationCard.requestPermissionCallback = { permission ->
            requestPermissionLauncher.launch(permission)
        }
        loggedInUserViewModel.getLoggedInUser()
        loggedInUserViewModel.getLastVisitedStations()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(optionsMenu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, optionsMenu)
        super.onCreateOptionsMenu(optionsMenu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_dashboard_notification -> {
                handleNotificationMenuClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleNotificationMenuClick() {
        val alertBottomSheet = AlertBottomSheet(
            AlertType.ERROR,
            requireContext().getString(R.string.notifications_not_implemented_text),
            5000
        )
        alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Swipe to refresh
        checkInsLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshDashboardCheckIns.isRefreshing = loading
            binding.layoutDataLoading.root.visibility = when (loading) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            loggedInUserViewModel.getLoggedInUser()
            currentPage = 1
            loadCheckins(currentPage)
        }
        binding.searchCard.setOnStationSelectedCallback { station ->
            findNavController()
                .navigate(DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(station))
        }

        // Init recycler view
        val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter =
            CheckInAdapter(
                mutableListOf(),
                loggedInUserViewModel.userId
            ) {
                stationName -> searchStationCard.searchConnections(stationName)
            }

        binding.nestedScrollViewDashboard.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val vw = v?.getChildAt(v.childCount - 1)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchCard.removeLocationUpdates()
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