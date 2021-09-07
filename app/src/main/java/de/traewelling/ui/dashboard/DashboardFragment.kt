package de.traewelling.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.models.CheckIn
import de.traewelling.ui.include.cardSearchStation.SearchStationCard

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var searchStationCard: SearchStationCard

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        searchStationCard = SearchStationCard(this, binding.searchCard, "")

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
        val checkIns = mutableListOf<CheckIn>()
        repeat (5) {
            checkIns.add(CheckIn(
                "Memmingen",
                "Kempten(Allgäu)Hbf",
                "RE 75",
                "35km",
                "22min",
                "1$it:02",
                "Dietmannsried",
                "1$it:24",
                "der_heubi",
                "1$it:00",
                it % 2 == 0
            ))
        }
        recyclerView.adapter = CheckInAdapter(checkIns)

        // Swipe to refresh
        binding.swipeRefreshDashboardCheckIns.setOnRefreshListener {
            binding.swipeRefreshDashboardCheckIns.isRefreshing = false
        }
    }
}