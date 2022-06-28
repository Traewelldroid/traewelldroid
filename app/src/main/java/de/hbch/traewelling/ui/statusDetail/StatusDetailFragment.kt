package de.hbch.traewelling.ui.statusDetail

import android.os.Build
import android.os.Bundle
import android.view.*
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                setHasOptionsMenu(canAlsoCheckIntoThisConnection())
            },
            { }
        )

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.status_detail_also_check_in_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_also_check_in -> {
                alsoCheckIntoThisConnection()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun canAlsoCheckIntoThisConnection(): Boolean {
        return (userViewModel.loggedInUser.value?.id ?: 0) != args.userId;
    }

    private fun alsoCheckIntoThisConnection() {
        if (!canAlsoCheckIntoThisConnection())
            return

        val status = binding.status!!
        checkInViewModel.reset()
        checkInViewModel.lineName = status.journey.line
        checkInViewModel.tripId = status.journey.hafasTripId
        checkInViewModel.startStationId = status.journey.origin.id
        checkInViewModel.departureTime = status.journey.origin.departurePlanned
        checkInViewModel.destinationStationId = status.journey.destination.id
        checkInViewModel.arrivalTime = status.journey.destination.arrivalPlanned

        findNavController().navigate(StatusDetailFragmentDirections.actionStatusDetailFragmentToCheckInFragment("", status.journey.destination.name))
    }
}