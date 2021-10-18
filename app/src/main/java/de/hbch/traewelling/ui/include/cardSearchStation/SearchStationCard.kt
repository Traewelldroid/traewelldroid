package de.hbch.traewelling.ui.include.cardSearchStation

import StandardListItemAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.station.StationData
import de.hbch.traewelling.databinding.CardSearchStationBinding
import de.hbch.traewelling.ui.dashboard.DashboardFragmentDirections
import de.hbch.traewelling.ui.searchConnection.SearchConnectionFragment
import de.hbch.traewelling.ui.searchConnection.SearchConnectionFragmentDirections
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchStationCard(
        private val parent: Fragment,
        private val binding: CardSearchStationBinding,
        private val stationName: String
    ) : LocationListener {

    private var locationManager: LocationManager? = null
    private val requestPermissionLauncher = parent.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
                run {
                    if (isGranted) {
                        getCurrentLocation()
                    }
                }
    }

    val homelandStation = MutableLiveData("")
    private val autocompleteOptions = MutableLiveData<List<String>>(listOf())

    init {
        binding.editTextSearchStation.setText(stationName)
        val adapter = ArrayAdapter<String>(
            parent.requireContext(),
            android.R.layout.simple_dropdown_item_1line
        )
        adapter.setNotifyOnChange(true)
        binding.editTextSearchStation.setAdapter(adapter)
        autocompleteOptions.observe(parent.viewLifecycleOwner) { options ->
            if (options != null) {
                adapter.clear()
                adapter.addAll(options)
                adapter.notifyDataSetChanged()
            }
        }
        binding.editTextSearchStation.doOnTextChanged { text, _, _, count ->
            if (count >= 3) {
                TraewellingApi.travelService.autoCompleteStationSearch(text?.toString() ?: "")
                    .enqueue(object : Callback<Data<List<Station>>> {
                        override fun onResponse(
                            call: Call<Data<List<Station>>>,
                            response: Response<Data<List<Station>>>
                        ) {
                            if (response.isSuccessful) {
                                val list = response.body()
                                if (list != null) {
                                    val stationNames = list.data.map {
                                        it.name
                                    }
                                    autocompleteOptions.postValue(stationNames)
                                }
                            }
                        }

                        override fun onFailure(call: Call<Data<List<Station>>>, t: Throwable) {
                            Log.e("SearchStationCard", t.stackTraceToString())
                            Sentry.captureException(t)
                        }
                    })
            }
        }

        homelandStation.observe(parent.viewLifecycleOwner) { stationName ->
            if (stationName == null || stationName == "")
                binding.inputLayoutStop.endIconMode = END_ICON_NONE
            else {
                binding.inputLayoutStop.endIconMode = END_ICON_CUSTOM
                binding.inputLayoutStop.setEndIconOnClickListener {
                    searchConnections(homelandStation.value!!)
                }
            }
            binding.executePendingBindings()
        }
    }

    // Location listener
    override fun onLocationChanged(location: Location) {
        locationManager?.removeUpdates(this)

        TraewellingApi.travelService.getNearbyStation(location.latitude, location.longitude)
            .enqueue(object: Callback<StationData> {
                override fun onResponse(call: Call<StationData>, response: Response<StationData>) {
                    if (response.isSuccessful) {
                        val station = response.body()?.data?.name
                        if (station != null) {
                            searchConnections(station)
                        }
                    } else {
                        Log.e("SearchStationCard", response.toString())
                    }
                }

                override fun onFailure(call: Call<StationData>, t: Throwable) {
                    Log.e("SearchStationCard", t.stackTraceToString())
                    Sentry.captureException(t)
                }
            })
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

    fun searchConnections(station: String) {
        val action = when (parent is SearchConnectionFragment) {
            true -> SearchConnectionFragmentDirections.actionSearchConnectionFragmentSelf(station)
            false -> DashboardFragmentDirections.actionDashboardFragmentToSearchConnectionFragment(station)
        }
        parent.findNavController().navigate(action)
    }

    fun searchConnections() {
        val stationName = binding.editTextSearchStation.text.toString()
        searchConnections(stationName)
    }

    fun removeLocationUpdates() {
        locationManager?.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationManager = parent.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            LocationManager.FUSED_PROVIDER
        else
            LocationManager.GPS_PROVIDER

        locationManager?.requestLocationUpdates(provider, 0L, 0F, this)
    }

    private fun showToast(text: String) {
        Toast.makeText(parent.requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}