package de.hbch.traewelling.ui.searchConnection

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.Hold
import de.hbch.traewelling.R
import de.hbch.traewelling.adapters.ConnectionAdapter
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasTripPage
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.databinding.FragmentSearchConnectionBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCard
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.homelandStation.HomelandStationBottomSheet
import de.hbch.traewelling.util.toShortCut
import kotlinx.coroutines.*
import java.util.*

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding
    private lateinit var searchStationCard: SearchStationCard
    private val args: SearchConnectionFragmentArgs by navArgs()
    private val viewModel: SearchConnectionViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private val onFoundConnectionsCallback: (HafasTripPage) -> Unit = { connections ->
        dataLoading.postValue(false)
        binding.stationName = connections.meta.station.name
        binding.searchCard.binding.editTextSearchStation.clearFocus()
        binding.searchCard.loggedInUserViewModel = loggedInUserViewModel
        binding.searchCard.binding.editTextSearchStation.setText(connections.meta.station.name)
        val adapter = binding.recyclerViewConnections.adapter as ConnectionAdapter
        adapter.addNewConnections(connections.data)
        val availableTypes = connections.data.mapNotNull { it.line?.product }
        fun Chip.setStatus(vararg types: ProductType) {
            visibility = if (types.any { it in availableTypes }) VISIBLE else GONE
            if (args.travelType in types) {
                performClick()
            }
        }
        binding.executePendingBindings()
        binding.chipFilterBus.setStatus(ProductType.BUS)
        binding.chipFilterTram.setStatus(ProductType.TRAM)
        binding.chipFilterFerry.setStatus(ProductType.FERRY)
        binding.chipFilterRegional.setStatus(ProductType.REGIONAL, ProductType.REGIONAL_EXPRESS)
        binding.chipFilterSuburban.setStatus(ProductType.SUBURBAN)
        binding.chipFilterSubway.setStatus(ProductType.SUBWAY)
        binding.chipFilterExpress.setStatus(ProductType.NATIONAL, ProductType.NATIONAL_EXPRESS)

        binding.chipGroupFilter.visibility = if (connections.data.isNotEmpty())
            VISIBLE else GONE
        binding.textNoDepartures.visibility = if (connections.data.isEmpty())
            VISIBLE else GONE
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        run {
            searchStationCard.onPermissionResult(isGranted)
        }
    }
    private lateinit var currentSearchDate: Date

    private val dataLoading = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchCard.removeLocationUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)

        dataLoading.observe(viewLifecycleOwner) { loading ->
            when (loading) {
                true -> {
                    binding.cardConnections.visibility = View.GONE
                    binding.connectionDataLoadingView.root.visibility = View.VISIBLE
                }
                false -> {
                    binding.cardConnections.visibility = View.VISIBLE
                    binding.connectionDataLoadingView.root.visibility = View.GONE
                }
            }
        }

        searchStationCard = binding.searchCard
        searchStationCard.viewModel = searchStationCardViewModel
        searchStationCard.binding.card = searchStationCard
        searchStationCard.setOnStationSelectedCallback { station ->
            searchConnections(
                station,
                currentSearchDate
            )
        }
        searchStationCard.requestPermissionCallback = { permission ->
            requestPermissionLauncher.launch(permission)
        }
        searchStationCard.binding.editTextSearchStation.setText(args.stationName)
        binding.searchConnectionFragment = this
        binding.lifecycleOwner = viewLifecycleOwner
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val adapter = binding.recyclerViewConnections.adapter as ConnectionAdapter
            val checkedId = if (checkedIds.size == 0) null else checkedIds[0]
            adapter.applyFilter(
                when (checkedId) {
                    R.id.chip_filter_bus -> ProductType.BUS
                    R.id.chip_filter_express -> ProductType.LONG_DISTANCE
                    R.id.chip_filter_ferry -> ProductType.FERRY
                    R.id.chip_filter_regional -> ProductType.REGIONAL
                    R.id.chip_filter_suburban -> ProductType.SUBURBAN
                    R.id.chip_filter_subway -> ProductType.SUBWAY
                    R.id.chip_filter_tram -> ProductType.TRAM
                    else -> null
                }
            )
        }

        val connectionRecyclerView = binding.recyclerViewConnections
        connectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        connectionRecyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerViewConnections.adapter =
            ConnectionAdapter(mutableListOf()) { itemView, connection ->
                checkInViewModel.reset()
                checkInViewModel.lineName = connection.line?.name ?: ""
                checkInViewModel.tripId = connection.tripId
                checkInViewModel.startStationId = connection.station?.id ?: -1
                checkInViewModel.departureTime = connection.plannedDeparture

                val transitionName = connection.tripId
                val extras = FragmentNavigatorExtras(itemView to transitionName)
                val action =
                    SearchConnectionFragmentDirections.actionSearchConnectionFragmentToSelectDestinationFragment(
                        transitionName,
                        connection.finalDestination
                    )
                findNavController().navigate(action, extras)
            }

        binding.stationName = args.stationName

        binding.apply {
            viewModel = (this@SearchConnectionFragment).viewModel
        }

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.MINUTE, -5)
        currentSearchDate = cal.time
        searchConnections(
            binding.stationName.toString(),
            currentSearchDate
        )
        return binding.root
    }

    fun setHomelandStation(context: Context) {
        viewModel.setUserHomelandStation(
            binding.stationName ?: "",
            { station ->
                loggedInUserViewModel.setHomelandStation(station)
                val bottomSheet = HomelandStationBottomSheet(station.name)
                bottomSheet.show(parentFragmentManager, "SetHomelandStationBottomSheet")
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000)
                    bottomSheet.dismiss()
                }

                createHomelandStationShortCut(context, station)
            },
            {}
        )
    }

    private fun createHomelandStationShortCut(context: Context, station: Station) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcut = station.toShortCut(context, home = true)
            val pinnedShortcutCallbackIntent =
                ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)

            val successCallback = PendingIntent.getBroadcast(
                context, /* request code */ 0,
                pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
            )

            ShortcutManagerCompat.requestPinShortcut(
                context, shortcut,
                successCallback.intentSender
            )
        }
    }

    fun searchConnections(
        stationName: String,
        timestamp: Date
    ) {
        binding.stationName = stationName
        dataLoading.postValue(true)
        viewModel.searchConnections(
            stationName,
            timestamp,
            onFoundConnectionsCallback,
            {
                dataLoading.postValue(false)
            }
        )
    }

    fun requestDepartureTimeAndSearchConnections() {
        val datePicker = MaterialDatePicker
            .Builder
            .datePicker()
            .setTitleText(R.string.title_select_date)
            .setSelection(Date().time)
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateLong ->
            val selectedDate = Date(selectedDateLong)

            val currentDate = Calendar.getInstance()
            currentDate.time = Date()

            val timePickerBuilder = MaterialTimePicker
                .Builder()
                .setTitleText(R.string.title_select_time)
                .setHour(currentDate.get(Calendar.HOUR_OF_DAY))
                .setMinute(currentDate.get(Calendar.MINUTE))

            timePickerBuilder.setTimeFormat(
                when (is24HourFormat(requireContext())) {
                    true -> TimeFormat.CLOCK_24H
                    false -> TimeFormat.CLOCK_12H
                }
            )

            val timePicker = timePickerBuilder.build()

            timePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.time = selectedDate
                cal.set(Calendar.HOUR, timePicker.hour)
                cal.set(Calendar.MINUTE, timePicker.minute)

                searchConnections(
                    binding.stationName.toString(),
                    cal.time
                )
            }

            timePicker.show(childFragmentManager, "SearchConnectionTimePicker")
        }

        datePicker.show(childFragmentManager, "SearchConnectionDatePicker")
    }
}