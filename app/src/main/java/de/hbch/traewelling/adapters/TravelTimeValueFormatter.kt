package de.hbch.traewelling.adapters

import android.content.res.Resources
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import de.hbch.traewelling.R

class TravelTimeValueFormatter(private val resources: Resources) : ValueFormatter() {
    override fun getBarLabel(barEntry: BarEntry?): String {
        if (barEntry == null)
            return ""

        return getDurationString(resources, barEntry.y.toInt())
    }
}