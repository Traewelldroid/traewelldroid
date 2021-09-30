package de.traewelling.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.status.StatusPage
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.shared.LoggedInUserViewModel
import de.traewelling.ui.include.cardSearchStation.SearchStationCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var searchStationCard: SearchStationCard
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private var currentPage = 1

    private var checkInsLoading = MutableLiveData(false)

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
        // Swipe to refresh
        checkInsLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefreshDashboardCheckIns.isRefreshing = loading
        }
        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            loggedInUserViewModel.getLoggedInUser()
            currentPage = 1
            loadCheckins(currentPage)
        }

        // Init recycler view
        val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CheckInAdapter(mutableListOf())
        binding.nestedScrollViewDashboard.setOnScrollChangeListener(object: NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                val view = v?.getChildAt(v.childCount - 1)
                val diff = (view?.bottom?.minus((v?.height + v.scrollY)))
                if (diff!! == 0) {
                    if (!checkInsLoading.value!!) {
                        currentPage++
                        loadCheckins(currentPage)
                        checkInsLoading.value = true
                    }
                }
            }

        })

        loadCheckins(currentPage)
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
                checkInsLoading.value = false
            }
            override fun onFailure(call: Call<StatusPage>, t: Throwable) {
                checkInsLoading.value = false
                t.printStackTrace()
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}