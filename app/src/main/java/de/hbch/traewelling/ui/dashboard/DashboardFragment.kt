package de.hbch.traewelling.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCard
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.status.CheckInListFragment
import de.hbch.traewelling.util.publishStationShortcuts

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var searchStationCard: SearchStationCard
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val dashboardFragmentViewModel: DashboardFragmentViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        run {
            searchStationCard.onPermissionResult(isGranted)
        }
    }

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
        loggedInUserViewModel.getLastVisitedStations {
            publishStationShortcuts(requireContext(), it)
        }
        eventViewModel.activeEvents()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchCard.setOnStationSelectedCallback { station, date ->
            findNavController()
                .navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(
                        station,
                        date
                    )
                )
        }

        binding.checkInList.getFragment<CheckInListFragment>().checkInListViewModel =
            dashboardFragmentViewModel

        return super.onViewCreated(view, savedInstanceState)
    }

    private fun handleNotificationMenuClick() {
        val alertBottomSheet = AlertBottomSheet(
            AlertType.ERROR,
            requireContext().getString(R.string.notifications_not_implemented_text),
            5000
        )
        alertBottomSheet.show(parentFragmentManager, AlertBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchCard.removeLocationUpdates()
    }
}