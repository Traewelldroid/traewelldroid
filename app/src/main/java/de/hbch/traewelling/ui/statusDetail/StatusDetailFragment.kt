package de.hbch.traewelling.ui.statusDetail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentStatusDetailBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

class StatusDetailFragment : Fragment() {

    private val MAPNIK: OnlineTileSourceBase = XYTileSource(
        "Mapnik",
        0, 19, 256, ".png", arrayOf(
            "https://a.tile.openstreetmap.org/",
            "https://b.tile.openstreetmap.org/",
            "https://c.tile.openstreetmap.org/"
        ), "© OpenStreetMap contributors, Style: CC-BY-SA 2.0, OpenRailwayMap",
        TileSourcePolicy(
            2,
            TileSourcePolicy.FLAG_NO_BULK
                    or TileSourcePolicy.FLAG_NO_PREVENTIVE
                    or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                    or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
        )
    )

    private lateinit var binding: FragmentStatusDetailBinding
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val userViewModel: LoggedInUserViewModel by activityViewModels()
    private val viewModel: StatusDetailViewModel by viewModels()
    private val args: StatusDetailFragmentArgs by navArgs()
    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.status_detail_also_check_in_menu, menu)
            if (isOwnConnection()) {
                menu.getItem(1).isVisible = true
            } else {
                menu.getItem(2).isVisible = true
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
                R.id.menu_share_check_in -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        val url = "https://traewelling.de/status/${binding.status?.id}"
                        type = "text/plain"
                        flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        putExtra(Intent.EXTRA_TEXT, binding.status?.socialText?.plus("\n\n")?.plus(url) ?: url)
                    }

                    startActivity(
                        Intent.createChooser(
                            intent,
                            resources.getString(R.string.title_share)
                        )
                    )
                    true
                }
                else -> false
            }
        }
    }

    private val _statusLoading = MutableLiveData(true)
    val statusLoading: LiveData<Boolean> get() = _statusLoading

    private val _statusLoadingSuccess = MutableLiveData(true)
    val statusLoadingSuccess: LiveData<Boolean> get() = _statusLoadingSuccess

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusDetailBinding.inflate(inflater, container, false)
        Configuration.getInstance().userAgentValue = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"
        binding.mapStatusDetail.setTileSource(MAPNIK)
        binding.mapStatusDetail.setMultiTouchControls(true)
        binding.mapStatusDetail.controller.setZoom(17.5)

        val tileProvider = MapTileProviderBasic(requireContext())
        tileProvider.tileSource = XYTileSource("OpenRailwayMap", 0, 19, 256, ".png", arrayOf("https://tiles.openrailwaymap.org/standard/"))

        val tilesOverlay = TilesOverlay(tileProvider, requireContext())
        binding.mapStatusDetail.overlays.add(tilesOverlay)

        val copyrightOverlay = CopyrightOverlay(requireContext()).apply {
            setAlignRight(true)
            setTextSize(8)
        }
        binding.mapStatusDetail.overlays.add(copyrightOverlay)

        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

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
                requireActivity().addMenuProvider(menuProvider)
                _statusLoading.postValue(false)
                _statusLoadingSuccess.postValue(true)
            },
            {
                _statusLoading.postValue(false)
                _statusLoadingSuccess.postValue(false)
            }
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
                status.journey.origin.id,
                category = status.journey.category
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
        checkInViewModel.category = status.journey.category

        findNavController().navigate(
            StatusDetailFragmentDirections.actionStatusDetailFragmentToCheckInFragment(
                "",
                status.journey.destination.name
            )
        )
    }
}