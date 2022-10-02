package de.hbch.traewelling.ui.statusDetail

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentStatusDetailBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

class StatusDetailFragment : Fragment() {

    private lateinit var binding: FragmentStatusDetailBinding
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val userViewModel: LoggedInUserViewModel by activityViewModels()
    private val viewModel: StatusDetailViewModel by viewModels()
    private val args: StatusDetailFragmentArgs by navArgs()
    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.status_detail_also_check_in_menu, menu)
            if (isOwnConnection()) {
                menu.getItem(0).isVisible = true
            } else {
                menu.getItem(1).isVisible = true
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_also_check_in -> {
                    alsoCheckIntoThisConnection()
                    true
                }
                R.id.menu_edit_check_in -> {
                    editStatus()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusDetailBinding.inflate(inflater, container, false)
        Configuration.getInstance().userAgentValue = "de.hbch.traewelldroid/1.0"
        binding.mapStatusDetail.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapStatusDetail.setMultiTouchControls(true)
        binding.mapStatusDetail.controller.setZoom(17.5)

        viewModel.getPolylineForStatus(
            args.statusId,
            { featureCollection ->
                val polylines: MutableList<Polyline> = mutableListOf()
                featureCollection.features?.forEach { feature ->
                    val polyline = Polyline()
                    feature.geometry?.coordinates?.forEach { coordinate ->
                        polyline.addPoint(
                            GeoPoint(
                                coordinate[1],
                                coordinate[0]
                            )
                        )
                    }
                    polylines.add(polyline)
                    val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        R.color.material_dynamic_primary40
                    else
                        R.color.traewelling

                    polyline.outlinePaint.color = resources.getColor(
                        color,
                        requireContext().theme
                    )
                }
                binding.mapStatusDetail.overlays.addAll(polylines)
                if (polylines.size > 0)
                    binding
                        .mapStatusDetail
                        .zoomToBoundingBox(
                            polylines[0]
                                .bounds
                                .increaseByScale(1.1f),
                            false
                        )
            },
            { }
        )

        viewModel.getStatusById(
            args.statusId,
            { status ->
                binding.status = status
                if (args.statusId != -1) {
                    requireActivity().addMenuProvider(menuProvider)
                }
            },
            { }
        )

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    private fun isOwnConnection(): Boolean {
        return (userViewModel.loggedInUser.value?.id ?: 0) != args.userId;
    }

    private fun editStatus() {
        if (isOwnConnection())
            return

        val status = binding.status!!
        findNavController().navigate(
            StatusDetailFragmentDirections.actionStatusDetailFragmentToEditStatusFragment(
                status.journey.origin.name,
                status.journey.destination.name,
                status.journey.origin.departurePlanned,
                status.body,
                status.journey.line,
                status.visibility.ordinal,
                status.business.ordinal,
                status.id,
                status.journey.hafasTripId,
                status.journey.origin.id
            )
        )
    }

    private fun alsoCheckIntoThisConnection() {
        if (!isOwnConnection())
            return

        val status = binding.status!!
        checkInViewModel.reset()
        checkInViewModel.lineName = status.journey.line
        checkInViewModel.tripId = status.journey.hafasTripId
        checkInViewModel.startStationId = status.journey.origin.id
        checkInViewModel.departureTime = status.journey.origin.departurePlanned
        checkInViewModel.destinationStationId = status.journey.destination.id
        checkInViewModel.arrivalTime = status.journey.destination.arrivalPlanned

        findNavController().navigate(
            StatusDetailFragmentDirections.actionStatusDetailFragmentToCheckInFragment(
                "",
                status.journey.destination.name
            )
        )
    }
}