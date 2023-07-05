package de.hbch.traewelling.ui.selectDestination

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Trip
import de.hbch.traewelling.api.dtos.TripStation
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.DataLoading
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getDateTimeInstance
import java.text.DateFormat.getTimeInstance
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SelectDestination(
    modifier: Modifier = Modifier,
    stationSelectedAction: (TripStation) -> Unit = { },
    tripData: LiveData<Trip?>
) {
    val trip by tripData.observeAsState()

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (trip == null) {
                DataLoading()
            } else {
                FromToTextRow(
                    category = trip!!.category,
                    lineName = trip!!.lineName,
                    destination = trip!!.destination
                )
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    trip!!.stopovers.forEachIndexed { index, tripStation ->
                        TravelStopListItem(
                            modifier = Modifier.clickable(onClick = {
                                if (!tripStation.isCancelled) {
                                    stationSelectedAction(tripStation)
                                }
                            }),
                            station = tripStation,
                            isLastStop = index == trip!!.stopovers.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FromToTextRow(
    modifier: Modifier = Modifier,
    category: ProductType?,
    lineName: String,
    destination: String
) {
    Row(
        modifier = modifier
    ) {
        if (category != null) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = category.getIcon()),
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = stringResource(
                R.string.line_destination,
                lineName,
                destination
            ),
            style = AppTypography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Composable
private fun TravelStopListItem(
    modifier: Modifier = Modifier,
    station: TripStation,
    isLastStop: Boolean = false
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val (
            perlschnurTop,
            perlschnurMain,
            perlschnurBottom,
            stationName,
            time
        ) = createRefs()

        // Perlschnur
        Image(
            modifier = Modifier.constrainAs(perlschnurTop) {
                top.linkTo(parent.top)
                bottom.linkTo(perlschnurMain.top)
                start.linkTo(perlschnurMain.start)
                end.linkTo(perlschnurMain.end)
                height = Dimension.fillToConstraints
                width = Dimension.value(2.dp)
            },
            painter = painterResource(id = R.drawable.ic_perlschnur_connection),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Image(
            modifier = Modifier
                .size(20.dp)
                .constrainAs(perlschnurMain) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            painter = painterResource(id = R.drawable.ic_perlschnur_main),
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalColorScheme.current.primary)
        )
        val bottomConstraint = Modifier.constrainAs(perlschnurBottom) {
            top.linkTo(perlschnurMain.bottom)
            bottom.linkTo(parent.bottom)
            end.linkTo(perlschnurMain.end)
            start.linkTo(perlschnurMain.start)
            height = Dimension.fillToConstraints
            width = Dimension.value(2.dp)
        }
        if (isLastStop) {
            Box(
                modifier = bottomConstraint
            )
        } else {
            Image(
                modifier = bottomConstraint,
                painter = painterResource(id = R.drawable.ic_perlschnur_connection),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }

        // Station description
        var stationNameText = station.name
        if (station.rilIdentifier != null)
            stationNameText = stationNameText.plus(" [${station.rilIdentifier}]")
        Text(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .constrainAs(stationName) {
                    start.linkTo(perlschnurMain.end, margin = 8.dp)
                    top.linkTo(perlschnurTop.top)
                    bottom.linkTo(perlschnurBottom.bottom)
                    end.linkTo(time.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                },
            text = stationNameText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = AppTypography.titleMedium
        )

        // Time/Cancelled
        Column(
            modifier = Modifier.constrainAs(time) {
                end.linkTo(parent.end)
                top.linkTo(perlschnurTop.top)
                bottom.linkTo(perlschnurBottom.bottom)
            },
            horizontalAlignment = Alignment.End
        ) {
            if (station.isCancelled) {
                Text(
                    text = stringResource(id = R.string.cancelled),
                    color = Color.Red,
                    style = AppTypography.titleMedium
                )
                Text(
                    text = getLocalTimeString(
                        date = station.arrivalPlanned
                    ),
                    textDecoration = TextDecoration.LineThrough,
                    style = AppTypography.labelMedium
                )
            } else {
                Text(
                    text = getLocalTimeString(
                        date = station.arrivalReal ?: station.arrivalPlanned
                    ),
                    color = getDelayColor(
                        real = station.arrivalReal,
                        planned = station.arrivalPlanned
                    ),
                    style = AppTypography.titleMedium
                )
            }
        }
    }
}

@Composable
fun getLocalTimeString(date: Date): String {
    return getTimeInstance(DateFormat.SHORT).format(date)
}

@Composable
fun getLocalDateTimeString(date: Date): String {
    return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(date)
}

@Composable
fun getLocalDateString(date: Date): String {
    return getDateInstance(DateFormat.SHORT).format(date)
}

@Composable
fun getDelayColor(planned: Date, real: Date?): Color {
    val differenceMillis = (real ?: Date()).time - planned.time
    val difference = TimeUnit.MILLISECONDS.toMinutes(differenceMillis)

    val color = when {
        difference <= 0 -> R.color.train_on_time
        difference in 0..5 -> R.color.warning
        else -> R.color.train_delayed
    }

    return colorResource(id = color)
}

@Preview
@Composable
private fun TravelStopListItemPreview() {
    val station1 = TripStation(
        id = 0,
        name = "Bregenz",
        rilIdentifier = null,
        departurePlanned = Date(1685365200L * 1000),
        departureReal = Date(1685365200L * 1000),
        arrivalPlanned = Date(1685365200L * 1000),
        arrivalReal = Date(1685365200L * 1000),
        isCancelled = false
    )
    val station2 = TripStation(
        id = 1,
        name = "Lindau-Reutin",
        rilIdentifier = "MLIR",
        departurePlanned = Date(1685365680L * 1000),
        departureReal = Date(1685365800L * 1000),
        arrivalPlanned = Date(1685365680L * 1000),
        arrivalReal = Date(1685365800L * 1000),
        isCancelled = false
    )
    val station3 = TripStation(
        id = 1,
        name = "Memmingen",
        rilIdentifier = "MM",
        departurePlanned = Date(1685368680L * 1000),
        departureReal = Date(1685369280L * 1000),
        arrivalPlanned = Date(1685368680L * 1000),
        arrivalReal = Date(1685369280L * 1000),
        isCancelled = false
    )
    val station4 = TripStation(
        id = 1,
        name = "München Hbf Gl.27-36 langlanglanglang",
        rilIdentifier = "MH N",
        departurePlanned= Date(1685372640L * 1000),
        departureReal = null,
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
        "München Hbf Gl.27-36 langlanglanglang",
        stopovers = stopoverList
    )

    MainTheme {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectDestination(tripData = MutableLiveData(trip))
            SelectDestination(tripData = MutableLiveData(null))
        }
    }
}
