package de.hbch.traewelling.ui.statusDetail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.FragmentStatusDetailBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

class StatusDetailFragment : Fragment() {

    private lateinit var binding: FragmentStatusDetailBinding
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
            },
            { }
        )

        return binding.root
    }

}