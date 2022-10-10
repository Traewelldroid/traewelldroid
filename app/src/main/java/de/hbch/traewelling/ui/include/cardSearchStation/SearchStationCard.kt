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
import android.view.Menu
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import de.hbch.traewelling.R
import de.hbch.traewelling.databinding.CardSearchStationBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.PermissionResultReceiver
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.util.StationNameClickListener
import java.util.Date

class SearchStationCard(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs), LocationListener, PermissionResultReceiver {

    private var locationManager: LocationManager? = null
    var requestPermissionCallback: (String) -> Unit = {}

    private var onStationSelectedCallback: StationNameClickListener = { _, _ -> }
    private lateinit var _loggedInUserViewModel: LoggedInUserViewModel

    val binding = CardSearchStationBinding.inflate(
        LayoutInflater.from(context)
    )
    lateinit var viewModel: SearchStationCardViewModel
    var loggedInUserViewModel: LoggedInUserViewModel
        get() = _loggedInUserViewModel
        set(value) {
            _loggedInUserViewModel = value
            loggedInUserViewModel.homelandStation.observe(context as FragmentActivity) {
                setSearchEndIconAndDisplayMode()
            }
            loggedInUserViewModel.lastVisitedStations.observe(context as FragmentActivity) {
                setSearchEndIconAndDisplayMode()
            }
        }

    private val autocompleteOptions = MutableLiveData<List<String>>(listOf())

    init {
        addView(binding.root)
        val adapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_dropdown_item_1line
        )
        adapter.setNotifyOnChange(true)
        binding.editTextSearchStation.clearFocus()
        binding.editTextSearchStation.setOnEditorActionListener { _, editorAction, _ ->
            if (editorAction == EditorInfo.IME_ACTION_SEARCH) {
                searchConnections()
                true
            } else
                false
        }
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
    }

    @SuppressLint("RestrictedApi")
    fun setSearchEndIconAndDisplayMode() {
        var hasHomelandStation = false
        var hasLastVisitedStations = false
        if (loggedInUserViewModel.loggedInUser.value?.home != null)
            hasHomelandStation = true
        if (loggedInUserViewModel.lastVisitedStations.value != null &&
            loggedInUserViewModel.lastVisitedStations.value?.size!! > 0
        )
            hasLastVisitedStations = true

        if (hasLastVisitedStations) {
            // Show dropdown
            binding.inputLayoutStop.endIconDrawable =
                ContextCompat.getDrawable(context, R.drawable.ic_expand_more)
            binding.inputLayoutStop.endIconMode = END_ICON_CUSTOM
            binding.inputLayoutStop.setEndIconOnClickListener { button ->
                val popupMenu = PopupMenu(context, button)
                var menuIndex = Menu.FIRST
                if (hasHomelandStation) {
                    popupMenu.menu.add(
                        0,
                        menuIndex,
                        Menu.NONE,
                        loggedInUserViewModel.loggedInUser.value?.home?.name ?: ""
                    )
                    popupMenu.menu.findItem(menuIndex)?.setIcon(R.drawable.ic_home)
                    menuIndex++
                }
                if (hasLastVisitedStations) {
                    loggedInUserViewModel
                        .lastVisitedStations
                        .value!!
                        .forEachIndexed { index, station ->
                            popupMenu.menu.add(
                                0,
                                menuIndex + index,
                                Menu.NONE,
                                station.name
                            )
                            popupMenu.menu.findItem(menuIndex + index)
                                ?.setIcon(R.drawable.ic_history)
                        }
                }
                popupMenu.setOnMenuItemClickListener { item ->
                    searchConnections(item.title.toString())
                    true
                }
                if (popupMenu.menu is MenuBuilder)
                    (popupMenu.menu as MenuBuilder).setOptionalIconsVisible(true)
                popupMenu.show()
            }
        } else if (hasHomelandStation) {
            // Show house icon
            binding.inputLayoutStop.endIconDrawable =
                ContextCompat.getDrawable(context, R.drawable.ic_home)
            // binding.inputLayoutStop.setEndIconDrawable(R.drawable.ic_home)
            binding.inputLayoutStop.endIconMode = END_ICON_CUSTOM
            binding.inputLayoutStop.setEndIconOnClickListener {
                searchConnections(loggedInUserViewModel.homelandStation.value!!)
            }
        } else {
            binding.inputLayoutStop.endIconMode = END_ICON_NONE
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
        when (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            PackageManager.PERMISSION_DENIED -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as FragmentActivity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    val alertBottomSheet = AlertBottomSheet(
                        AlertType.ERROR,
                        context.getString(R.string.error_missing_location_permission),
                        3000
                    )
                    alertBottomSheet.show(
                        (context as FragmentActivity).supportFragmentManager,
                        AlertBottomSheet.TAG
                    )
                } else {
                    requestPermissionCallback(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    fun searchConnections(station: String, date: Date? = null) {
        onStationSelectedCallback(station, date)
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

    fun setOnStationSelectedCallback(callback: StationNameClickListener) {
        onStationSelectedCallback = callback
    }

    override fun onPermissionResult(isGranted: Boolean) {
        if (isGranted)
            getCurrentLocation()
    }
}