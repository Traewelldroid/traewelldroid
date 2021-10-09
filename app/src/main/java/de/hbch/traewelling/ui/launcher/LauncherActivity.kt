package de.hbch.traewelling.ui.launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
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
        val startupActivity = when (secureStorage.getObject(SharedValues.SS_JWT, String::class.java)) {
            null -> LoginActivity::class.java
            else -> MainActivity::class.java
        }
        startActivity(Intent(this, startupActivity))
        finish()
    }
}