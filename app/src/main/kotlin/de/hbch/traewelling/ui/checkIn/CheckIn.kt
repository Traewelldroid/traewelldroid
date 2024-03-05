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
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.shared.BottomSearchViewModel
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DateTimeSelection
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import de.hbch.traewelling.ui.selectDestination.FromToTextRow
import de.hbch.traewelling.util.checkAnyUsernames
import de.hbch.traewelling.util.getLocalDateString
import de.hbch.traewelling.util.useDebounce
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckIn(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel,
    eventViewModel: EventViewModel,
    bottomSearchViewModel: BottomSearchViewModel,
    checkInAction: (Boolean, Boolean) -> Unit = { _, _ -> },
    initText: String = "",
    isEditMode: Boolean = false,
    changeDestinationAction: () -> Unit = { }
) {
    val secureStorage = SecureStorage(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    var enableTrwlCheckIn by rememberSaveable { mutableStateOf(secureStorage.getObject(SharedValues.SS_TRWL_AUTO_LOGIN, Boolean::class.java) ?: true) }
    val travelynxConfigured = secureStorage.getObject(SharedValues.SS_TRAVELYNX_TOKEN, String::class.java)?.isNotBlank() ?: false
    var enableTravelynxCheckIn by rememberSaveable { mutableStateOf(secureStorage.getObject(SharedValues.SS_TRAVELYNX_AUTO_CHECKIN, Boolean::class.java) ?: false) }

    var businessSelectionVisible by remember { mutableStateOf(false) }
    var visibilitySelectionVisible by remember { mutableStateOf(false) }
    var eventSelectionVisible by remember { mutableStateOf(false) }

    var statusText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(text = initText)) }
    val userSearchQuery by remember { derivedStateOf {
        val matches = statusText.text.checkAnyUsernames()
        matches.firstOrNull { it.range.contains(statusText.selection.min - 1) || it.range.contains(statusText.selection.max + 1) }?.value?.replace("@", "")
    } }
    userSearchQuery.useDebounce(
        onChange = { query ->
            if (query == null) {
                bottomSearchViewModel.reset()
            } else {
                bottomSearchViewModel.registerClickHandler { selection ->
                    val firstMatch = statusText.text.checkAnyUsernames().first { it.range.contains(statusText.selection.min - 1) || it.range.contains(statusText.selection.max + 1) }
                    statusText = statusText.copy(
                        text = statusText.text.replaceRange(firstMatch.range.first, firstMatch.range.last + 1, selection),
                        selection = TextRange(firstMatch.range.first + selection.length)
                    )
                }
                coroutineScope.launch {
                    bottomSearchViewModel.searchUsers(query)
                }
            }
        },
        delayMillis = 500L
    )

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
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
                    lineId = checkInViewModel.lineId,
                    operatorCode = checkInViewModel.operatorCode,
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
                            if (it.text.count() > 280)
                                return@OutlinedTextField
                            statusText = it
                            checkInViewModel.message.postValue(it.text)
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.status_message)
                            )
                        }
                    )
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = "${statusText.text.count()}/280",
                        style = AppTypography.labelSmall
                    )
                }

                if (travelynxConfigured && !isEditMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            SwitchWithIconAndText(
                                checked = enableTrwlCheckIn,
                                onCheckedChange = {
                                    enableTrwlCheckIn = it
                                },
                                drawableId = R.drawable.ic_trwl,
                                stringId = R.string.check_in_trwl
                            )
                            SwitchWithIconAndText(
                                checked = enableTravelynxCheckIn,
                                onCheckedChange = {
                                    enableTravelynxCheckIn = it
                                },
                                drawableId = R.drawable.ic_travelynx,
                                stringId = R.string.check_in_travelynx
                            )
                        }
                    }
                }

                AnimatedVisibility(enableTrwlCheckIn) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
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
                                    stringId = selectedVisibility!!.title,
                                    drawableId = selectedVisibility!!.icon,
                                    onClick = {
                                        visibilitySelectionVisible = true
                                    }
                                )
                            }
                            if (selectedBusiness != null) {
                                OutlinedButtonWithIconAndText(
                                    modifier = optionButtonModifier,
                                    stringId = selectedBusiness!!.title,
                                    drawableId = selectedBusiness!!.icon,
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
                                text = selectedEvent?.name
                                    ?: stringResource(id = R.string.title_select_event),
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
                    }
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
                            checkInViewModel.message.value = statusText.text
                            checkInAction(enableTrwlCheckIn, (travelynxConfigured && enableTravelynxCheckIn))
                            isCheckingIn = true
                        },
                        isLoading = isCheckingIn,
                        isEnabled = (enableTrwlCheckIn || (travelynxConfigured && enableTravelynxCheckIn))
                    )
                }
            }
        }
        Box(modifier = Modifier.padding(top = 4.dp))
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
        StatusVisibility.entries.forEach { visibility ->
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
                    painter = painterResource(id = visibility.icon),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = visibility.title),
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
        StatusBusiness.entries.forEach { business ->
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
                    painter = painterResource(id = business.icon),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = business.title),
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
