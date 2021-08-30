package de.traewelling.ui.launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import de.traewelling.ui.login.LoginActivity

class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.launcherEvent.observe(this, { action ->
            when (action) {
                LaunchAction.LOGIN -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                LaunchAction.MAIN -> {
                    Toast.makeText(this, "Route to main", Toast.LENGTH_SHORT).show()
                }
                LaunchAction.ERROR -> {
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else -> {
                    finish()
                }
            }
        })
    }
}