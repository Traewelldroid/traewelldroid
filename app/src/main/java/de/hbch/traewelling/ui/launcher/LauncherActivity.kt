package de.hbch.traewelling.ui.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.login.LoginActivity
import de.hbch.traewelling.ui.main.MainActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val secureStorage = SecureStorage(this)
        val startupActivity =
            when (secureStorage.getObject(SharedValues.SS_JWT, String::class.java)) {
                null -> LoginActivity::class.java
                else -> MainActivity::class.java
            }

        var startupIntent: Intent? = null
        val action = intent?.action
        action?.let {
            // Show status by id
            if (it == Intent.ACTION_VIEW) {
                val data = intent?.data
                if (data != null) {
                    startupIntent = Intent(Intent.ACTION_VIEW, Uri.EMPTY, this, startupActivity)
                    if ("status" in data.pathSegments) {
                        startupIntent?.putExtra(SharedValues.EXTRA_STATUS_ID, data.lastPathSegment)
                    } else if ("stationboard" in data.pathSegments) {
                        val station = data.getQueryParameter("station")
                        startupIntent?.putExtra(
                            SharedValues.EXTRA_STATION_ID,
                            station
                        )
                        val travelType =
                            data.getQueryParameter("travelType")
                        if (travelType != null) {
                            startupIntent?.putExtra(
                                SharedValues.EXTRA_TRAVEL_TYPE,
                                travelType
                            )
                        }
                    } else {
                        startupIntent?.putExtra(
                            SharedValues.EXTRA_USER_NAME,
                            data.lastPathSegment?.substringAfter('@')
                        )
                    }
                }
            }
            if (startupIntent == null)
                startupIntent = Intent(this, startupActivity)

            startActivity(startupIntent)
            finish()
        }
    }
}