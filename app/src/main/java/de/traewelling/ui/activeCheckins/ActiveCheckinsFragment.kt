package de.traewelling.ui.activeCheckins

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.databinding.FragmentActiveCheckinsBinding
import de.traewelling.models.CheckIn
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class ActiveCheckinsFragment : Fragment() {

    private lateinit var binding: FragmentActiveCheckinsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName

        binding = FragmentActiveCheckinsBinding.inflate(inflater, container, false)

        binding.activeCheckinsMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        binding.activeCheckinsMap.setMultiTouchControls(true)
        val controller = binding.activeCheckinsMap.controller
        controller.setZoom(12.0)
        controller.setCenter(GeoPoint(47.98, 10.18))

        val recyclerView = binding.recyclerViewActiveCheckIns
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val checkIns = mutableListOf<CheckIn>()
        repeat (5) {
            checkIns.add(
                CheckIn(
                "Memmingen",
                "Kempten(Allgäu)Hbf",
                "RE 75",
                "35km",
                "22min",
                "1$it:02",
                "Dietmannsried",
                "1$it:24",
                "der_heubi",
                "1$it:00",
                it % 2 == 0
            )
            )
        }
        //recyclerView.adapter = CheckInAdapter(checkIns)

        return binding.root
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