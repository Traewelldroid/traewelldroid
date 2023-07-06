package de.hbch.traewelling.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.hbch.traewelling.navigation.BOTTOM_NAVIGATION
import de.hbch.traewelling.navigation.Dashboard
import de.hbch.traewelling.navigation.SCREENS
import de.hbch.traewelling.theme.MainTheme

class NewMainActivity : ComponentActivity() {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraewelldroidApp() {
    MainTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentScreen = SCREENS.find { it.route == currentDestination?.route } ?: Dashboard

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = currentScreen.label)
                        )
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
                                        popUpTo(
                                            navController.graph.findStartDestination().id
                                        ) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Dashboard.route) {
                }
            }
        }
    }
}
