package de.hbch.traewelling.ui.launcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.api.models.notifications.NotificationChannelType
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.main.MainActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        createNotificationChannels()

        val secureStorage = SecureStorage(this)
        val startupActivity =
            when (secureStorage.getObject(SharedValues.SS_JWT, String::class.java)) {
                null -> LoginActivity::class.java
                else -> MainActivity::class.java
            }

        val startupIntent = Intent(this, startupActivity)
        startActivity(startupIntent)
        finish()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        if (notificationManager != null) {
            val channels = NotificationChannelType.values().map { channel ->
                val channelName = getString(channel.title)
                val channelDescription = getString(channel.description)

                val notificationChannel = NotificationChannel(
                    channel.name,
                    channelName,
                    channel.importance
                )
                notificationChannel.description = channelDescription
                return@map notificationChannel
            }
            notificationManager.createNotificationChannels(channels)
        }
    }
}
