package de.traewelling.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.models.CheckIn
import de.traewelling.ui.include.cardSearchStation.CardSearchStationViewModel
import java.util.jar.Manifest

class DashboardFragment : Fragment(), LocationListener {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var locationManager: LocationManager
    private val cardSearchStationViewModel = CardSearchStationViewModel()
    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) {
        isGranted ->
        run {
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Too bad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            searchCard.viewModel = cardSearchStationViewModel
        }

        cardSearchStationViewModel.setRequestLocationListener {
            binding.searchCard.btnLocateProgress.visibility = VISIBLE
            when (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    getCurrentLocation()
                }
                PackageManager.PERMISSION_DENIED -> {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(requireContext(), "Just enable permission!", Toast.LENGTH_SHORT).show()
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = binding.recyclerViewCheckIn
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val checkIns = mutableListOf<CheckIn>()
        repeat (5) {
            checkIns.add(CheckIn(
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
            ))
        }
        recyclerView.adapter = CheckInAdapter(checkIns)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this)
    }

    override fun onLocationChanged(location: Location) {
        locationManager.removeUpdates(this)
        binding.searchCard.editTextStation.setText("${location?.latitude} ${location?.longitude}")
    }
}