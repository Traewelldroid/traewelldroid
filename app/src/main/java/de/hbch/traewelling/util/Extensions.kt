package de.hbch.traewelling.util

import androidx.navigation.NavHostController

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
