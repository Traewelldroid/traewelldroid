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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.StarYellow
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import de.hbch.traewelling.ui.include.status.StatusDetailsRow
import de.hbch.traewelling.ui.tag.StatusTags
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
    val checkInResult = checkInViewModel.checkInResult
    if (checkInResult != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(75.dp),
                painter = painterResource(id = checkInResult.getIcon()),
                tint = checkInResult.getColor(),
                contentDescription = null
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = checkInResult.getString()),
                textAlign = TextAlign.Center
            )

            when (checkInResult) {
                // Show row with status details and users also in connection
                CheckInResult.SUCCESSFUL -> {
                    onFloatingActionButtonChange(R.drawable.ic_check_in, R.string.finish)
                    SuccessfulCheckInResult(
                        checkInViewModel = checkInViewModel,
                        onStatusSelected = onStatusSelected,
                        loggedInUserViewModel = loggedInUserViewModel
                    )
                }
                // Show button with enforcing check-in
                CheckInResult.CONFLICTED -> {
                    onFloatingActionButtonChange(R.drawable.ic_cancel, R.string.abort)
                    OutlinedButtonWithIconAndText(
                        modifier = Modifier.fillMaxWidth(),
                        stringId = R.string.force_check_in,
                        onClick = onCheckInForced
                    )
                }
                // Display error message
                CheckInResult.ERROR -> {
                    onFloatingActionButtonChange(R.drawable.ic_cancel, R.string.abort)
                }
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
    val checkInResponse = checkInViewModel.checkInResponse
    if (checkInResponse != null) {
        val journey = checkInResponse.status.journey
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
                statusBusiness = checkInResponse.status.business
            )
            Text(
                text = stringResource(id = R.string.display_points, checkInResponse.points.points),
                fontWeight = FontWeight.ExtraBold
            )
            StatusTags(
                statusId = checkInResponse.status.id,
                isOwnStatus = true,
                defaultVisibility = loggedInUserViewModel.defaultStatusVisibility
            )
            val pointReasonText = checkInResponse.points.calculation.reason.getExplanation()
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
                    context.shareStatus(checkInResponse.status.toStatusDto())
                }
            )
            if (checkInResponse.coTravellers.isNotEmpty()) {
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
                    checkInResponse.coTravellers.forEach { status ->
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
            AsyncImage(
                model = status.profilePicture,
                contentDescription = status.username,
                modifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.ic_new_user),
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

enum class CheckInResult {
    SUCCESSFUL {
        override fun getIcon() = R.drawable.ic_check_in
        override fun getString() = R.string.check_in_successful
        override fun getColor() = Color.Green
    },
    CONFLICTED {
        override fun getIcon() = R.drawable.ic_error
        override fun getString() = R.string.check_in_conflict
        override fun getColor() = Color.Red
    },
    ERROR {
        override fun getIcon() = R.drawable.ic_error
        override fun getString() = R.string.check_in_failure
        override fun getColor() = Color.Red
    };

    abstract fun getIcon(): Int
    abstract fun getString(): Int
    abstract fun getColor(): Color
}
