package de.hbch.traewelling.ui.selectDestination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.api.dtos.TripStation

class SelectDestinationFragment : AbstractSelectDestinationFragment() {

    private val args: SelectDestinationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val response = super.onCreateView(inflater, container, savedInstanceState)
        binding.apply {
            selectDestinationView.transitionName = "transition"
        }

        return response
    }

    override fun select(itemView: View, stop: TripStation) {
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