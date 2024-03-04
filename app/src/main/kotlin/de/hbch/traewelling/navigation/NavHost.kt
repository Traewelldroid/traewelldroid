package de.hbch.traewelling.navigation

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.activeCheckins.EnRoute
import de.hbch.traewelling.ui.checkIn.CheckIn
import de.hbch.traewelling.ui.checkInResult.CheckInResultView
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.ui.info.InfoActivity
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
import de.hbch.traewelling.util.HOME
import de.hbch.traewelling.util.popBackStackAndNavigate
import de.hbch.traewelling.util.shareStatus
import de.hbch.traewelling.util.toShortcut
import kotlinx.coroutines.launch
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
    snackbarHostState: SnackbarHostState,
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
        checkInViewModel.lineName = it.journey.line
        checkInViewModel.lineId = it.journey.lineId
        checkInViewModel.operatorCode = it.journey.operator?.id
        checkInViewModel.message.postValue(it.body)
        checkInViewModel.statusVisibility.postValue(it.visibility)
        checkInViewModel.statusBusiness.postValue(it.business)
        checkInViewModel.destination = it.journey.destination.name
        checkInViewModel.destinationStationId = it.journey.destination.id
        checkInViewModel.departureTime = it.journey.origin.departurePlanned
        checkInViewModel.manualDepartureTime = it.journey.departureManual
        checkInViewModel.arrivalTime = it.journey.destination.arrivalPlanned
        checkInViewModel.manualArrivalTime = it.journey.arrivalManual
        checkInViewModel.startStationId = it.journey.origin.id
        checkInViewModel.tripId = it.journey.hafasTripId
        checkInViewModel.editStatusId = it.id
        checkInViewModel.category = it.journey.category

        navController.navigate(
            "check-in/?editMode=true"
        )
    }

    val initKnowsAboutNotifications = secureStorage.getObject(
        SharedValues.SS_NOTIFICATIONS_ENABLED,
        Boolean::class.java
    ) != null
    var knowsAboutNotifications by remember { mutableStateOf(initKnowsAboutNotifications) }
    val closeNotificationHint: () -> Unit = {
        secureStorage.storeObject(SharedValues.SS_NOTIFICATIONS_ENABLED, false)
        knowsAboutNotifications = true
    }

    NavHost(
        navController = navController,
        startDestination = Dashboard.route,
        modifier = modifier
    ) {
        composable(Dashboard.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            Dashboard(
                loggedInUserViewModel = loggedInUserViewModel,
                searchConnectionsAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn,
                knowsAboutNotifications = knowsAboutNotifications,
                notificationHintClosed = closeNotificationHint
            )
            onResetFloatingActionButton()
        }
        composable(EnRoute.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            EnRoute(
                loggedInUserViewModel = loggedInUserViewModel,
                statusSelectedAction = navToStatusDetails,
                userSelectedAction = navToUserProfile,
                statusEditAction = navToEditCheckIn
            )
        }
        composable(Notifications.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf(
                    ComposeMenuItem(
                        R.string.mark_all_as_read,
                        R.drawable.ic_mark_all_as_read
                    ) {
                        notificationsViewModel.markAllAsRead {
                            onNotificationCountChange()
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
                initialized = true
            }
            Notifications(
                loggedInUserViewModel = loggedInUserViewModel,
                notificationsViewModel = notificationsViewModel,
                navHostController = navController,
                unreadNotificationsChanged = onNotificationCountChange,
                knowsAboutNotifications = knowsAboutNotifications,
                notificationHintClosed = closeNotificationHint
            )
        }
        composable(Statistics.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            Statistics(
                modifier = Modifier.padding(bottom = 8.dp)
            )
            onResetFloatingActionButton()
        }
        composable(
            PersonalProfile.route,
            deepLinks = PersonalProfile.deepLinks
        ) {
            val username = it.arguments?.getString("username")
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
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
                initialized = true
            }

            Profile(
                username = username,
                loggedInUserViewModel = loggedInUserViewModel,
                stationSelectedAction = navToSearchConnections,
                statusSelectedAction = navToStatusDetails,
                statusEditAction = navToEditCheckIn,
                dailyStatisticsSelectedAction = { date ->
                    val formatted = DateTimeFormatter.ISO_DATE.format(date)
                    navController.navigate("daily-statistics/$formatted")
                },
                userSelectedAction = navToUserProfile
            )

            onResetFloatingActionButton()
        }
        composable(
            DailyStatistics.route,
            deepLinks = DailyStatistics.deepLinks
        ) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
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
            onResetFloatingActionButton()
        }
        composable(Settings.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            Settings(
                snackbarHostState = snackbarHostState,
                loggedInUserViewModel = loggedInUserViewModel,
                emojiPackItemAdapter = (context as? MainActivity)?.emojiPackItemAdapter
            )
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
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
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
                                    context.shareStatus(status)
                                }
                            )
                        } else {
                            menuItems.add(
                                ComposeMenuItem(
                                    R.string.title_also_check_in,
                                    R.drawable.ic_also_check_in
                                ) {
                                    checkInViewModel.lineName = status.journey.line
                                    checkInViewModel.operatorCode = status.journey.operator?.id
                                    checkInViewModel.lineId = status.journey.lineId
                                    checkInViewModel.tripId = status.journey.hafasTripId
                                    checkInViewModel.startStationId = status.journey.origin.id
                                    checkInViewModel.departureTime = status.journey.origin.departurePlanned
                                    checkInViewModel.destinationStationId = status.journey.destination.id
                                    checkInViewModel.arrivalTime = status.journey.destination.arrivalPlanned
                                    checkInViewModel.category = status.journey.category
                                    checkInViewModel.destination = status.journey.destination.name

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
                userSelected = navToUserProfile
            )
        }
        composable(
            SearchConnection.route,
            deepLinks = SearchConnection.deepLinks
        ) {
            // if specific date is passed, take it. if not, search from now -5min
            var zonedDateTime = ZonedDateTime.now().minusMinutes(5)
            val searchDate = it.arguments?.getString("date")
            if (!searchDate.isNullOrBlank()) {
                zonedDateTime = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(searchDate))
            }

            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
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
                    val shortcutManager: ShortcutManager?
                        = context.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager != null) {
                        val shortcut = station.toShortcut(context, HOME, true)
                        val intent = shortcutManager.createShortcutResultIntent(shortcut)
                        val successCallback = PendingIntent.getBroadcast(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
                    }
                }
            )
            onResetFloatingActionButton()
        }
        composable(SelectDestination.route) {
            val editMode = it.arguments?.getString("editMode")?.toBooleanStrictOrNull() ?: false
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
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
            onResetFloatingActionButton()
        }
        composable(CheckIn.route) {
            val editMode = it.arguments?.getString("editMode")?.toBooleanStrictOrNull() ?: false
            val initText =
                if (editMode) {
                    checkInViewModel.message.value ?: ""
                } else {
                    // Only set default visibility when a new check-in is created!
                    checkInViewModel.statusVisibility.postValue(
                        loggedInUserViewModel.defaultStatusVisibility
                    )

                    val hashtag = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java)
                    if (hashtag == null || hashtag == "")
                        ""
                    else
                        "\n#$hashtag"
                }
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }

            val coroutineScope = rememberCoroutineScope()

            CheckIn(
                checkInViewModel = checkInViewModel,
                eventViewModel = eventViewModel,
                initText = initText,
                checkInAction = { trwl, travelynx ->
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
                        val checkInCount = secureStorage.getObject(SharedValues.SS_CHECK_IN_COUNT, Long::class.java) ?: 0L

                        coroutineScope.launch {
                            checkInViewModel.checkIn(trwl, travelynx) { succeeded ->
                                navController.navigate(
                                    CheckInResult.route
                                ) {
                                    if (succeeded) {
                                        secureStorage.storeObject(
                                            SharedValues.SS_CHECK_IN_COUNT,
                                            checkInCount + 1
                                        )
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                    }

                                    launchSingleTop = true
                                }
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
            onResetFloatingActionButton()
        }
        composable(CheckInResult.route) {
            var initialized by remember { mutableStateOf(false) }
            if (!initialized) {
                onMenuChange(listOf())
                initialized = true
            }
            val coroutineScope = rememberCoroutineScope()

            CheckInResultView(
                checkInViewModel = checkInViewModel,
                loggedInUserViewModel = loggedInUserViewModel,
                onStatusSelected = navToStatusDetails,
                onFloatingActionButtonChange = { icon, label ->
                    onFloatingActionButtonChange(icon, label) {
                        checkInViewModel.reset()
                        navController.popBackStackAndNavigate(Dashboard.route)
                    }
                },
                onCheckInForced = {
                    coroutineScope.launch {
                        checkInViewModel.forceCheckIn {
                            navController.navigate(
                                CheckInResult.route
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}
