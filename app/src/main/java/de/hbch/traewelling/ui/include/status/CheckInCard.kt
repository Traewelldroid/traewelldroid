package de.hbch.traewelling.ui.include.status

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import de.hbch.traewelling.ui.selectDestination.getDelayColor
import de.hbch.traewelling.ui.selectDestination.getLocalDateTimeString
import de.hbch.traewelling.ui.selectDestination.getLocalTimeString
import de.hbch.traewelling.ui.user.getDurationString
import java.util.Date
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInCard(
    modifier: Modifier = Modifier,
    checkInCardViewModel: CheckInCardViewModel,
    status: Status,
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    stationSelected: (String, Date?) -> Unit = { _, _ -> }
) {
    val primaryColor = LocalColorScheme.current.primary

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { }
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
                    timeReal = status.departureReal,
                    stationSelected = stationSelected
                )

                // Station row bottom
                StationRow(
                    modifier = Modifier.constrainAs(stationRowBottom) {
                        start.linkTo(perlschnurBottom.end, 12.dp)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
                    stationName = status.destination,
                    timePlanned = status.arrivalPlanned,
                    timeReal = status.arrivalReal,
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
                    message = status.message
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
                checkInCardViewModel = checkInCardViewModel
            )
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

    return elapsedTimeSpanMillis.toFloat() / fullTimeSpanMillis.toFloat();
}

@Composable
private fun StationRow(
    modifier: Modifier = Modifier,
    stationName: String,
    timePlanned: Date,
    timeReal: Date?,
    stationSelected: (String, Date?) -> Unit = { _, _ -> }
) {
    val primaryColor = LocalColorScheme.current.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .clickable { stationSelected(stationName, null) },
            text = stationName,
            style = AppTypography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            color = primaryColor
        )
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
    kilometers: Int,
    duration: Int,
    statusBusiness: StatusBusiness,
    message: String?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
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
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(id = R.string.format_distance_kilometers, kilometers / 1000),
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
    checkInCardViewModel: CheckInCardViewModel
) {
    var likedState by rememberSaveable { mutableStateOf(liked ?: false) }
    var likeCountState by rememberSaveable { mutableStateOf(likeCount ?: 0) }

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
                        .clickable { }
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
                Icon(
                    modifier = Modifier
                        .clickable { }
                        .padding(2.dp),
                    painter = painterResource(id = R.drawable.ic_more),
                    contentDescription = null,
                    tint = LocalColorScheme.current.primary
                )
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

@Preview
@Composable
private fun CheckInCardPreview() {
    MainTheme {
        val checkInCardViewModel = CheckInCardViewModel()
        val status = Status(
            0,
            "Start Hbf",
            Date(),
            Date(),
            "Ende Hp",
            Date(),
            Date(),
            ProductType.TRAM,
            "STB U1",
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
