package de.hbch.traewelling.ui.checkIn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DateTimeSelection
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import de.hbch.traewelling.ui.selectDestination.FromToTextRow
import de.hbch.traewelling.util.getLocalDateString
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckIn(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel,
    eventViewModel: EventViewModel,
    checkInAction: () -> Unit = { },
    initText: String = "",
    isEditMode: Boolean = false,
    changeDestinationAction: () -> Unit = { }
) {
    var businessSelectionVisible by remember { mutableStateOf(false) }
    var visibilitySelectionVisible by remember { mutableStateOf(false) }
    var eventSelectionVisible by remember { mutableStateOf(false) }
    var statusText by rememberSaveable { mutableStateOf(initText) }
    val selectedVisibility by checkInViewModel.statusVisibility.observeAsState()
    val selectedBusiness by checkInViewModel.statusBusiness.observeAsState()
    val activeEvents by eventViewModel.activeEvents.observeAsState()
    val selectedEvent by checkInViewModel.event.observeAsState()
    val dialogModifier = Modifier.fillMaxWidth(0.99f)

    if (businessSelectionVisible) {
        Dialog(
            modifier = dialogModifier,
            onDismissRequest = {
                businessSelectionVisible = false
            }
        ) {
            SelectStatusBusinessDialog(
                businessSelectedAction = {
                    businessSelectionVisible = false
                    checkInViewModel.statusBusiness.postValue(it)
                }
            )
        }
    }

    if (visibilitySelectionVisible) {
        Dialog(
            modifier = dialogModifier,
            onDismissRequest = {
                visibilitySelectionVisible = false
            }
        ) {
            SelectStatusVisibilityDialog(
                visibilitySelectedAction = {
                    visibilitySelectionVisible = false
                    checkInViewModel.statusVisibility.postValue(it)
                }
            )
        }
    }

    if (eventSelectionVisible && activeEvents !== null) {
        Dialog(
            modifier = dialogModifier,
            onDismissRequest = {
                eventSelectionVisible = false
            }
        ) {
            SelectEventDialog(
                activeEvents = activeEvents!!,
                eventSelectedAction = {
                    checkInViewModel.event.postValue(it)
                    eventSelectionVisible = false
                }
            )
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            FromToTextRow(
                modifier = Modifier.fillMaxWidth(),
                category = checkInViewModel.category,
                lineName = checkInViewModel.lineName,
                destination = checkInViewModel.destination
            )

            // Text field
            Column(
                horizontalAlignment = Alignment.End
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(
                            min = 72.dp,
                            max = Dp.Unspecified
                        ),
                    value = statusText,
                    onValueChange = {
                        if (it.count() > 280)
                            return@OutlinedTextField
                        statusText = it
                        checkInViewModel.message.postValue(it)
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.status_message)
                        )
                    }
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "${statusText.count()}/280",
                    style = AppTypography.labelSmall
                )
            }

            // Option buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val optionButtonModifier = Modifier
                    .weight(1f)

                if (selectedVisibility != null) {
                    OutlinedButtonWithIconAndText(
                        modifier = optionButtonModifier,
                        stringId = selectedVisibility!!.getTitle(),
                        drawableId = selectedVisibility!!.getIcon(),
                        onClick = {
                            visibilitySelectionVisible = true
                        }
                    )
                }
                if (selectedBusiness != null) {
                    OutlinedButtonWithIconAndText(
                        modifier = optionButtonModifier,
                        stringId = selectedBusiness!!.getTitle(),
                        drawableId = selectedBusiness!!.getIcon(),
                        onClick = {
                            businessSelectionVisible = true
                        }
                    )
                }
            }

            // Event button
            if (!isEditMode && activeEvents?.isNotEmpty() == true) {
                OutlinedButtonWithIconAndText(
                    modifier = Modifier.fillMaxWidth(),
                    drawableId = if (selectedEvent == null)
                            R.drawable.ic_calendar
                        else
                            R.drawable.ic_calendar_checked,
                    text = selectedEvent?.name ?: stringResource(id = R.string.title_select_event),
                    onClick = {
                        eventSelectionVisible = true
                    }
                )
            }

            // Share options
            if (!isEditMode) {
                ShareOptions(
                    modifier = Modifier.fillMaxWidth(),
                    checkInViewModel = checkInViewModel
                )
            }

            // Manual time overwrites
            if (isEditMode) {
                val currentDateTime = ZonedDateTime.now()
                val plannedDeparture = checkInViewModel.departureTime
                if (plannedDeparture != null && currentDateTime.isAfter(plannedDeparture)) {
                    DateTimeSelection(
                        initDate = checkInViewModel.manualDepartureTime,
                        plannedDate = checkInViewModel.departureTime,
                        label = R.string.manual_departure,
                        modifier = Modifier.fillMaxWidth(),
                        dateSelected = { checkInViewModel.manualDepartureTime = it }
                    )
                }
                val plannedArrival = checkInViewModel.arrivalTime
                if (plannedArrival != null && currentDateTime.isAfter(plannedArrival)) {
                    DateTimeSelection(
                        initDate = checkInViewModel.manualArrivalTime,
                        plannedDate = checkInViewModel.arrivalTime,
                        label = R.string.manual_arrival,
                        modifier = Modifier.fillMaxWidth(),
                        dateSelected = { checkInViewModel.manualArrivalTime = it }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditMode) {
                    ButtonWithIconAndText(
                        stringId = R.string.change_destination,
                        drawableId = R.drawable.ic_edit,
                        onClick = changeDestinationAction
                    )
                } else {
                    Box {}
                }
                var isCheckingIn by remember { mutableStateOf(false) }
                ButtonWithIconAndText(
                    stringId = if (isEditMode) R.string.save else R.string.check_in,
                    drawableId = R.drawable.ic_check_in,
                    onClick = {
                        checkInViewModel.message.postValue(statusText)
                        checkInAction()
                        isCheckingIn = true
                    },
                    isLoading = isCheckingIn
                )
            }
        }
    }
}

