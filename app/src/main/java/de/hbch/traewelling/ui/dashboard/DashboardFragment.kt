package de.hbch.traewelling.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.status.StatusPage
import de.hbch.traewelling.databinding.FragmentDashboardBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Swipe to refresh
        checkInsLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshDashboardCheckIns.isRefreshing = loading
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
        TraewellingApi.checkInService.getPersonalDashboard(page).enqueue(object:
            Callback<StatusPage> {
            override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                if (response.isSuccessful) {
                    val checkInAdapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                    if (page == 1) {
                        val itemCount = checkInAdapter.checkIns.size
                        checkInAdapter.checkIns.clear()
                        checkInAdapter.notifyItemRangeRemoved(0, itemCount - 1)
                        checkInAdapter.checkIns.addAll(response.body()?.data!!)
                        checkInAdapter.notifyItemRangeInserted(0, checkInAdapter.itemCount - 1)
                    } else {
                        val previousItemCount = checkInAdapter.itemCount
                        checkInAdapter.checkIns.addAll(response.body()?.data!!)
                        checkInAdapter.notifyItemRangeInserted(previousItemCount, checkInAdapter.itemCount - 1)
                    }
                }
                checkInsLoading.postValue(false)
            }
            override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                checkInsLoading.postValue(false)
                t.printStackTrace()
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}