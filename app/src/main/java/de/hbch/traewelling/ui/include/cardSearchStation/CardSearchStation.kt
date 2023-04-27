package de.hbch.traewelling.ui.include.cardSearchStation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSearchStation(
    modifier: Modifier = Modifier,
    stations: List<Station>? = null,
    isLocating: Boolean = false,
    locateAction: () -> Unit = { },
    searchAction: (String) -> Unit = { }
) {
    var shortcutsVisible by rememberSaveable { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            var text by remember { mutableStateOf("") }
            Text(
                text = stringResource(id = R.string.where_are_you),
                style = AppTypography.headlineSmall
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        text = stringResource(id = R.string.input_stop)
                    )
                },
                trailingIcon = {
                    if (!stations.isNullOrEmpty()) {
                        IconButton(onClick = { shortcutsVisible = !shortcutsVisible }) {
                            AnimatedContent(shortcutsVisible) {
                                val icon =
                                    if (it)
                                        R.drawable.ic_expand_less
                                    else
                                        R.drawable.ic_expand_more
                                Icon(
                                    painterResource(id = icon),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            AnimatedVisibility (shortcutsVisible && !stations.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            stations?.forEachIndexed { i, s ->
                                val shortcutModifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = {
                                        shortcutsVisible = false
                                        searchAction(s.name)
                                    })
                                    .padding(8.dp)
                                item {
                                    if (i == 0)
                                        HomeStationShortcut(
                                            modifier = shortcutModifier,
                                            station = s
                                        )
                                    else
                                        LastVisitedStationShortcut(
                                            modifier = shortcutModifier,
                                            station = s
                                        )
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButtonWithIconAndText(
                    stringId = R.string.locate,
                    drawableId = R.drawable.ic_locate,
                    isLoading = isLocating,
                    onClick = locateAction
                )
                ButtonWithIconAndText(
                    stringId = R.string.search,
                    drawableId = R.drawable.ic_search,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    onClick = {
                        shortcutsVisible = false
                        searchAction(text)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeStationShortcut(
    modifier: Modifier = Modifier,
    station: Station
) {
    StationShortcut(
        modifier = modifier,
        station = station,
        drawable = R.drawable.ic_home
    )
}

@Composable
private fun LastVisitedStationShortcut(
    modifier: Modifier = Modifier,
    station: Station
) {
    StationShortcut(
        modifier = modifier,
        station = station,
        drawable = R.drawable.ic_history
    )
}

@Composable
private fun StationShortcut(
    modifier: Modifier = Modifier,
    station: Station,
    @DrawableRes drawable: Int,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = drawable),
            contentDescription = null
        )
        var stationName = station.name
        if (station.ds100 != null)
            stationName = stationName.plus(" [${station.ds100}]")
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stationName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(locale="de")
@Composable
private fun CardSearchStationPreview() {
    MainTheme {
        var isLocating by remember { mutableStateOf(false) }
        var search by remember { mutableStateOf("") }

        val locateAction: () -> Unit = {
            isLocating = true
            CoroutineScope(Dispatchers.Main).launch {
                delay(2500)
                search = "Location Hbf"
                isLocating = false
            }
        }
        val searchAction: (String) -> Unit = {
            search = it
        }
        val stations = listOf(
            Station(
                42,
                "Memmingen",
                47.0,
                10.0,
                "123456",
                "MM"
            ),
            Station(
                43,
                "Kempten(Allgäu)Hbf",
                47.0,
                10.0,
                "123456",
                "MKP"
            ),
            Station(
                43,
                "Stuttgart Hbf",
                47.0,
                10.0,
                "123456",
                "TS"
            ),
            Station(
                43,
                "Mannheim Hbf",
                47.0,
                10.0,
                "123456",
                "RM"
            ),
            Station(
                43,
                "Frankfurt (Main) Flughafen Regionalbahnhof",
                47.0,
                10.0,
                "123456",
                "FFLU"
            ),
            Station(
                43,
                "Berlin Gesundbrunnen",
                47.0,
                10.0,
                "123456",
                "BGS"
            )
        )

        Column {
            CardSearchStation(
                stations = stations,
                isLocating = isLocating,
                locateAction = locateAction,
                searchAction = searchAction
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = search
            )
        }
    }
}

@Preview
@Composable
private fun StationShortcutPreview() {
    MainTheme {
        val station = Station(
            42,
            "Station Hbf",
            47.0,
            10.0,
            "123456",
            "ZSHB"
        )

        StationShortcut(
            modifier = Modifier.fillMaxWidth(),
            station = station,
            drawable = R.drawable.ic_home
        )
    }
}