@Composable
private fun SelectStatusVisibilityDialog(
    visibilitySelectedAction: (StatusVisibility) -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_visibility),
            style = AppTypography.titleLarge,
            color = LocalColorScheme.current.primary
        )
        StatusVisibility.values().forEach { visibility ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        visibilitySelectedAction(visibility)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = visibility.getIcon()),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = visibility.getTitle()),
                    style = AppTypography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SelectStatusBusinessDialog(
    businessSelectedAction: (StatusBusiness) -> Unit = { }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_business),
            style = AppTypography.titleLarge,
            color = LocalColorScheme.current.primary
        )
        StatusBusiness.values().forEach { business ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        businessSelectedAction(business)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = business.getIcon()),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = business.getTitle()),
                    style = AppTypography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SelectEventDialog(
    activeEvents: List<Event?>,
    eventSelectedAction: (Event?) -> Unit = { }
) {
    val events = mutableListOf<Event?>(null)
    events.addAll(activeEvents)
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_event),
            style = AppTypography.titleLarge,
            color = LocalColorScheme.current.primary
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = stringResource(id = R.string.hint_event_missing),
            style = AppTypography.labelLarge
        )
        events.forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        eventSelectedAction(event)
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Icon(
                        painter = painterResource(id =
                            if (event == null)
                                R.drawable.ic_remove
                            else
                                R.drawable.ic_calendar
                        ),
                        contentDescription = null
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = event?.name ?: stringResource(id = R.string.reset_selection),
                        style = AppTypography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (event == null) {
                            stringResource(R.string.no_event_check_in)
                        } else {
                            stringResource(
                                id = R.string.date_range,
                                getLocalDateString(event.begin),
                                getLocalDateString(event.end)
                            )
                        },
                        style = AppTypography.titleSmall
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_select),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ShareOptions(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel
) {
    val shareOnMastodon by checkInViewModel.toot.observeAsState(false)
    val chainShareOnMastodon by checkInViewModel.chainToot.observeAsState(false)
    var toot by remember { mutableStateOf(shareOnMastodon) }
    var chainToot by remember { mutableStateOf(chainShareOnMastodon) }

    val chainTootAction: (Boolean) -> Unit = {
        chainToot = it
        checkInViewModel.chainToot.postValue(it)
    }
    val tootAction: (Boolean) -> Unit = {
        toot = it
        checkInViewModel.toot.postValue(it)

        if (!it) {
            chainTootAction(false)
        }
    }

    Column(
        modifier = modifier
    ) {
        SwitchWithIconAndText(
            modifier = Modifier.fillMaxWidth(),
            checked = toot,
            onCheckedChange = {
                tootAction(it)
            },
            drawableId = R.drawable.ic_mastodon,
            stringId = R.string.send_toot
        )
        AnimatedVisibility(shareOnMastodon) {
            SwitchWithIconAndText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                checked = chainToot,
                onCheckedChange =  {
                    chainTootAction(it)
                },
                drawableId = R.drawable.ic_chain,
                stringId = R.string.chain_toot
            )
        }
    }
}

