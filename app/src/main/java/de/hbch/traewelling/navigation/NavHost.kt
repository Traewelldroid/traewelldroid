package de.hbch.traewelling.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.activeCheckins.EnRoute
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.ui.include.alert.AlertBottomSheet
import de.hbch.traewelling.ui.include.alert.AlertType
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.searchConnection.SearchConnection
import de.hbch.traewelling.ui.settings.Settings
import de.hbch.traewelling.ui.statistics.Statistics
import de.hbch.traewelling.ui.statusDetail.StatusDetail
import de.hbch.traewelling.ui.user.MenuItem
import de.hbch.traewelling.ui.user.Profile
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@Composable
fun TraewelldroidNavHost(
    navController: NavHostController,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier,
    onMenuChange: (List<ComposeMenuItem>) -> Unit = { }
) {
    val context = LocalContext.current
    val navToSearchConnections: (String, Date?) -> Unit = { station, date ->
        navController.navigate(
            "search-connection/$station/${date?.time}"
        )
    }

    val navToStatusDetails: (Int) -> Unit = { statusId ->
        navController.navigate(
            "status-details/$statusId"
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
            onMenuChange(listOf())
        }
        composable(EnRoute.route) {
            EnRoute(
                loggedInUserViewModel = loggedInUserViewModel
            )
            onMenuChange(listOf())
        }
        composable(Statistics.route) {
            Statistics()
            onMenuChange(listOf())
        }
        composable(PersonalProfile.route) {
            val menuItems = listOf(
                ComposeMenuItem(
                    R.string.settings,
                    R.drawable.ic_settings
                ) {
                    navController.navigate(Settings.route) {
                        launchSingleTop = true
                    }
                }
            )

            Profile(
                username = null,
                loggedInUserViewModel = loggedInUserViewModel,
                stationSelectedAction = navToSearchConnections
            )

            onMenuChange(menuItems)
        }
        composable(Settings.route) {
            Settings(
                loggedInUserViewModel = loggedInUserViewModel,
                traewellingLogoutAction = {
                    loggedInUserViewModel.logout( {
                        val secureStorage = SecureStorage(context)
                        secureStorage.removeObject(SharedValues.SS_JWT)
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? Activity)?.finish()
                    }, {})
                }
            )
        }
        composable(StatusDetails.route) {
            val statusId = it.arguments?.getInt("statusId")
            if (statusId == null || statusId == 0) {
                navController.popBackStack()
                return@composable
            }

            StatusDetail(statusId = statusId)
        }
        composable(SearchConnection.route) {
            // if specific date is passed, take it. if not, search from now -5min
            var searchDate = it.arguments?.getString("date")?.toLongOrNull()
            if (searchDate == null) {
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
