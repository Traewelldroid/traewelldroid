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

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                "14:02",
                "Dietmannsried",
                "14:24",
                "der_heubi",
                "14:00",
                it % 2 == 0
            ))
        }
        recyclerView.adapter = CheckInAdapter(checkIns)
    }
}