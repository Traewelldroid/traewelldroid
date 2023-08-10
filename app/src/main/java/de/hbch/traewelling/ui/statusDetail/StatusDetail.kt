package de.hbch.traewelling.ui.statusDetail

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.theme.PolylineColor
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OpenRailwayMapView
import de.hbch.traewelling.ui.composables.getPolylinesFromFeatureCollection
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import org.osmdroid.views.overlay.Polyline
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StatusDetail(
    statusId: Int,
    modifier: Modifier = Modifier,
    statusLoaded: (Status) -> Unit = { },
    statusDeleted: (Status) -> Unit = { },
    statusEdit: (Status) -> Unit = { },
    loggedInUserViewModel: LoggedInUserViewModel? = null
) {
    val statusDetailViewModel: StatusDetailViewModel = viewModel()
    val checkInCardViewModel: CheckInCardViewModel = viewModel()
    var mapExpanded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Status?>(null) }
    val context = LocalContext.current

    LaunchedEffect(status == null) {
        statusDetailViewModel.getStatusById(statusId, {
            val statusDto = it.toStatusDto()
            status = statusDto
            statusLoaded(statusDto)
        }, { })
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val mapModifier = modifier
            .fillMaxHeight(if (mapExpanded) 1.0f else 0.5f)
            .animateContentSize()
        Box(
            modifier = modifier
        ) {
            StatusDetailMap(
                modifier = mapModifier.align(Alignment.TopCenter),
                statusId = statusId,
                statusDetailViewModel = statusDetailViewModel
            )
            IconToggleButton(
                modifier = Modifier.align(Alignment.TopEnd),
                checked = mapExpanded,
                onCheckedChange = {
                    mapExpanded = it
                },
                colors = IconButtonDefaults.filledIconToggleButtonColors()
            ) {
                AnimatedContent(mapExpanded) {
                    val iconSource =
                        if (it) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen
                    Icon(
                        painter = painterResource(id = iconSource),
                        contentDescription = null
                    )
                }
            }
        }
        AnimatedVisibility (!mapExpanded) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckInCard(
                    checkInCardViewModel = checkInCardViewModel,
                    status = status,
                    loggedInUserViewModel = loggedInUserViewModel,
                    onDeleted = statusDeleted,
                    handleEditClicked = statusEdit,
                    displayLongDate = true
                )
                if (status?.productType?.isTrain == true) {
                    ButtonWithIconAndText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.open_with_bahnexpert),
                        drawableId = R.drawable.ic_train,
                        onClick = {
                            val dStatus = status
                            if (dStatus != null) {
                                val intent = CustomTabsIntent.Builder()
                                    .setShowTitle(false)
                                    .build()

                                val trainNo = dStatus.line.split(' ')[0].plus(" ${dStatus.journeyNumber}")
                                val isoDate = DateTimeFormatter.ISO_INSTANT.format(dStatus.departurePlanned)

                                intent.launchUrl(
                                    context,
                                    Uri.parse("https://bahn.expert/details/$trainNo/$isoDate")
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDetailMap(
    modifier: Modifier = Modifier,
    statusId: Int,
    statusDetailViewModel: StatusDetailViewModel
) {
    val color = PolylineColor.toArgb()
    var polylines: List<Polyline> = listOf()
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        OpenRailwayMapView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            onLoad = {
                if (polylines.isEmpty()) {
                    statusDetailViewModel.getPolylineForStatus(statusId, { collection ->
                        polylines = getPolylinesFromFeatureCollection(collection, color)
                        if (polylines.isNotEmpty()) {
                            it.overlays.addAll(polylines)
                            it.zoomToBoundingBox(polylines[0].bounds.increaseByScale(1.1f), false)
                        }
                    }, { })
                }
            }
        )
    }
}

@Preview
@Composable
private fun StatusDetailPreview() {
    MainTheme {
        StatusDetail(
            modifier = Modifier.fillMaxWidth(),
            statusId = 1117900,
        )
    }
}
