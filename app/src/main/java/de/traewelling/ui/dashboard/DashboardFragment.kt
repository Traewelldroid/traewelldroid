package de.traewelling.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.status.Status
import de.traewelling.api.models.status.StatusPage
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.models.CheckIn
import de.traewelling.shared.LoggedInUserViewModel
import de.traewelling.ui.include.cardSearchStation.SearchStationCard
import de.traewelling.ui.include.status.StatusCardViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var searchStationCard: SearchStationCard
    private val statusCardViewModel: StatusCardViewModel by viewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        searchStationCard = SearchStationCard(this, binding.searchCard, "")
        loggedInUserViewModel.getLoggedInUser()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            searchCard.viewModel = searchStationCard
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Init recycler view
        val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CheckInAdapter(mutableListOf(), statusCardViewModel)

        loadCheckins()

        // Swipe to refresh
        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            loggedInUserViewModel.getLoggedInUser()
            loadCheckins()
        }
    }

    fun loadCheckins() {
        binding.swipeRefreshDashboardCheckIns.isRefreshing = true
        TraewellingApi.checkInService.getPersonalDashboard(1).enqueue(object:
            Callback<StatusPage> {
            override fun onResponse(call: Call<StatusPage>, response: Response<StatusPage>) {
                if (response.isSuccessful) {
                    binding.swipeRefreshDashboardCheckIns.isRefreshing = false
                    binding.recyclerViewCheckIn.adapter = CheckInAdapter(
                        response.body()?.data!!,
                        statusCardViewModel
                    )
                }
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
            }
            override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                binding.swipeRefreshDashboardCheckIns.isRefreshing = false
                t.printStackTrace()
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}