package de.hbch.traewelling.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.navigation.BOTTOM_NAVIGATION
import de.hbch.traewelling.navigation.Dashboard
import de.hbch.traewelling.navigation.SCREENS
import de.hbch.traewelling.navigation.TraewelldroidNavHost
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.dashboard.Dashboard
import de.hbch.traewelling.util.publishStationShortcuts

class NewMainActivity : ComponentActivity()
{
    private val loggedInUserViewModel: LoggedInUserViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val secureStorage = SecureStorage(this)
        TraewellingApi.jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java)!!

        loggedInUserViewModel.getLoggedInUser()
        loggedInUserViewModel.getLastVisitedStations {
            publishStationShortcuts(this, it)
        }
        eventViewModel.activeEvents()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TraewelldroidApp(
                loggedInUserViewModel = loggedInUserViewModel,
                eventViewModel = eventViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraewelldroidApp(
    loggedInUserViewModel: LoggedInUserViewModel,
    eventViewModel: EventViewModel
) {
    MainTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentScreen = SCREENS.find { it.route == currentDestination?.route } ?: Dashboard

        val appBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = currentScreen.label)
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(BOTTOM_NAVIGATION.contains(currentScreen)) {
                    NavigationBar {
                        BOTTOM_NAVIGATION.forEach {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(id = it.icon),
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = it.label)
                                    )
                                },
                                selected = currentScreen == it,
                                onClick = {
                                    navController.navigate(it.route) {
                                        val previousBackStackEntry =
                                            navController.previousBackStackEntry
                                        if (previousBackStackEntry != null) {
                                            popUpTo(
                                                previousBackStackEntry.id
                                            ) {
                                                inclusive = true
                                                saveState = false
                                            }
                                            launchSingleTop = true
                                            restoreState = false
                                            navController.clearBackStack(it.route)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            TraewelldroidNavHost(
                navController = navController,
                loggedInUserViewModel = loggedInUserViewModel,
                eventViewModel = eventViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
