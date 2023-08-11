package de.hbch.traewelling.navigation

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.activeCheckins.EnRoute
import de.hbch.traewelling.ui.checkIn.CheckIn
import de.hbch.traewelling.ui.checkInResult.CheckInResultView
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.main.MainActivity
import de.hbch.traewelling.ui.notifications.Notifications
import de.hbch.traewelling.ui.notifications.NotificationsViewModel
import de.hbch.traewelling.ui.searchConnection.SearchConnection
import de.hbch.traewelling.ui.selectDestination.SelectDestination
import de.hbch.traewelling.ui.settings.Settings
import de.hbch.traewelling.ui.statistics.DailyStatistics
import de.hbch.traewelling.ui.statistics.Statistics
import de.hbch.traewelling.ui.statusDetail.StatusDetail
import de.hbch.traewelling.ui.user.Profile
import de.hbch.traewelling.util.popBackStackAndNavigate
import de.hbch.traewelling.util.toShortCut
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TraewelldroidNavHost(
    navController: NavHostController,
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel,
    checkInViewModel: CheckInViewModel,
    notificationsViewModel: NotificationsViewModel,
    modifier: Modifier = Modifier,
    onFloatingActionButtonChange: (Int, Int, () -> Unit) -> Unit = { _, _, _ -> },
    onResetFloatingActionButton: () -> Unit = { },
    onMenuChange: (List<ComposeMenuItem>) -> Unit = { },
    onNotificationCountChange: () -> Unit = { }
) {
    val context = LocalContext.current
    val secureStorage = SecureStorage(context)

    val navToSearchConnections: (String, ZonedDateTime?) -> Unit = { station, date ->
        val formattedDate =
            if (date == null)
                ""
            else 
                DateTimeFormatter.ISO_DATE_TIME.format(date)

        navController.navigate(
            "search-connection/?station=$station&date=$formattedDate"
        )
    }
    val navToStatusDetails: (Int) -> Unit = { statusId ->
        navController.navigate(
            "status-details/$statusId"
        )
    }
    val navToUserProfile: (String) -> Unit = { username ->
        navController.navigate(
            "personal-profile/?username=$username"
        )
    }

    val navToEditCheckIn: (Status) -> Unit = {
        checkInViewModel.lineName = it.line
        checkInViewModel.message.postValue(it.message)
        checkInViewModel.statusVisibility.postValue(it.visibility)
        checkInViewModel.statusBusiness.postValue(it.business)
        checkInViewModel.destination = it.destination
        checkInViewModel.destinationStationId = it.destinationId
        checkInViewModel.departureTime = it.departurePlanned
        checkInViewModel.manualDepartureTime = it.departureManual
        checkInViewModel.arrivalTime = it.arrivalPlanned
        checkInViewModel.manualArrivalTime = it.arrivalManual
        checkInViewModel.startStationId = it.originId
        checkInViewModel.tripId = it.hafasTripId
        checkInViewModel.editStatusId = it.statusId
        checkInViewModel.departureTime = it.departurePlanned
        checkInViewModel.category = it.productType

        navController.navigate(
            "check-in/?editMode=true"
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
                searchConnectionsAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(EnRoute.route) {
            EnRoute(
                loggedInUserViewModel = loggedInUserViewModel,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn
            )
            onMenuChange(listOf())
        }
        composable(Notifications.route) {
            Notifications(
                notificationsViewModel = notificationsViewModel,
                navHostController = navController,
                unreadNotificationsChanged = onNotificationCountChange
            )
            onMenuChange(listOf(
                ComposeMenuItem(
                    R.string.mark_all_as_read,
                    R.drawable.ic_mark_all_as_read
                ) {
                    notificationsViewModel.markAllAsRead {
                        navController.popBackStack()
                        navController.navigate(Notifications.route) {
                            navController.graph.startDestinationRoute?.let { screenRoute ->
                                popUpTo(screenRoute) {
                                    inclusive = true
                                }
                            }
                            launchSingleTop = true
                        }
                    }
                }
            ))
        }
        composable(Statistics.route) {
            Statistics(
                modifier = Modifier.padding(bottom = 8.dp)
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(
            PersonalProfile.route,
            deepLinks = PersonalProfile.deepLinks
        ) {
            val username = it.arguments?.getString("username")

            Profile(
                username = username,
                loggedInUserViewModel = loggedInUserViewModel,
                stationSelectedAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                statusEditAction = navToEditCheckIn,
                dailyStatisticsSelectedAction = { date ->
                    val formatted = DateTimeFormatter.ISO_DATE.format(date)
                    navController.navigate("daily-statistics/$formatted")
                }
            )

            val menuItems = mutableListOf<ComposeMenuItem>()
            if (username == null) {
                menuItems.addAll(
                    listOf(
                        ComposeMenuItem(
                            R.string.information,
                            R.drawable.ic_privacy
                        ) {
                            context.startActivity(Intent(context, InfoActivity::class.java))
                        },
                        ComposeMenuItem(
                            R.string.settings,
                            R.drawable.ic_settings
                        ) {
                            navController.navigate(Settings.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                )
            }
            onMenuChange(menuItems)
            onResetFloatingActionButton()
        }
        composable(
            DailyStatistics.route,
            deepLinks = DailyStatistics.deepLinks
        ) {
            val date = it.arguments?.getString("date")
            var localDate = LocalDate.now()
            if (date != null) {
                localDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(date))
            }
            DailyStatistics(
                date = localDate,
                loggedInUserViewModel = loggedInUserViewModel,
                statusSelectedAction = navToStatusDetails,
                statusEditAction = navToEditCheckIn
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(Settings.route) {
            Settings(
                loggedInUserViewModel = loggedInUserViewModel,
                emojiPackItemAdapter = (context as? MainActivity)?.emojiPackItemAdapter,
                traewellingLogoutAction = {
                    loggedInUserViewModel.logout( {
                        secureStorage.removeObject(SharedValues.SS_JWT)
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? Activity)?.finish()
                    }, {})
                }
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(
            StatusDetails.route,
            deepLinks = StatusDetails.deepLinks
        ) {
            val statusId = it.arguments?.getString("statusId")?.toInt()
            if (statusId == null || statusId == 0) {
                navController.popBackStack()
                return@composable
            }

            StatusDetail(
                statusId = statusId,
                loggedInUserViewModel = loggedInUserViewModel,
                statusLoaded = { status ->
                    val menuItems = mutableListOf<ComposeMenuItem>()
                    if (loggedInUserViewModel.loggedInUser.value != null) {
                        if (status.userId == loggedInUserViewModel.loggedInUser.value?.id) {
                            menuItems.add(
                                ComposeMenuItem(
                                    R.string.title_share,
                                    R.drawable.ic_share
                                ) {
                                    var shareText = context.getString(
                                        R.string.share_text,
                                        status.line,
                                        status.destination
                                    )
                                    shareText =
                                        shareText.plus("\n\nhttps://traewelling.de/status/${status.statusId}")
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }

                                    val shareIntent = Intent.createChooser(
                                        sendIntent,
                                        context.getString(R.string.title_share)
                                    )
                                    context.startActivity(shareIntent)
                                }
                            )
                        } else {
                            menuItems.add(
                                ComposeMenuItem(
                                    R.string.title_also_check_in,
                                    R.drawable.ic_also_check_in
                                ) {
                                    checkInViewModel.lineName = status.line
                                    checkInViewModel.tripId = status.hafasTripId
                                    checkInViewModel.startStationId = status.originId
                                    checkInViewModel.departureTime = status.departurePlanned
                                    checkInViewModel.destinationStationId = status.destinationId
                                    checkInViewModel.arrivalTime = status.arrivalPlanned
                                    checkInViewModel.category = status.productType
                                    checkInViewModel.destination = status.destination

                                    navController.navigate(
                                        CheckIn.route
                                    )
                                }
                            )
                        }
                    }
                    onMenuChange(menuItems)
                    onResetFloatingActionButton()
                },
                statusEdit = navToEditCheckIn,
                statusDeleted = {
                    navController.popBackStack()
                },
                likerSelected = navToUserProfile
            )
        }
        composable(
            SearchConnection.route,
            deepLinks = SearchConnection.deepLinks
        ) {
            // if specific date is passed, take it. if not, search from now -5min
            var zonedDateTime = ZonedDateTime.now()
            val searchDate = it.arguments?.getString("date")
            if (!searchDate.isNullOrBlank()) {
                zonedDateTime = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(searchDate))
            }

            SearchConnection(
                loggedInUserViewModel = loggedInUserViewModel,
                station = it.arguments?.getString("station") ?: "",
                currentSearchDate = zonedDateTime,
                checkInViewModel = checkInViewModel,
                onTripSelected = {
                    navController.navigate(
                        "select-destination/?editMode=false"
                    )
                },
                onHomelandSelected = { station ->
                    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                        val shortcut = station.toShortCut(context, home = true)
                        val pinnedShortcutCallbackIntent =
                            ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)

                        val successCallback = PendingIntent.getBroadcast(
                            context, /* request code */ 0,
                            pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE
                        )

                        ShortcutManagerCompat.requestPinShortcut(
                            context, shortcut,
                            successCallback.intentSender
                        )
                    }
                }
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(SelectDestination.route) {
            val editMode = it.arguments?.getString("editMode")?.toBooleanStrictOrNull() ?: false
            SelectDestination(
                checkInViewModel = checkInViewModel,
                onStationSelected = {
                    navController.navigate(
                        "check-in/?editMode=$editMode"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(CheckIn.route) {
            val editMode = it.arguments?.getString("editMode")?.toBooleanStrictOrNull() ?: false
            val initText =
                if (editMode) {
                    checkInViewModel.message.value ?: ""
                } else {
                    val hashtag = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java)
                    if (hashtag == null || hashtag == "")
                        ""
                    else
                        "\n#$hashtag"
                }
            checkInViewModel.statusVisibility.postValue(loggedInUserViewModel.defaultStatusVisibility)
            CheckIn(
                checkInViewModel = checkInViewModel,
                eventViewModel = eventViewModel,
                initText = initText,
                checkInAction = {
                    if (editMode) {
                        checkInViewModel.updateCheckIn { status ->
                            navController.navigate(
                                "status-details/${status.id}"
                            ) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        checkInViewModel.checkIn { succeeded ->
                            navController.navigate(
                                CheckInResult.route
                            ) {
                                if (succeeded) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                }

                                launchSingleTop = true
                            }
                        }
                    }
                },
                isEditMode = editMode,
                changeDestinationAction = {
                    navController.navigate(
                        "select-destination/?editMode=true"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
            onMenuChange(listOf())
            onResetFloatingActionButton()
        }
        composable(CheckInResult.route) {
            CheckInResultView(
                checkInViewModel = checkInViewModel,
                onStatusSelected = navToStatusDetails,
                onFloatingActionButtonChange = { icon, label ->
                    onFloatingActionButtonChange(icon, label) {
                        checkInViewModel.reset()
                        navController.popBackStackAndNavigate(Dashboard.route)
                    }
                },
                onCheckInForced = {
                    checkInViewModel.forceCheckIn {
                        navController.navigate(
                            CheckInResult.route
                        ) {
                            launchSingleTop = true
                        }
                    }
                }
            )
            onMenuChange(listOf())
        }
    }
}
