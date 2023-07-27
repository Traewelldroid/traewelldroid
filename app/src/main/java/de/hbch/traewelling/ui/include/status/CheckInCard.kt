package de.hbch.traewelling.ui.include.status

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.theme.StarYellow
import de.hbch.traewelling.ui.selectDestination.getLocalDateTimeString
import de.hbch.traewelling.ui.selectDestination.getLocalTimeString
import de.hbch.traewelling.ui.user.getDurationString
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInCard(
    modifier: Modifier = Modifier,
    checkInCardViewModel: CheckInCardViewModel,
    status: Status?,
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    stationSelected: (String, Date?) -> Unit = { _, _ -> },
    userSelected: (String) -> Unit = { },
    statusSelected: (Int) -> Unit = { },
    handleEditClicked: (Status) -> Unit = { },
    onDeleted: (Status) -> Unit = { }
) {
    val primaryColor = LocalColorScheme.current.primary
    if(status != null) {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            onClick = {
                statusSelected(status.statusId)
            }
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (
                        perlschnurTop,
                        perlschnurConnection,
                        perlschnurBottom,
                        stationRowTop,
                        stationRowBottom,
                        content
                    ) = createRefs()

                    // Perlschnur
                    Icon(
                        modifier = Modifier
                            .constrainAs(perlschnurTop) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            }
                            .size(20.dp),
                        painter = painterResource(id = R.drawable.ic_perlschnur_main),
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Image(
                        modifier = Modifier.constrainAs(perlschnurConnection) {
                            start.linkTo(perlschnurTop.start)
                            end.linkTo(perlschnurTop.end)
                            top.linkTo(perlschnurTop.bottom)
                            bottom.linkTo(perlschnurBottom.top)
                            height = Dimension.fillToConstraints
                            width = Dimension.value(2.dp)
                        },
                        painter = painterResource(id = R.drawable.ic_perlschnur_connection),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        modifier = Modifier
                            .constrainAs(perlschnurBottom) {
                                start.linkTo(parent.start)
                                bottom.linkTo(parent.bottom)
                            }
                            .size(20.dp),
                        painter = painterResource(id = R.drawable.ic_perlschnur_main),
                        contentDescription = null,
                        tint = primaryColor
                    )

                    // Station row top
                    StationRow(
                        modifier = Modifier.constrainAs(stationRowTop) {
                            start.linkTo(perlschnurTop.end, 8.dp)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            width = Dimension.fillToConstraints
                        },
                        stationName = status.origin,
                        timePlanned = status.departurePlanned,
                        timeReal = status.departureOverwritten ?: status.departureReal,
                        stationSelected = stationSelected
                    )

                    // Station row bottom
                    StationRow(
                        modifier = Modifier
                            .constrainAs(stationRowBottom) {
                                start.linkTo(perlschnurBottom.end, 12.dp)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.fillToConstraints
                            },
                        stationName = status.destination,
                        timePlanned = status.arrivalPlanned,
                        timeReal = status.arrivalOverwritten ?: status.arrivalReal,
                        verticalAlignment = Alignment.Bottom,
                        stationSelected = stationSelected
                    )

                    // Main content
                    CheckInCardContent(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .constrainAs(content) {
                                top.linkTo(stationRowTop.bottom)
                                bottom.linkTo(stationRowBottom.top)
                                start.linkTo(stationRowTop.start)
                                end.linkTo(stationRowTop.end)
                                width = Dimension.fillToConstraints
                            },
                        productType = status.productType,
                        line = status.line,
                        kilometers = status.distance,
                        duration = status.duration,
                        statusBusiness = status.business,
                        message = status.message,
                        journeyNumber = status.journeyNumber
                    )
                }
                val progress = calculateProgress(
                    from = status.departureReal ?: status.departurePlanned,
                    to = status.arrivalReal ?: status.arrivalPlanned
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    progress = if (progress.isNaN()) 1f else progress
                )
                CheckInCardFooter(
                    modifier = Modifier.fillMaxWidth(),
                    statusId = status.statusId,
                    username = status.username,
                    createdAt = status.createdAt,
                    liked = status.liked,
                    likeCount = status.likeCount,
                    visibility = status.visibility,
                    isOwnStatus =
                    (loggedInUserViewModel?.loggedInUser?.value?.id ?: -1) == status.userId,
                    eventName = status.eventName,
                    checkInCardViewModel = checkInCardViewModel,
                    userSelected = userSelected,
                    handleEditClicked = {
                        handleEditClicked(status)
                    },
                    handleDeleteClicked = {
                        checkInCardViewModel.deleteStatus(status.statusId, {
                            onDeleted(status)
                        }, { })
                    }
                )
            }
        }
    }
}

@Composable
private fun calculateProgress(
    from: Date,
    to: Date
): Float {
    val currentDate = Date()
    // Default cases
    if (currentDate > to) {
        return 1f
    } else if (currentDate < from) {
        return 0f
    }

    val fullTimeSpanMillis = to.time - from.time
    val elapsedTimeSpanMillis = currentDate.time - from.time

    return elapsedTimeSpanMillis.toFloat() / fullTimeSpanMillis.toFloat()
}

