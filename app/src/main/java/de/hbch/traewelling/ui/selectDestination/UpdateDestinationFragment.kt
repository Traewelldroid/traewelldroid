package de.hbch.traewelling.ui.selectDestination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation

class UpdateDestinationFragment : AbstractSelectDestinationFragment() {
    private val args: UpdateDestinationFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkInViewModel.reset()
        checkInViewModel.tripId = args.tripId
        checkInViewModel.lineName = args.lineName
        checkInViewModel.startStationId = args.startStationId
        val response = super.onCreateView(inflater, container, savedInstanceState)
        binding.destination = args.destination

        return response
    }

    override fun select(itemView: View, stop: HafasTrainTripStation) {
        findNavController().navigate(
            UpdateDestinationFragmentDirections.actionSelectDestinationFragmentToEditCheckIn(
                args.transitionName,
                stop.name,
                args.departureTime,
                args.body,
                args.lineName,
                args.visibility,
                args.business,
                args.statusId,
                args.tripId,
                args.startStationId,
                replace = true,
                destinationId = stop.id
            )
        )
    }
}