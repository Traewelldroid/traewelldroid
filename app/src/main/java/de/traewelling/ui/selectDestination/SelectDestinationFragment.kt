package de.traewelling.ui.selectDestination

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import de.traewelling.R
import de.traewelling.adapters.TravelStopAdapter
import de.traewelling.databinding.FragmentSelectDestinationBinding
import de.traewelling.models.TravelStop
import de.traewelling.shared.CheckInViewModel

class SelectDestinationFragment : Fragment() {

    private lateinit var binding: FragmentSelectDestinationBinding
    private val args: SelectDestinationFragmentArgs by navArgs()
    private val viewModel: SelectDestinationViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.design_default_color_surface, requireContext().theme))
        }
        sharedElementEnterTransition = transition
        exitTransition = Hold()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)
        binding.layoutSelectDestination.transitionName = args.transitionName
        binding.apply {
            destination = args.destination
            line = checkInViewModel.lineName
        }

        val recyclerView = binding.recyclerViewTravelStops
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.trip.observe(viewLifecycleOwner) { trip ->

            val relevantStations = trip.stopovers.subList(
                trip.stopovers.indexOf(
                    trip.stopovers.find {
                        it.id == checkInViewModel.startStationId
                    }
                ) + 1, trip.stopovers.lastIndex + 1)

            recyclerView.adapter = TravelStopAdapter(relevantStations) { itemView, stop ->

                checkInViewModel.destinationStationId = stop.id
                checkInViewModel.arrivalTime = stop.arrivalPlanned

                val transitionName = stop.name
                val extras = FragmentNavigatorExtras(
                    itemView to transitionName
                )
                findNavController().navigate(
                    SelectDestinationFragmentDirections
                        .actionSelectDestinationFragmentToCheckInFragment(
                            transitionName,
                            stop.name
                        ),
                    extras
                )
            }
        }
        viewModel.getTrip(
            checkInViewModel.tripId,
            checkInViewModel.lineName,
            checkInViewModel.startStationId
        )

        return binding.root
    }
}