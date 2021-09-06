package de.traewelling.ui.dashboard

import StandardListItemAdapter
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.R
import de.traewelling.adapters.CheckInAdapter
import de.traewelling.databinding.FragmentDashboardBinding
import de.traewelling.models.CheckIn
import de.traewelling.ui.include.cardSearchStation.CardSearchStationViewModel

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
                showToast("Permission not granted")
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
            when (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    getCurrentLocation()
                }
                PackageManager.PERMISSION_DENIED -> {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showToast("Please enable the location permission.")
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
        val lastStations = listOf("Memmingen", "Kempten(Allgäu)Hbf", "München Hbf")
        val lastStationsAdapter = StandardListItemAdapter(lastStations, {item, binding ->
            binding.title = item
            binding.imageId = R.drawable.ic_history
            binding.executePendingBindings()
        }, {
            binding.searchCard.editTextSearchStation.setText(it)
        })
        binding.searchCard.expandableHistory.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.searchCard.expandableHistory.visibility = GONE
        binding.searchCard.expandableHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.searchCard.expandableHistory.adapter = lastStationsAdapter
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
        binding.searchCard.inputLayoutStop.setEndIconOnClickListener {
            when (binding.searchCard.expandableHistory.visibility) {
                VISIBLE -> {
                    binding.searchCard.expandableHistory.visibility = GONE
                }
                else -> {
                    binding.searchCard.expandableHistory.visibility = VISIBLE
                }
            }
            binding.executePendingBindings()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            LocationManager.FUSED_PROVIDER
        else
            LocationManager.GPS_PROVIDER

        locationManager.requestLocationUpdates(provider, 0L, 0F, this)
    }

    override fun onLocationChanged(location: Location) {
        locationManager.removeUpdates(this)
        showToast("${location?.latitude}, ${location?.longitude}")
        binding.searchCard.editTextSearchStation.setText("${location?.latitude} ${location?.longitude}")
    }
}