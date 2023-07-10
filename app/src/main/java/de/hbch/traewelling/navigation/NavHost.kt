package de.hbch.traewelling.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.ui.activeCheckins.EnRoute
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.ui.searchConnection.SearchConnection
import de.hbch.traewelling.ui.statistics.Statistics
import de.hbch.traewelling.ui.user.Profile
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@Composable
fun TraewelldroidNavHost(
    navController: NavHostController,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val navToSearchConnections: (String, Date?) -> Unit = { station, date ->
        navController.navigate(
            "search-connection/$station/${date?.time}"
        )
    }

    NavHost(
        navController = navController,
        startDestination = Dashboard.route,
        modifier = modifier
    ) {
        composable(Dashboard.route) {
            Dashboard(
                loggedInUserViewModel = loggedInUserViewModel,
                searchConnectionsAction = navToSearchConnections
            )
        }
        composable(EnRoute.route) {
            EnRoute(
                loggedInUserViewModel = loggedInUserViewModel
            )
        }
        composable(Statistics.route) {
            Statistics()
        }
        composable(PersonalProfile.route) {
            Profile(
                username = null,
                loggedInUserViewModel = loggedInUserViewModel,
                stationSelectedAction = navToSearchConnections
            )
        }
        composable(SearchConnection.route) {
            // if specific date is passed, take it. if not, search from now -5min
            var searchDate = it.arguments?.getLong("date")
            if (searchDate == null || searchDate == 0L) {
                val calendar = GregorianCalendar()
                calendar.time = Date()
                calendar.add(Calendar.MINUTE, -5)
                searchDate = calendar.time.time
            }

            SearchConnection(
                loggedInUserViewModel = loggedInUserViewModel,
                station = it.arguments?.getString("station") ?: "",
                currentSearchDate = Date(searchDate)
            )
        }
    }
}
