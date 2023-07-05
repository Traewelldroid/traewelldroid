package de.hbch.traewelling.api.models.status

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.api.models.trip.ProductType
import java.util.Locale

data class Journey(
    @SerializedName("trip") val tripId: Int,
    @SerializedName("hafasId") val hafasTripId: String,
    @SerializedName("category") val category: ProductType,
    @SerializedName("number") val number: String,
    @SerializedName("lineName") val line: String,
    @SerializedName("journeyNumber") val journeyNumber: Int?,
    @SerializedName("distance") val distance: Int,
    @SerializedName("points") val points: Int,
    @SerializedName("delay") val delay: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("speed") val averageSpeed: Double,
    @SerializedName("origin") val origin: HafasTrainTripStation,
    @SerializedName("destination") val destination: HafasTrainTripStation
) {
    private val roundedDistance: Measure
        get() = if (distance < 1000) Measure(distance, MeasureUnit.METER) else Measure(distance / 1000, MeasureUnit.KILOMETER)

    val formattedDistance: String
        get() = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
            .formatMeasures(roundedDistance)
}