@Preview
@Composable
private fun CheckInPreview() {
    val viewModel = CheckInViewModel()
    val station1 = TripStation(
        id = 0,
        name = "Bregenz",
        rilIdentifier = null,
        departurePlanned = Instant.ofEpochMilli(1685365200L * 1000).atZone(ZoneId.systemDefault()),
        departureReal = Instant.ofEpochMilli(1685365200L * 1000).atZone(ZoneId.systemDefault()),
        arrivalPlanned = Instant.ofEpochMilli(1685365200L * 1000).atZone(ZoneId.systemDefault()),
        arrivalReal = Instant.ofEpochMilli(1685365200L * 1000).atZone(ZoneId.systemDefault()),
        isCancelled = false
    )
    val station2 = TripStation(
        id = 1,
        name = "Lindau-Reutin",
        rilIdentifier = "MLIR",
        departurePlanned = Instant.ofEpochMilli(1685365680L * 1000).atZone(ZoneId.systemDefault()),
        departureReal = Instant.ofEpochMilli(1685365800L * 1000).atZone(ZoneId.systemDefault()),
        arrivalPlanned = Instant.ofEpochMilli(1685365680L * 1000).atZone(ZoneId.systemDefault()),
        arrivalReal = Instant.ofEpochMilli(1685365800L * 1000).atZone(ZoneId.systemDefault()),
        isCancelled = false
    )
    val station3 = TripStation(
        id = 1,
        name = "Memmingen",
        rilIdentifier = "MM",
        departurePlanned = Instant.ofEpochMilli(1685368680L * 1000).atZone(ZoneId.systemDefault()),
        departureReal = Instant.ofEpochMilli(1685369280L * 1000).atZone(ZoneId.systemDefault()),
        arrivalPlanned = Instant.ofEpochMilli(1685368680L * 1000).atZone(ZoneId.systemDefault()),
        arrivalReal = Instant.ofEpochMilli(1685369280L * 1000).atZone(ZoneId.systemDefault()),
        isCancelled = false
    )
    val station4 = TripStation(
        id = 1,
        name = "München Hbf Gl.27-36 langlanglanglang",
        rilIdentifier = "MH N",
        departurePlanned= Instant.ofEpochMilli(1685372640L * 1000).atZone(ZoneId.systemDefault()),
        departureReal = null,
        arrivalPlanned = Instant.ofEpochMilli(1685372640L * 1000).atZone(ZoneId.systemDefault()),
        arrivalReal = null,
        isCancelled = true
    )
    val stopoverList = listOf(
        station1,
        station2,
        station3,
        station4
    )

    val trip = Trip(
        0,
        ProductType.NATIONAL_EXPRESS,
        "ECE 193",
        "Zürich HB",
        "Memmingen",
        stopovers = stopoverList
    )

    viewModel.lineName = trip.lineName
    viewModel.destination = trip.destination
    viewModel.category = trip.category
    viewModel.toot.value = true

    val eventViewModel = EventViewModel()
    eventViewModel.activeEvents.value = listOf(
        Event(
            0,
            "Tolle Veranstaltung",
            "tv",
            "#toll",
            "Host",
            "url",
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            null
        )
    )

    MainTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckIn(
                checkInViewModel = viewModel,
                isEditMode = false,
                eventViewModel = eventViewModel
            )
            CheckIn(
                checkInViewModel = viewModel,
                isEditMode = true,
                eventViewModel = eventViewModel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DialogPreviews() {
    MainTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectStatusBusinessDialog()
            SelectStatusVisibilityDialog()
            SelectEventDialog(activeEvents = listOf())
        }
    }
}
