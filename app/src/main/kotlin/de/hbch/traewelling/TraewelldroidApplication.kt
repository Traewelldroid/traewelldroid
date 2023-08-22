package de.hbch.traewelling

import android.app.Application
import com.google.android.material.color.DynamicColors

class TraewelldroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
