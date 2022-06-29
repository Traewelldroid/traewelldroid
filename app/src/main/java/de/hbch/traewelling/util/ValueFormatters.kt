package de.hbch.traewelling.util

import android.content.res.Resources
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import de.hbch.traewelling.adapters.getDurationString

class TravelTimeValueFormatter(private val resources: Resources) : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        if (barEntry == null)
            return ""

        return getDurationString(resources, barEntry.y.toInt())
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return getDurationString(resources, value.toInt())
    }
}

class CheckInCountValueFormatter(): ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        if (barEntry == null)
            return ""

        return "${barEntry.y.toInt()}x"
    }
}