@Composable
private fun StationRow(
    modifier: Modifier = Modifier,
    stationName: String,
    timePlanned: Date,
    timeReal: Date?,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    stationSelected: (String, Date?) -> Unit = { _, _ -> }
) {
    val primaryColor = LocalColorScheme.current.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = verticalAlignment
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier
                    .clickable { stationSelected(stationName, null) },
                text = stationName,
                style = AppTypography.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = primaryColor
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            val difference = TimeUnit.MILLISECONDS.toMinutes(
                (timeReal?.time ?: timePlanned.time) - timePlanned.time
            )
            val hasDelay = difference > 0
            val displayedDate =
                if (hasDelay && timeReal != null)
                    timeReal
                else
                    timePlanned
            Text(
                modifier = Modifier.clickable { stationSelected(stationName, displayedDate) },
                text = getLocalTimeString(
                    date = displayedDate
                ),
                color = primaryColor,
                style = AppTypography.titleLarge
            )
            if (hasDelay) {
                Text(
                    text = getLocalTimeString(
                        date = timePlanned
                    ),
                    textDecoration = TextDecoration.LineThrough,
                    style = AppTypography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckInCardContent(
    modifier: Modifier = Modifier,
    productType: ProductType,
    line: String,
    journeyNumber: Int?,
    kilometers: Int,
    duration: Int,
    statusBusiness: StatusBusiness,
    message: String?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusDetailsRow(
            productType = productType,
            line = line,
            journeyNumber = journeyNumber,
            kilometers = kilometers,
            duration = duration,
            statusBusiness = statusBusiness
        )
        if (!message.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_quote),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = message,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusDetailsRow(
    productType: ProductType,
    line: String,
    journeyNumber: Int?,
    kilometers: Int,
    duration: Int,
    statusBusiness: StatusBusiness,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = productType.getIcon()),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = line,
            style = AppTypography.bodyLarge,
            fontWeight = FontWeight.ExtraBold
        )
        if (journeyNumber != null && !line.contains(journeyNumber.toString())) {
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "($journeyNumber)",
                style = AppTypography.bodySmall
            )
        }
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = getFormattedDistance(kilometers),
            style = AppTypography.bodySmall
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = getDurationString(duration = duration),
            style = AppTypography.bodySmall
        )
        Icon(
            modifier = Modifier.padding(start = 8.dp),
            painter = painterResource(id = statusBusiness.getIcon()),
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
private fun CheckInCardFooter(
    modifier: Modifier = Modifier,
    statusId: Int,
    username: String,
    createdAt: Date,
    visibility: StatusVisibility,
    liked: Boolean?,
    likeCount: Int?,
    isOwnStatus: Boolean = false,
    eventName: String?,
    checkInCardViewModel: CheckInCardViewModel,
    userSelected: (String) -> Unit = { },
    handleEditClicked: () -> Unit = { },
    handleDeleteClicked: () -> Unit = { }
) {
    var likedState by remember { mutableStateOf(liked ?: false) }
    var likeCountState by remember { mutableStateOf(likeCount ?: 0) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (liked != null && likeCount != null) {
                Row(
                    modifier = Modifier
                        .clickable {
                            if (likedState) {
                                checkInCardViewModel.deleteFavorite(statusId) {
                                    likedState = false
                                    likeCountState--
                                }
                            } else {
                                checkInCardViewModel.createFavorite(statusId) {
                                    likedState = true
                                    likeCountState++
                                }
                            }
                        }
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedContent(targetState = likedState) {
                        val icon = if (it) R.drawable.ic_faved else R.drawable.ic_not_faved
                        Icon(
                            painterResource(id = icon),
                            contentDescription = null,
                            tint = StarYellow
                        )
                    }
                    Text(
                        text = likeCountState.toString()
                    )
                }
            } else {
                Box { }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .clickable { userSelected(username) }
                        .padding(2.dp),
                    text = stringResource(
                        id = R.string.check_in_user_time,
                        username,
                        getLocalDateTimeString(
                            date = createdAt
                        )
                    ),
                    textAlign = TextAlign.End,
                    style = AppTypography.labelLarge
                )
                Icon(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    painter = painterResource(id = visibility.getIcon()),
                    contentDescription = null
                )
            }
            if (isOwnStatus) {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                menuExpanded = true
                            }
                            .padding(2.dp),
                        painter = painterResource(id = R.drawable.ic_more),
                        contentDescription = null,
                        tint = LocalColorScheme.current.primary
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.title_edit)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = null
                                )
                            },
                            onClick = handleEditClicked
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.delete)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = null
                                )
                            },
                            onClick = handleDeleteClicked
                        )
                    }
                }
            }
        }
    }

    // Event
    if (!eventName.isNullOrEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null
            )
            Text(
                text = eventName,
                style = AppTypography.labelMedium
            )
        }
    }
}

@Composable
fun getFormattedDistance(distance: Int): String {
    val roundedDistance =
        if (distance < 1000)
            Measure(distance, MeasureUnit.METER)
        else
            Measure(distance / 1000, MeasureUnit.KILOMETER)

    return MeasureFormat
        .getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
        .formatMeasures(roundedDistance)
}

@Preview
@Composable
private fun CheckInCardPreview() {
    MainTheme {
        val checkInCardViewModel = CheckInCardViewModel()
        val status = Status(
            0,
            "Start Hbf",
            123,
            Date(),
            Date(),
            Date(),
            "Ende Hp",
            123,
            Date(),
            Date(),
            Date(),
            ProductType.TRAM,
            "lalal",
            "STB U1",
            4711,
            1234,
            1234,
            StatusBusiness.COMMUTE,
            "Testnachricht 123456789",
            true,
            10,
            0,
            "username",
            Date(),
            StatusVisibility.PRIVATE,
            "Tolle Veranstaltung!"
        )
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            CheckInCard(
                checkInCardViewModel = checkInCardViewModel,
                status = status
            )
        }
    }
}
