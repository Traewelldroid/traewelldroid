package de.hbch.traewelling.ui.selectDestination

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
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
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.TravelStopAdapter
import de.hbch.traewelling.databinding.FragmentSelectDestinationBinding
import de.hbch.traewelling.models.TravelStop
import de.hbch.traewelling.shared.CheckInViewModel
import java.lang.reflect.Type

class SelectDestinationFragment : Fragment() {

    private lateinit var binding: FragmentSelectDestinationBinding
    private val args: SelectDestinationFragmentArgs by navArgs()
    private val viewModel: SelectDestinationViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)



        val transition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            val color = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.windowBackground, color, true)
            if (color.type >= TypedValue.TYPE_FIRST_COLOR_INT && color.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                setAllContainerColors(color.data)
            }
        }
        sharedElementEnterTransition = transition
        exitTransition = Hold()

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