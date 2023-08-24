package de.hbch.traewelling.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.hbch.traewelling.R

const val TRWL_BASE_URI = "https://traewelling.de"
const val TRAEWELLDROID_BASE_URI = "traewelldroid://app.traewelldroid.de"

interface Destination {
    val label: Int
    val route: String
}

interface ArgumentDestination : Destination {
    val arguments: List<NamedNavArgument>
}

interface MainDestination : Destination {
    val icon: Int
}

interface DeepLinkedDestination: Destination {
    val deepLinks: List<NavDeepLink>
}

object Dashboard : MainDestination {
    override val icon = R.drawable.ic_dashboard
    override val label = R.string.title_dashboard
    override val route = "dashboard"
}

object EnRoute : MainDestination {
    override val icon = R.drawable.ic_train
    override val label = R.string.title_active_checkins
    override val route = "en-route"
}

object Notifications : MainDestination {
    override val icon = R.drawable.ic_notification
    override val label = R.string.title_notifications
    override val route = "notifications"
}

object Statistics : MainDestination {
    override val icon = R.drawable.ic_statistics
    override val label = R.string.title_statistics
    override val route = "statistics"
}

object PersonalProfile : MainDestination, ArgumentDestination, DeepLinkedDestination {
    override val icon = R.drawable.ic_account
    override val label = R.string.title_user
    override val route = "personal-profile/?username={username}"
    override val arguments = listOf(
        navArgument("username") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )
    override val deepLinks = listOf(
        navDeepLink {
            uriPattern = "$TRWL_BASE_URI/@{username}"
        },
        navDeepLink {
            uriPattern = "$TRAEWELLDROID_BASE_URI/@{username}"
        }
    )
}

object DailyStatistics: ArgumentDestination, DeepLinkedDestination {
    override val route = "daily-statistics/{date}"
    override val label = R.string.daily_overview
    override val arguments = listOf(
        navArgument("date") {
            type = NavType.StringType
        }
    )
    override val deepLinks = listOf(
        navDeepLink {
            uriPattern = "$TRWL_BASE_URI/stats/daily/{date}"
        },
        navDeepLink {
            uriPattern = "$TRAEWELLDROID_BASE_URI/stats/daily/{date}"
        }
    )
}

object SearchConnection : ArgumentDestination, DeepLinkedDestination {
    override val label = R.string.title_search_connection
    override val route = "search-connection/?station={station}&date={date}"
    override val arguments = listOf(
        navArgument("station") {
            type = NavType.StringType
        },
        navArgument("date") {
            type = NavType.LongType
        }
    )
    override val deepLinks = listOf(
        navDeepLink {
            uriPattern = "$TRWL_BASE_URI/trains/stationboard?station={station}"
        },
        navDeepLink {
            uriPattern = "$TRAEWELLDROID_BASE_URI/trains/stationboard?station={station}"
        }
    )
}

object SelectDestination : ArgumentDestination {
    override val label = R.string.title_select_destination
    override val route = "select-destination/?editMode={editMode}"
    override val arguments = listOf(
        navArgument("editMode") {
            type = NavType.BoolType
            defaultValue = false
        }
    )
}

object CheckIn : ArgumentDestination {
    override val label = R.string.check_in
    override val route = "check-in/?editMode={editMode}"
    override val arguments = listOf(
        navArgument("editMode") {
            type = NavType.BoolType
            defaultValue = false
        }
    )
}

object CheckInResult: Destination {
    override val label = R.string.check_in
    override val route = "check-in-result"
}

object StatusDetails : ArgumentDestination, DeepLinkedDestination {
    override val label = R.string.status_details
    override val route = "status-details/{statusId}"
    override val arguments = listOf(
        navArgument("statusId") {
            type = NavType.IntType
        }
    )
    override val deepLinks = listOf(
        navDeepLink {
            uriPattern = "$TRWL_BASE_URI/status/{statusId}"
        },
        navDeepLink {
            uriPattern = "$TRAEWELLDROID_BASE_URI/status/{statusId}"
        }
    )
}

object Settings : Destination {
    override val label = R.string.settings
    override val route = "settings"
}

val SCREENS = listOf(
    Dashboard,
    EnRoute,
    Notifications,
    Statistics,
    PersonalProfile,
    DailyStatistics,
    SearchConnection,
    SelectDestination,
    CheckIn,
    CheckInResult,
    StatusDetails,
    Settings
)

val BOTTOM_NAVIGATION = listOf(
    Dashboard,
    EnRoute,
    Notifications,
    Statistics,
    PersonalProfile
)

data class ComposeMenuItem(
    val label: Int,
    val icon: Int,
    val badge: String? = null,
    val onClick: () -> Unit = { }
)
