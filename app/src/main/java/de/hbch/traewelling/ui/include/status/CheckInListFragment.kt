package de.hbch.traewelling.ui.include.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.CheckInAdapter
import de.hbch.traewelling.databinding.FragmentCheckInListBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCard


class CheckInListFragment : Fragment() {

    private lateinit var binding: FragmentCheckInListBinding
    private var checkInsLoading = MutableLiveData(false)
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    lateinit var checkInListViewModel: CheckInListViewModel
    var searchStationCard: SearchStationCard? = null

    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCheckInListBinding.inflate(
        inflater, container, false
    ).also {
        binding = it
    }.root

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
        // Init recycler view
        val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter =
            CheckInAdapter(
                mutableListOf(),
                loggedInUserViewModel.userId
            ) { stationName, date ->
                searchStationCard?.searchConnections(stationName, date)
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
        checkInListViewModel.loadCheckIns(
            page,
            { statuses ->
                val checkInAdapter = binding.recyclerViewCheckIn.adapter as CheckInAdapter
                if (statuses.isNotEmpty()) {
                    displayCheckIns()
                }
                if (page == 1) {
                    if (statuses.isEmpty()) {
                        displayLoadingError()
                    } else {
                        checkInAdapter.clearAndAddCheckIns(statuses)
                    }
                } else {
                    checkInAdapter.concatCheckIns(statuses)
                }
                checkInsLoading.postValue(false)
            },
            {
                displayLoadingError(it.localizedMessage)
                checkInsLoading.postValue(false)
            }
        )
    }

    private fun displayLoadingError(errorName: String? = null) {
        binding.recyclerViewCheckIn.visibility = View.GONE
        binding.layoutCheckInLoadingError.visibility = View.VISIBLE
        binding.textLoadingError.text = if (errorName != null) {
            resources.getString(R.string.check_in_loading_error, errorName)
        } else {
            resources.getString(R.string.no_checkins)
        }
    }

    private fun displayCheckIns() {
        binding.recyclerViewCheckIn.visibility = View.VISIBLE
        binding.layoutCheckInLoadingError.visibility = View.GONE
    }
}