package de.hbch.traewelling.ui.include.cardSearchStation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import de.hbch.traewelling.api.models.station.Station
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.theme.AppTypography
import de.hbch.traewelling.ui.search.Search
import de.hbch.traewelling.util.getGreeting

@Composable
fun CardSearch(
    modifier: Modifier = Modifier,
    homelandStationData: LiveData<Station?>,
    recentStationsData: LiveData<List<Station>?>,
    onStationSelected: (String) -> Unit = { },
    onUserSelected: (User) -> Unit = { },
    queryStations: Boolean = true,
    queryUsers: Boolean = true
) {
    val recentStations by recentStationsData.observeAsState()
    val homelandStation by homelandStationData.observeAsState()

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = getGreeting(),
                style = AppTypography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )
            Search(
                homelandStation = homelandStation,
                recentStations = recentStations,
                onStationSelected = onStationSelected,
                onUserSelected = onUserSelected,
                queryStations = queryStations,
                queryUsers = queryUsers,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
