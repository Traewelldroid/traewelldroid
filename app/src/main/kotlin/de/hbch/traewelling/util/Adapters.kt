package de.hbch.traewelling.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.trip.HafasTrip
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun getLocalTimeString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLocalDateTimeString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLocalDateString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

fun getLongLocalDateString(date: ZonedDateTime): String
    = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)
        .withZone(ZoneId.systemDefault())
        .withLocale(Locale.getDefault())
        .format(date)

@Composable
fun getDelayColor(planned: ZonedDateTime, real: ZonedDateTime?): Color {
    val duration = Duration.between(planned, real ?: planned)
    val difference = duration.toMinutes()

    val color = when {
        difference <= 0 -> R.color.train_on_time
        difference in 1..5 -> R.color.warning
        else -> R.color.train_delayed
    }

    return colorResource(id = color)
}

@Composable
fun getDateRangeString(range: Pair<LocalDate, LocalDate>?): String {
    if (range == null)
        return ""
    return stringResource(
        id = R.string.date_range,
        getLocalDateString(date = range.first.atStartOfDay(ZoneId.systemDefault())),
        getLocalDateString(date = range.second.atStartOfDay(ZoneId.systemDefault()))
    )
}

fun isSameDay(date1: LocalDate, date2: LocalDate): Boolean {
    return date1.isEqual(date2)
}

fun getLastDestination(trip: HafasTrip): String {
    val lastDestination = clarifyRingbahnBerlin(trip)

    return lastDestination.ifBlank {
        trip.direction ?: (trip.destination?.name ?: "")
    }
}

private fun clarifyRingbahnBerlin(trip: HafasTrip): String {
    if (trip.line == null || trip.direction == null || trip.line.operator == null)
        return ""

    if (trip.line.operator.id == "s-bahn-berlin" && trip.direction.contains("Ring")) {
        return trip.direction.replace("S41", "↻")
            .replace("S42", "↺")
    }

    return ""
}
