package de.hbch.traewelling.adapters

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.auth0.android.jwt.JWT
import com.google.android.material.button.MaterialButton
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.event.Event
import de.hbch.traewelling.api.models.status.PointReason
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusPoints
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.HafasTrip
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.ui.include.alert.AlertType
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resourceId: Int?) {
    if (resourceId == null)
        return
    imageView.setImageResource(resourceId)
}

@BindingAdapter("alertIcon")
fun setAlertIcon(imageView: ImageView, alertType: AlertType?) {
    if (alertType == null)
        return
    imageView.setImageResource(when(alertType) {
        AlertType.ERROR -> R.drawable.ic_error
        AlertType.SUCCESS -> R.drawable.ic_check_in
    })
    imageView.setColorFilter(imageView.resources.getColor(when(alertType) {
        AlertType.ERROR -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                R.color.train_delayed
                            else
                                R.color.traewelling
        AlertType.SUCCESS -> R.color.success
    }, null))
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

@BindingAdapter("dateRange")
fun setDisplayTimeRange(textView: TextView, dateRange: Pair<Date, Date>?) {
    if (dateRange == null)
        return

    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
    textView.text = textView.resources.getString(
        R.string.date_range,
        dateFormat.format(dateRange.first),
        dateFormat.format(dateRange.second)
    )
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
    textView.text = getDurationString(textView.resources, duration)
}

fun getDurationString(resources: Resources, duration: Int): String {
    val minutes = duration % 60
    var hours = duration / 60
    val days = hours / 24
    hours -= days * 24

    return if (days > 0)
        resources.getString(R.string.display_travel_time_days_hours_minutes, days, hours, minutes)
    else {
        if (hours == 0)
            resources.getString(R.string.display_travel_time_minutes, minutes)
        else
            resources.getString(R.string.display_travel_time_hours_minutes, hours, minutes)
    }
}

@BindingAdapter(value = [ "username", "timestamp" ], requireAll = true)
fun setUsernameAndTimeOnCheckIn(textView: TextView, username: String?, timestamp: Date?) {
    if (username == null || timestamp == null)
        return
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    textView.text = textView.resources.getString(R.string.check_in_user_time, username, df.format(timestamp))
}

@BindingAdapter("productType")
fun setProductTypeImage(imageView: ImageView, productType: ProductType?) {
    val drawable = when (productType) {
        ProductType.SUBURBAN -> R.drawable.ic_suburban
        ProductType.BUS -> R.drawable.ic_bus
        ProductType.SUBWAY -> R.drawable.ic_subway
        ProductType.TRAM -> R.drawable.ic_tram
        else -> R.drawable.ic_train
    }
    imageView.setImageResource(drawable)
}

@BindingAdapter("statusVisibility")
fun setStatusVisibility(button: MaterialButton, statusVisibility: StatusVisibility?) {
    if (statusVisibility == null)
        return
    button.setIconResource(getStatusVisibilityImageResource(statusVisibility))
    button.text = button.resources.getString(getStatusVisibilityTextResource(statusVisibility))
}

@BindingAdapter("statusVisibility")
fun setStatusVisibility(imageView: ImageView, statusVisibility: StatusVisibility?) {
    if (statusVisibility == null)
        return

    imageView.setImageResource(getStatusVisibilityImageResource(statusVisibility))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        imageView.tooltipText =
            imageView.resources.getString(getStatusVisibilityTextResource(statusVisibility))
}

@BindingAdapter("jwtExpiration")
fun setJwtExpiration(textView: TextView, jwt: String) {
    if (jwt == "")
        return
    val decodedJwt = JWT(jwt)
    var dateTimeString = ""
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
    if (decodedJwt.expiresAt != null)
        dateTimeString = df.format(decodedJwt.expiresAt)
    textView.setText(textView.resources.getString(R.string.jwt_expiration, dateTimeString))
}

@BindingAdapter("loggedInAs")
fun setLoggedInAs(textView: TextView, username: String?) {
    textView.setText(textView.resources.getString(R.string.signed_in_as, username ?: ""))
}

fun getStatusVisibilityImageResource(visibility: StatusVisibility): Int {
    return when (visibility) {
        StatusVisibility.PUBLIC -> R.drawable.ic_public
        StatusVisibility.UNLISTED -> R.drawable.ic_lock_open
        StatusVisibility.FOLLOWERS -> R.drawable.ic_people
        StatusVisibility.PRIVATE -> R.drawable.ic_lock
        StatusVisibility.ONLY_AUTHENTICATED -> R.drawable.ic_authorized
    }
}

fun getStatusVisibilityTextResource(visibility: StatusVisibility): Int {
    return when (visibility) {
        StatusVisibility.PUBLIC -> R.string.visibility_public
        StatusVisibility.UNLISTED -> R.string.visibility_unlisted
        StatusVisibility.FOLLOWERS -> R.string.visibility_followers
        StatusVisibility.PRIVATE -> R.string.visibility_private
        StatusVisibility.ONLY_AUTHENTICATED -> R.string.visibility_only_authenticated
    }
}

@BindingAdapter("business")
fun setStatusBusiness(button: MaterialButton, business: StatusBusiness?) {
    if (business == null)
        return
    button.setIconResource(getBusinessImageResource(business))
    button.text = button.resources.getString(getBusinessTextResource(business))
}

@BindingAdapter("business")
fun setStatusBusiness(imageView: ImageView, business: StatusBusiness?) {
    if (business == null)
        return

    imageView.setImageResource(getBusinessImageResource(business))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        imageView.tooltipText = imageView.resources.getString(getBusinessTextResource(business))
}

@BindingAdapter("event")
fun setEventOnButton(button: MaterialButton, event: Event?) {
    button.text =
        event?.name ?: button.resources.getString(R.string.title_select_event)
}

@BindingAdapter("event")
fun setEventOnTextView(textView: TextView, event: Event?) {
    textView.text = event?.name ?: ""
}

@BindingAdapter("pointReason")
fun setPointReasonOnTextView(textView: TextView, statusPoints: StatusPoints?) {
    if (statusPoints?.calculation != null && statusPoints.calculation.reason != PointReason.IN_TIME)
        textView.text = textView.resources.getString(when(statusPoints.calculation.reason) {
            PointReason.FORCED -> R.string.point_reason_forced
            PointReason.GOOD_ENOUGH -> R.string.point_reason_good_enough
            PointReason.NOT_SUFFICIENT -> R.string.point_reason_not_sufficient
            PointReason.IN_TIME -> R.string.base
        });
}

fun getBusinessImageResource(business: StatusBusiness): Int {
    return when (business) {
        StatusBusiness.PRIVATE -> R.drawable.ic_person
        StatusBusiness.BUSINESS -> R.drawable.ic_business
        StatusBusiness.COMMUTE -> R.drawable.ic_commute
    }
}

fun getBusinessTextResource(business: StatusBusiness): Int {
    return when (business) {
        StatusBusiness.PRIVATE -> R.string.business_private
        StatusBusiness.BUSINESS -> R.string.business
        StatusBusiness.COMMUTE -> R.string.business_commute
    }
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

fun getLastDestination(trip: HafasTrip): String {
    var lastDestination = ""

    lastDestination = clarifyRingbahnBerlin(trip)

    // default case
    if (lastDestination == "")
        lastDestination = trip.direction ?: (trip.destination?.name ?: "")

    return lastDestination
}

private fun clarifyRingbahnBerlin(trip: HafasTrip): String {
    if (trip.line == null)
        return ""
    if (trip.direction == null)
        return ""

    if (trip.line.operator.id == "s-bahn-berlin" && trip.direction.contains("Ring")) {
        var direction = trip.direction
        direction = direction.replace("S41", "↻")
        direction = direction.replace("S42", "↺")

        return direction
    }

    return ""
}
