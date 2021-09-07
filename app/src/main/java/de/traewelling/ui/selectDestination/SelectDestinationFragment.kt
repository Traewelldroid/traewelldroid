package de.traewelling.ui.selectDestination

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.TravelStopAdapter
import de.traewelling.databinding.FragmentSelectDestinationBinding
import de.traewelling.models.TravelStop

class SelectDestinationFragment : Fragment() {

    private lateinit var binding: FragmentSelectDestinationBinding
    private val stops = listOf(
        TravelStop("Bad Grönenbach", "15:09"),
        TravelStop("Dietmannsried", "15:17"),
        TravelStop("Kempten(Allgäu)Ost", "15:22"),
        TravelStop("Kempten(Allgäu)Hbf", "15:25")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)
        binding.apply {
            destination = stops.last().stationName
            line = "RE 75"
        }

        val recyclerView = binding.recyclerViewTravelStops
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TravelStopAdapter(stops) {
            Toast.makeText(requireContext(), it.stationName, Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
}