package de.hbch.traewelling.ui.checkInResult

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.logging.Logger
import de.hbch.traewelling.providers.checkin.CheckInResult
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.StarYellow
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.ui.include.status.StatusDetailsRow
import de.hbch.traewelling.ui.tag.StatusTags
import de.hbch.traewelling.util.ReviewRequest
import de.hbch.traewelling.util.shareStatus

@Composable
fun CheckInResultView(
    checkInViewModel: CheckInViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    modifier: Modifier = Modifier,
    onStatusSelected: (Int) -> Unit = { },
    onCheckInForced: () -> Unit = { },
    onFloatingActionButtonChange: (Int, Int) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val traewellingResponse = checkInViewModel.trwlCheckInResponse
    val travelynxResponse = checkInViewModel.travelynxCheckInResponse

    if (traewellingResponse?.result == CheckInResult.SUCCESSFUL || travelynxResponse?.result == CheckInResult.SUCCESSFUL) {
        onFloatingActionButtonChange(R.drawable.ic_check_in, R.string.finish)
    } else {
        onFloatingActionButtonChange(R.drawable.ic_cancel, R.string.abort)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (traewellingResponse != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = traewellingResponse.result.getIcon()),
                    tint = traewellingResponse.result.getColor(),
                    contentDescription = stringResource(traewellingResponse.result.getString()),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "Träwelling",
                    modifier = Modifier.padding(start = 12.dp),
                    style = AppTypography.titleLarge
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = traewellingResponse.result.getString()),
                textAlign = TextAlign.Center
            )

            when (traewellingResponse.result) {
                // Show row with status details and users also in connection
                CheckInResult.SUCCESSFUL -> {
                    SuccessfulCheckInResult(
                        checkInViewModel = checkInViewModel,
                        onStatusSelected = onStatusSelected,
                        loggedInUserViewModel = loggedInUserViewModel
                    )
                }
                // Show button with enforcing check-in
                CheckInResult.CONFLICTED -> {
                    OutlinedButtonWithIconAndText(
                        modifier = Modifier.fillMaxWidth(),
                        stringId = R.string.force_check_in,
                        onClick = onCheckInForced
                    )
                }
                // Display error message
                CheckInResult.ERROR -> { }
            }
        }
        if (travelynxResponse != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = travelynxResponse.result.getIcon()),
                    tint = travelynxResponse.result.getColor(),
                    contentDescription = stringResource(travelynxResponse.result.getString()),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "travelynx",
                    modifier = Modifier.padding(start = 12.dp),
                    style = AppTypography.titleLarge
                )
            }
            if (travelynxResponse.result == CheckInResult.ERROR) {
                Text(
                    text = stringResource(id = R.string.please_use_website),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SuccessfulCheckInResult(
    checkInViewModel: CheckInViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    onStatusSelected: (Int) -> Unit = { }
) {
    val context = LocalContext.current
    val checkInResponse = checkInViewModel.trwlCheckInResponse

    val reviewRequest = remember { ReviewRequest() }
    var reviewRequested by remember { mutableStateOf(false) }

    LaunchedEffect(reviewRequested) {
        if (!reviewRequested) {
            reviewRequested = true

            val checkInCount = SecureStorage(context)
                .getObject(SharedValues.SS_CHECK_IN_COUNT, Long::class.java) ?: 0L
            if (checkInCount.mod(10) == 2) {
                reviewRequest.request(
                    context,
                    Logger.getInstance()
                ) { }
            }
        }
    }

    if (checkInResponse?.data != null) {
        val journey = checkInResponse.data.status.journey
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusDetailsRow(
                productType = journey.category,
                line = journey.line,
                journeyNumber = journey.journeyNumber,
                kilometers = journey.distance,
                duration = journey.duration,
                statusBusiness = checkInResponse.data.status.business
            )
            Text(
                text = stringResource(id = R.string.display_points, checkInResponse.data.points.points),
                fontWeight = FontWeight.ExtraBold
            )
            StatusTags(
                statusId = checkInResponse.data.status.id,
                isOwnStatus = true,
                defaultVisibility = loggedInUserViewModel.defaultStatusVisibility
            )
            val pointReasonText = checkInResponse.data.points.calculation.reason.getExplanation()
            if (pointReasonText != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = pointReasonText),
                    color = StarYellow,
                    textAlign = TextAlign.Center
                )
            }
            ButtonWithIconAndText(
                stringId = R.string.title_share,
                drawableId = R.drawable.ic_share,
                onClick = {
                    context.shareStatus(checkInResponse.data.status)
                }
            )
            if (checkInResponse.data.coTravellers.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(id = R.string.co_travellers)
                    )
                    checkInResponse.data.coTravellers.forEach { status ->
                        CoTraveller(
                            status = status,
                            modifier = Modifier.clickable { onStatusSelected(status.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoTraveller(
    status: Status,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfilePicture(
                name = status.username,
                url = status.profilePicture ?: "",
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
            )
            Column {
                Text(
                    text = "@${status.username}",
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${status.journey.origin.name} → ${status.journey.destination.name}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_select),
            contentDescription = null
        )
    }
}
