package de.hbch.traewelling.ui.searchConnection

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.Hold
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.trip.HafasTrip
import de.hbch.traewelling.databinding.FragmentSearchConnectionBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)
        if (args.date != null) {
            currentSearchDate = args.date!!
        } else {
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.MINUTE, -5)
            currentSearchDate = cal.time
        }

        binding.searchConnectionContent.transitionName = "transition"
        binding.searchConnectionContent.setContent {
            val context = LocalContext.current
            MainTheme {
                var stationName by remember { mutableStateOf(args.stationName) }
                val scrollState = rememberScrollState()
                val trips = remember { mutableStateListOf<HafasTrip>() }
                val times by viewModel.pageTimes.observeAsState()
                var searchDate by remember { mutableStateOf(currentSearchDate) }
                var loading by remember { mutableStateOf(false) }

                LaunchedEffect(stationName, searchDate) {
                    loading = true
                    viewModel.searchConnections(
                        stationName,
                        searchDate,
                        {
                            loading = false
                            trips.clear()
                            trips.addAll(it.data)
                        },
                        { }
                    )
                }

                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardSearchStation(
                        modifier = Modifier.padding(8.dp),
                        searchAction = { station ->
                            stationName = station
                        },
                        searchStationCardViewModel = searchStationCardViewModel,
                        homelandStationData = loggedInUserViewModel.home,
                        recentStationsData = loggedInUserViewModel.lastVisitedStations
                    )
                    ElevatedCard(
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                                text = stringResource(id = R.string.departures_at, stationName),
                                style = AppTypography.headlineSmall
                            )
                            Divider(
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (loading) {
                                DataLoading()
                            } else {
                                SearchConnection(
                                    searchTime = searchDate,
                                    trips = trips,
                                    onPreviousTime = {
                                        val time = times?.previous
                                        time?.let {
                                            searchDate = it
                                        }
                                    },
                                    onNextTime = {
                                        val time = times?.next
                                        time?.let {
                                            searchDate = it
                                        }
                                    },
                                    onTripSelection = { trip ->
                                        checkInViewModel.reset()
                                        checkInViewModel.lineName = trip.line?.name ?: ""
                                        checkInViewModel.tripId = trip.tripId
                                        checkInViewModel.startStationId = trip.station?.id ?: -1
                                        checkInViewModel.departureTime = trip.plannedDeparture

                                        val action =
                                            SearchConnectionFragmentDirections.actionSearchConnectionFragmentToSelectDestinationFragment(
                                                trip.tripId,
                                                trip.finalDestination
                                            )
                                        findNavController().navigate(action)
                                    },
                                    onTimeSelection = {
                                        searchDate = it
                                    },
                                    onHomelandStationSelection = {
                                        setHomelandStation(context, stationName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun setHomelandStation(context: Context, station: String) {
        viewModel.setUserHomelandStation(
            station,
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
}