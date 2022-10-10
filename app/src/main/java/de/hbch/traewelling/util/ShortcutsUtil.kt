package de.hbch.traewelling.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.station.Station

private const val HOME = "home"

fun Station.toShortCut(context: Context, home: Boolean = false): ShortcutInfoCompat {
    val icon = if (home) R.drawable.ic_home else R.drawable.ic_history
    return ShortcutInfoCompat.Builder(context, if (home) HOME else ibnr)
        .setShortLabel(name)
        .setIcon(IconCompat.createWithResource(context, icon))
        .setIntent(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://traewelling.de/trains/stationboard?station=${ibnr}")
            )
        )
        .build()
}

fun publishStationShortcuts(context: Context, stations: List<Station>, home: Boolean = false) {
    val shortcuts = stations.map {
        it.toShortCut(context)
    }

    ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
}

