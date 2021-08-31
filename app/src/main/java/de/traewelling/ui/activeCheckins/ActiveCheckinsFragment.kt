package de.traewelling.ui.activeCheckins

import android.os.Build
import android.os.Bundle
import android.security.NetworkSecurityPolicy
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import de.traewelling.databinding.FragmentActiveCheckinsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.util.jar.Manifest

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