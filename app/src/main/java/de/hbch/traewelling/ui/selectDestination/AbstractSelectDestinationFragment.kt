package de.hbch.traewelling.ui.selectDestination

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import de.hbch.traewelling.adapters.TravelStopAdapter
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.databinding.FragmentSelectDestinationBinding
import de.hbch.traewelling.shared.CheckInViewModel

abstract class AbstractSelectDestinationFragment : Fragment() {
    protected lateinit var binding: FragmentSelectDestinationBinding
    private val viewModel: SelectDestinationViewModel by viewModels()
    protected val checkInViewModel: CheckInViewModel by activityViewModels()

    private val dataLoading = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectDestinationBinding.inflate(inflater, container, false)

        binding.line = checkInViewModel.lineName
        dataLoading.observe(viewLifecycleOwner) { loading ->
            binding.viewConnectionDataLoading.root.visibility = when (loading) {
                true -> View.VISIBLE
                false -> View.GONE
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

        val recyclerView = binding.recyclerViewTravelStops
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dataLoading.postValue(true)
        viewModel.getTrip(
            checkInViewModel.tripId,
            checkInViewModel.lineName,
            checkInViewModel.startStationId,
            { trip ->
                dataLoading.postValue(false)
                val relevantStations = trip.stopovers.subList(
                    trip.stopovers.indexOf(
                        trip.stopovers.find {
                            it.id == checkInViewModel.startStationId
                        }
                    ) + 1, trip.stopovers.lastIndex + 1)

                recyclerView.adapter = TravelStopAdapter(relevantStations) { itemView, stop ->
                    println(relevantStations)

                    checkInViewModel.destinationStationId = stop.id
                    checkInViewModel.arrivalTime = stop.arrivalPlanned

                    select(itemView, stop)
                }
            },
            {
                dataLoading.postValue(false)
            }
        )

        return binding.root
    }

    protected abstract fun select(itemView: View, stop: HafasTrainTripStation)
}