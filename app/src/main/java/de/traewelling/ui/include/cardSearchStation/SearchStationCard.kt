package de.traewelling.ui.include.cardSearchStation

import StandardListItemAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.traewelling.R
import de.traewelling.databinding.CardSearchStationBinding
import de.traewelling.ui.searchConnection.SearchConnectionFragment

class SearchStationCard(private val parent: Fragment, private val binding: CardSearchStationBinding) : LocationListener {

    private lateinit var locationManager: LocationManager
    private val requestPermissionLauncher = parent.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
                run {
                    if (isGranted) {
                        getCurrentLocation()
                    }
                }
    }
    private val lastStations = listOf(
        Pair("Memmingen", R.drawable.ic_home),
        Pair("Kempten(Allgäu)Hbf", R.drawable.ic_history),
        Pair("München Hbf", R.drawable.ic_history),
        Pair("Zürich HB", R.drawable.ic_history),
        Pair("Lindau-Reutin", R.drawable.ic_history),
    )

    init {
        val lastStationsAdapter = StandardListItemAdapter(lastStations, {item, binding ->
            binding.title = item.first
            binding.imageId = item.second
            binding.executePendingBindings()
        }, {
            binding.editTextSearchStation.setText(it.first)
        })
        binding.expandableHistory.addItemDecoration(DividerItemDecoration(parent.requireContext(), DividerItemDecoration.VERTICAL))
        binding.expandableHistory.visibility = View.GONE
        binding.expandableHistory.layoutManager = LinearLayoutManager(parent.requireContext())
        binding.expandableHistory.adapter = lastStationsAdapter
        binding.inputLayoutStop.setEndIconOnClickListener {
            when (binding.expandableHistory.visibility) {
                View.VISIBLE -> {
                    binding.expandableHistory.visibility = View.GONE
                }
                else -> {
                    binding.expandableHistory.visibility = View.VISIBLE
                }
            }
            binding.executePendingBindings()
        }
        binding.executePendingBindings()
    }

    // Location listener
    override fun onLocationChanged(location: Location) {
        locationManager.removeUpdates(this)
        binding.editTextSearchStation.setText("${location.latitude}, ${location.longitude}")
    }

    fun findNearbyStations() {
        when (ContextCompat.checkSelfPermission(parent.requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    getCurrentLocation()
                }
                PackageManager.PERMISSION_DENIED -> {
                    if (parent.shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showToast("Please enable the location permission.")
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
        }
    }

    fun searchConnections() {
        val action = when (parent is SearchConnectionFragment) {
            true -> R.id.action_searchConnectionFragment_self
            false -> R.id.action_dashboard_fragment_to_searchConnectionFragment
        }
        parent.findNavController().navigate(action)
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationManager = parent.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            LocationManager.FUSED_PROVIDER
        else
            LocationManager.GPS_PROVIDER

        locationManager.requestLocationUpdates(provider, 0L, 0F, this)
    }

    private fun showToast(text: String) {
        Toast.makeText(parent.requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}