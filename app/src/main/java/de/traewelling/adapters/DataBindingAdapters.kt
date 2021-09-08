package de.traewelling.adapters

import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.traewelling.R
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resourceId: Int) {
    imageView.setImageResource(resourceId)
}

@BindingAdapter("displayTime")
fun setTimeText(textView: TextView, date: Date?) {
    textView.text = getLocalTimeString(date)
}

@BindingAdapter(value = [ "departure", "arrival" ], requireAll = true)
fun setTimeProgress(progressBar: ProgressBar, departure: Date, arrival: Date) {
    progressBar.max = getJourneyMinutes(departure, arrival).toInt()
    progressBar.progress = getJourneyProgress(departure, arrival).toInt()
}

@BindingAdapter("duration")
fun setDuration(textView: TextView, duration: Int) {
    val hours = duration / 60
    val minutes = duration % 60
    textView.text = textView.resources.getString(R.string.display_travel_time, hours, minutes)
}

fun getLocalTimeString(date: Date?): String {
    if (date == null) return ""
    return getTimeInstance(DateFormat.SHORT).format(date)
}

fun getJourneyMinutes(departure: Date, arrival: Date): Long {
    val timeSpanMillis = abs(arrival.time - departure.time)
    return TimeUnit.MINUTES.convert(timeSpanMillis, TimeUnit.MILLISECONDS)
}

fun getJourneyProgress(departure: Date, arrival: Date): Long {
    val timeSpanMinutes = getJourneyMinutes(departure, arrival)

    val currentDateTime = Date()
    var progressMinutes = timeSpanMinutes
    if (currentDateTime < arrival) {
        val progressMillis = abs(currentDateTime.time - departure.time)
        progressMinutes = TimeUnit.MINUTES.convert(progressMillis, TimeUnit.MILLISECONDS)
    }

    return progressMinutes
}