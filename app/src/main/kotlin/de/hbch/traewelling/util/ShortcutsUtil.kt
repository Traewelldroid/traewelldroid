package de.hbch.traewelling.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station
import java.lang.Exception

const val HOME = "0"


fun Station.toShortcut(context: Context, shortcutId: String, home: Boolean = false): ShortcutInfo {
    val icon = if (home) R.mipmap.ic_shortcut_home else R.mipmap.ic_shortcut_history
    val shortcutLabel = if (home) "$name (${context.getString(R.string.home)})" else name
    return ShortcutInfo.Builder(context, shortcutId)
        .setShortLabel(shortcutLabel)
        .setIcon(Icon.createWithResource(context, icon))
        .setIntent(
            Intent(
                Intent.ACTION_VIEW,
                TraewelldroidUriBuilder()
                    .appendPath("trains")
                    .appendPath("stationboard")
                    .appendQueryParameter("station", ibnr)
                    .build()
            )
        )
        .build()
}

fun Context.publishStationShortcuts(
    homelandStation: Station?,
    recentStations: List<Station>?
): Boolean {
    if (homelandStation != null && recentStations != null) {
        val shortcutManager: ShortcutManager? = getSystemService(ShortcutManager::class.java)
        if (shortcutManager != null) {
            // Remove stale shortcuts
            val shortcutsToBeRemoved = shortcutManager
                .dynamicShortcuts
                .map { it.id }
                .filter { it.length > 1 }
            shortcutManager.removeDynamicShortcuts(shortcutsToBeRemoved)

            val shortcuts = mutableListOf<ShortcutInfo>()
            shortcuts.add(homelandStation.toShortcut(this, HOME, true))
            shortcuts.addAll(
                recentStations
                    .take(3)
                    .mapIndexed { index, station ->
                        station.toShortcut(this, index.toString(), false)
                    }
            )
            try {
                return shortcutManager.addDynamicShortcuts(shortcuts)
            } catch (_: Exception) {
            }
        }
    }
    return false
}

fun Context.removeDynamicShortcuts() {
    val shortcutManager: ShortcutManager? = getSystemService(ShortcutManager::class.java)
    shortcutManager?.removeAllDynamicShortcuts()
}
