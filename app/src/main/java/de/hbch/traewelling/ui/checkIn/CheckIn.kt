package de.hbch.traewelling.ui.checkIn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.Dimension
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.DataLoading
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.SwitchWithIconAndText
import de.hbch.traewelling.ui.selectDestination.FromToTextRow
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckIn(
    modifier: Modifier = Modifier,
    checkInViewModel: CheckInViewModel,
    checkInAction: () -> Unit = { }
) {
    var businessSelectionVisible by remember { mutableStateOf(false) }
    var visibilitySelectionVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    val trip by checkInViewModel.trip.observeAsState()
    val selectedVisibility by checkInViewModel.statusVisibility.observeAsState()
    val selectedBusiness by checkInViewModel.statusBusiness.observeAsState()

    if (businessSelectionVisible) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                businessSelectionVisible = false
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                SelectStatusBusinessDialog(
                    businessSelectedAction = {
                        businessSelectionVisible = false
                        checkInViewModel.statusBusiness.postValue(it)
                    }
                )
            }
        }
    }

    if (visibilitySelectionVisible) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                visibilitySelectionVisible = false
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                SelectStatusVisibilityDialog(
                    visibilitySelectedAction = {
                        visibilitySelectionVisible = false
                        checkInViewModel.statusVisibility.postValue(it)
                    }
                )
            }
        }
    }

    if (trip != null) {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                FromToTextRow(
                    modifier = Modifier.fillMaxWidth(),
                    trip = trip!!
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
                        value = statusMessage,
                        onValueChange = {
                            if (it.count() > 280)
                                return@OutlinedTextField
                            statusMessage = it
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
                        text = "${statusMessage.count()}/280",
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

                // Share options
                ShareOptions(
                    modifier = Modifier.fillMaxWidth(),
                    checkInViewModel = checkInViewModel
                )

                ButtonWithIconAndText(
                    stringId = R.string.check_in,
                    drawableId = R.drawable.ic_check_in,
                    onClick = {
                        checkInAction()
                    }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_visibility),
            style = AppTypography.headlineSmall,
            color = LocalColorScheme.current.primary
        )
        StatusVisibility.values().forEach { visibility ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    visibilitySelectedAction(visibility)
                }.padding(8.dp),
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
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.title_select_business),
            style = AppTypography.headlineSmall,
            color = LocalColorScheme.current.primary
        )
        StatusBusiness.values().forEach { business ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    businessSelectedAction(business)
                }.padding(8.dp),
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
        arrivalPlanned = Date(1685365200L * 1000),
        arrivalReal = Date(1685365200L * 1000),
        isCancelled = false
    )
    val station2 = TripStation(
        id = 1,
        name = "Lindau-Reutin",
        rilIdentifier = "MLIR",
        arrivalPlanned = Date(1685365680L * 1000),
        arrivalReal = Date(1685365800L * 1000),
        isCancelled = false
    )
    val station3 = TripStation(
        id = 1,
        name = "Memmingen",
        rilIdentifier = "MM",
        arrivalPlanned = Date(1685368680L * 1000),
        arrivalReal = Date(1685369280L * 1000),
        isCancelled = false
    )
    val station4 = TripStation(
        id = 1,
        name = "München Hbf Gl.27-36 langlanglanglang",
        rilIdentifier = "MH N",
        arrivalPlanned = Date(1685372640L * 1000),
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

    viewModel.trip.value = trip
    viewModel.toot.value = true

    MainTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckIn(
                checkInViewModel = viewModel
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
        }
    }
}
