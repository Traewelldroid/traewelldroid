package de.traewelling.adapters

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.traewelling.R
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resourceId: Int?) {
    if (resourceId == null)
        return
    imageView.setImageResource(resourceId)
}

@BindingAdapter(value = ["planned", "real"], requireAll = true)
fun setVisibilityForDelay(textView: TextView, planned: Date?, real: Date?) {
    if (planned == null || real == null) {
        textView.visibility = View.GONE
        return
    }
    if (planned.compareTo(real) == 0)
        textView.visibility = View.GONE
    else
        textView.visibility = View.VISIBLE
}

@BindingAdapter("displayTime")
fun setTimeText(textView: TextView, date: Date?) {
    textView.text = getLocalTimeString(date)
}

@BindingAdapter(value = [ "departure", "arrival" ], requireAll = true)
fun setTimeProgress(progressBar: ProgressBar, departure: Date?, arrival: Date?) {
    if (departure == null || arrival == null)
        return

    progressBar.max = getJourneyMinutes(departure, arrival).toInt()
    progressBar.progress = getJourneyProgress(departure, arrival).toInt()
}

@BindingAdapter("duration")
fun setDuration(textView: TextView, duration: Int?) {
    if (duration == null)
        return

    val hours = duration / 60
    val minutes = duration % 60
    textView.text = textView.resources.getString(R.string.display_travel_time, hours, minutes)
}

@BindingAdapter(value = [ "username", "timestamp" ], requireAll = true)
fun setUsernameAndTimeOnCheckIn(textView: TextView, username: String?, timestamp: Date?) {
    if (username == null || timestamp == null)
        return
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    textView.text = textView.resources.getString(R.string.check_in_user_time, username, df.format(timestamp))
}

@BindingAdapter("productType")
fun setProductTypeImage(imageView: ImageView, productType: String?) {
    val drawable = when (productType) {
        "suburban" -> R.drawable.ic_suburban
        "bus" -> R.drawable.ic_bus
        "subway" -> R.drawable.ic_subway
        "tram" -> R.drawable.ic_tram
        else -> R.drawable.ic_train
    }
    imageView.setImageResource(drawable)
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
        val progressMillis = currentDateTime.time - departure.time
        // Return 0% progress when journey hasn't started yet
        if (progressMillis < 0)
            return 0
        progressMinutes = TimeUnit.MINUTES.convert(progressMillis, TimeUnit.MILLISECONDS)
    }

    return progressMinutes
}