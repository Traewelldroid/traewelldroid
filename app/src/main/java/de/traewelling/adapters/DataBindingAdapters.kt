package de.traewelling.adapters

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
fun setImageResource(imageView: ImageView, resourceId: Int) {
    imageView.setImageResource(resourceId)
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
fun setDuration(textView: TextView, duration: Int) {
    val hours = duration / 60
    val minutes = duration % 60
    textView.text = textView.resources.getString(R.string.display_travel_time, hours, minutes)
}

@BindingAdapter(value = [ "username", "timestamp" ], requireAll = true)
fun setUsernameAndTimeOnCheckIn(textView: TextView, username: String?, timestamp: String?) {
    if (username == null || timestamp == null)
        return
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    val date = dateFormat.parse(timestamp)
    textView.text = textView.resources.getString(R.string.check_in_user_time, username, df.format(date))
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
        val progressMillis = abs(currentDateTime.time - departure.time)
        progressMinutes = TimeUnit.MINUTES.convert(progressMillis, TimeUnit.MILLISECONDS)
    }

    return progressMinutes
}