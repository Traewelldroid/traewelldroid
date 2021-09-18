package de.traewelling.ui.activeCheckins

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.databinding.FragmentActiveCheckinsBinding
import de.traewelling.models.CheckIn
import de.traewelling.ui.include.status.StatusCardViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class ActiveCheckinsFragment : Fragment() {

    private lateinit var binding: FragmentActiveCheckinsBinding
    private val viewModel: ActiveCheckinsViewModel by viewModels()
    private val statusCardViewModel: StatusCardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        binding = FragmentActiveCheckinsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.activeCheckinsMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        binding.activeCheckinsMap.setMultiTouchControls(true)
        val controller = binding.activeCheckinsMap.controller
        controller.setZoom(12.0)
        controller.setCenter(GeoPoint(47.98, 10.18))

        val recyclerView = binding.recyclerViewActiveCheckIns
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.statuses.observe(viewLifecycleOwner) { statusPage ->
            if (statusPage != null) {
                recyclerView.adapter = CheckInAdapter(
                    statusPage.data.toMutableList(),
                    statusCardViewModel
                )
            }
            binding.swipeRefreshCheckins.isRefreshing = false
        }
        getActiveCheckins()

        binding.swipeRefreshCheckins.setOnRefreshListener {
            getActiveCheckins()
        }

        return binding.root
    }

    fun getActiveCheckins() {
        binding.swipeRefreshCheckins.isRefreshing = true
        viewModel.getActiveCheckins()
    }

    override fun onPause() {
        super.onPause()
        binding.activeCheckinsMap.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.activeCheckinsMap.onResume()
    }
}