package de.hbch.traewelling.ui.selectDestination

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.databinding.FragmentSelectDestinationBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.theme.MainTheme

abstract class AbstractSelectDestinationFragment : Fragment() {
    protected lateinit var binding: FragmentSelectDestinationBinding
    private val viewModel: SelectDestinationViewModel by viewModels()
    protected val checkInViewModel: CheckInViewModel by activityViewModels()

    private val tripData = MutableLiveData<Trip?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)

        binding.selectDestinationView.setContent {
            MainTheme {
                SelectDestination(
                    modifier = Modifier.padding(16.dp),
                    tripData = tripData,
                    stationSelectedAction = { station ->
                        checkInViewModel.destinationStationId = station.id
                        checkInViewModel.arrivalTime = station.arrivalPlanned
                        select(binding.selectDestinationView, station)
                    }
                )
            }
        }

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

        viewModel.getTrip(
            checkInViewModel.tripId,
            checkInViewModel.lineName,
            checkInViewModel.startStationId,
            { trip ->
                val relevantStations = trip.stopovers.subList(
                    trip.stopovers.indexOf(
                        trip.stopovers.find {
                            it.id == checkInViewModel.startStationId
                        }
                    ) + 1, trip.stopovers.lastIndex + 1)

                trip.stopovers = relevantStations

                tripData.postValue(trip)
            },
            {
            }
        )

        return binding.root
    }

    protected abstract fun select(itemView: View, stop: TripStation)
}