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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.google.accompanist.themeadapter.material3.Mdc3Theme
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
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.include.cardSearchStation.CardSearchStation
import de.hbch.traewelling.ui.include.cardSearchStation.SearchStationCardViewModel
import de.hbch.traewelling.ui.include.homelandStation.HomelandStationBottomSheet
import de.hbch.traewelling.util.toShortCut
import kotlinx.coroutines.*
import java.util.*

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding
    private val args: SearchConnectionFragmentArgs by navArgs()
    private val viewModel: SearchConnectionViewModel by viewModels()
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val loggedInUserViewModel: LoggedInUserViewModel by activityViewModels()
    private val searchStationCardViewModel: SearchStationCardViewModel by viewModels()
    private lateinit var currentSearchDate: Date

    private val dataLoading = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)
        if (args.date != null) {
            currentSearchDate = args.date!!
        } else {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.MINUTE, -5)
            currentSearchDate = cal.time
        }

        binding.searchConnectionContent.setContent {
            MainTheme {
                var stationName by rememberSaveable { mutableStateOf(args.stationName) }
                var scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardSearchStation(
                        searchAction = { station ->
                            stationName = station
                            //searchConnections(station, currentSearchDate)
                        },
                        searchStationCardViewModel = searchStationCardViewModel,
                        homelandStationData = loggedInUserViewModel.home,
                        recentStationsData = loggedInUserViewModel.lastVisitedStations
                    )
                    SearchConnection(
                        searchConnectionViewModel = viewModel,
                        stationName = stationName,
                        searchTime = currentSearchDate
                    )
                }
            }
        }

        return binding.root
    }

    /*fun setHomelandStation(context: Context) {
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
    }*/

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

    /*fun searchConnections(
        stationName: String,
        timestamp: Date
    ) {
        binding.stationName = stationName
        dataLoading.postValue(true)
        viewModel.searchConnections(
            stationName,
            timestamp,
            onFoundConnectionsCallback
        ) {
            dataLoading.postValue(false)
        }
    }*/

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

                /*searchConnections(
                    binding.stationName.toString(),
                    cal.time
                )*/
            }

            timePicker.show(childFragmentManager, "SearchConnectionTimePicker")
        }

        datePicker.show(childFragmentManager, "SearchConnectionDatePicker")
    }
}