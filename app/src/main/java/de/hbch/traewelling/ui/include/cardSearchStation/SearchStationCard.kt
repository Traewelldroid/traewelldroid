package de.hbch.traewelling.ui.include.cardSearchStation

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import de.hbch.traewelling.databinding.CardSearchStationBinding
import de.hbch.traewelling.shared.PermissionResultReceiver

class SearchStationCard(
        context: Context,
        attrs: AttributeSet? = null
    ) : MaterialCardView(context, attrs), LocationListener, PermissionResultReceiver {

    private var locationManager: LocationManager? = null
    var requestPermissionCallback: (String) -> Unit = {}

    private var onStationSelectedCallback: (String) -> Unit = {}

    val binding = CardSearchStationBinding.inflate(
        LayoutInflater.from(context)
    )
    lateinit var viewModel: SearchStationCardViewModel

    val homelandStation = MutableLiveData("")
    private val autocompleteOptions = MutableLiveData<List<String>>(listOf())

    init {
        addView(binding.root)
        val adapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_dropdown_item_1line
        )
        adapter.setNotifyOnChange(true)
        binding.editTextSearchStation.setAdapter(adapter)
        autocompleteOptions.observe(context as FragmentActivity) { options ->
            if (options != null) {
                adapter.clear()
                adapter.addAll(options)
                adapter.notifyDataSetChanged()
            }
        }
        binding.editTextSearchStation.doOnTextChanged { text, _, _, count ->
            if (count >= 3) {
                viewModel.autoCompleteStationSearch(
                    text?.toString() ?: "",
                    { stations ->
                        autocompleteOptions.postValue(stations)
                    },
                    {}
                )
            }
        }

        homelandStation.observe(context) { stationName ->
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
        viewModel.getNearbyStation(
            location.latitude,
            location.longitude,
            { station ->
                searchConnections(station)
            },
            {}
        )
    }

    fun findNearbyStations() {
        when (ContextCompat.checkSelfPermission(context,
            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                PackageManager.PERMISSION_GRANTED -> {
                    getCurrentLocation()
                }
                PackageManager.PERMISSION_DENIED -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context as FragmentActivity,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        showToast("Please enable the location permission.")
                    } else {
                        requestPermissionCallback(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
        }
    }

    fun searchConnections(station: String) {
        onStationSelectedCallback(station)
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
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            LocationManager.FUSED_PROVIDER
        else
            LocationManager.GPS_PROVIDER

        locationManager?.requestLocationUpdates(provider, 0L, 0F, this)
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun setOnStationSelectedCallback(callback: (String) -> Unit) {
        onStationSelectedCallback = callback
    }

    override fun onPermissionResult(isGranted: Boolean) {
        if (isGranted)
            getCurrentLocation()
    }
}