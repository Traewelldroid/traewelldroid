package de.hbch.traewelling.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.lineIcons.LineIcon
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.FeatureFlags
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.ui.include.status.CheckInCard
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.ZonedDateTime

fun NavHostController.popBackStackAndNavigate(
    route: String,
    launchSingleTop: Boolean = true,
    popUpToInclusive: Boolean = true
) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = popUpToInclusive
        }
        this.launchSingleTop = launchSingleTop
    }
}

fun LazyListScope.checkInList(
    checkIns: SnapshotStateList<Status>,
    checkInCardViewModel: CheckInCardViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    stationSelectedAction: (String, ZonedDateTime?) -> Unit = { _, _ -> },
    statusSelectedAction: (Int) -> Unit = { },
    statusEditAction: (Status) -> Unit = { },
    statusDeletedAction: () -> Unit = { },
    userSelectedAction: (String) -> Unit = { },
    showDailyStatisticsLink: Boolean = false,
    dailyStatisticsSelectedAction: (LocalDate) -> Unit = { },
    showDate: Boolean = true
) {
    @Suppress("UNUSED_VARIABLE") val featureFlags = FeatureFlags.getInstance()

    itemsIndexed(
        items = checkIns
    ) { index, status ->
        val previousStatus = checkIns.getOrNull(index - 1)
        if (
            showDate &&
            (
                previousStatus == null ||
                !isSameDay(
                    previousStatus.journey.origin.departurePlanned.toLocalDate(),
                    status.journey.origin.departurePlanned.toLocalDate()
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getLongLocalDateString(status.journey.origin.departurePlanned),
                    modifier = Modifier
                        .weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTypography.titleLarge
                )
                if (showDailyStatisticsLink) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_score),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            dailyStatisticsSelectedAction(status.journey.origin.departurePlanned.toLocalDate())
                        })
                    )
                }
            }
        }
        CheckInCard(
            checkInCardViewModel = checkInCardViewModel,
            status = status,
            loggedInUserViewModel = loggedInUserViewModel,
            stationSelected = stationSelectedAction,
            statusSelected = statusSelectedAction,
            handleEditClicked = statusEditAction,
            onDeleted = { statusValue ->
                checkIns.removeIf { it.id == statusValue.id }
                statusDeletedAction()
            },
            userSelected = userSelectedAction
        )
        if (checkIns.size == (index + 1)) {
            Box(Modifier.height(16.dp))
        }
    }
}


@Composable
fun LazyListState.OnBottomReached(
    loadMore : () -> Unit
){
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore){
        snapshotFlow { shouldLoadMore.value }
            .collect {
                if (it) loadMore()
            }
    }
}

fun Context.shareStatus(
    status: Status
) {
    var shareText =
        if (status.body?.isBlank() == true)
            getString(R.string.share_text, status.journey.line, status.journey.destination.name)
        else
            getString(R.string.share_text_with_body, status.body, status.journey.line, status.journey.destination.name)

    val shareUri = Uri.Builder()
        .scheme("https")
        .authority("traewelling.de")
        .appendPath("status")
        .appendPath(status.id.toString())
        .build()

    shareText = shareText.plus("\n\n$shareUri")

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(
        sendIntent,
        getString(R.string.title_share)
    )
    startActivity(shareIntent)
}

suspend fun Context.readOrDownloadLineIcons(
    overwrite: Boolean = false
): List<LineIcon> {
    val lineColorCsvUrl = URL("https://raw.githubusercontent.com/Traewelling/line-colors/main/line-colors.csv")
    val file = File(filesDir, "line-colors.csv")
    val icons = withContext(Dispatchers.IO) {
        if (overwrite || !file.exists()) {
            Files.copy(
                lineColorCsvUrl.openStream(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
        return@withContext readCsv(file.inputStream())
    }
    return icons
}

fun TraewelldroidUriBuilder(): Uri.Builder {
    return Uri.Builder()
        .scheme("traewelldroid")
        .authority("app.traewelldroid.de")
}

fun colorFromHex(color: String)
    = Color(android.graphics.Color.parseColor(color))
