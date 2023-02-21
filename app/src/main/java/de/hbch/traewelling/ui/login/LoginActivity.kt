package de.hbch.traewelling.ui.login

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.updatePadding
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.databinding.ActivityLoginBinding
import de.hbch.traewelling.ui.info.InfoActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.apply {
            loginActivity = this@LoginActivity
            lifecycleOwner = this@LoginActivity
        }
        setContentView(binding.root)
    }

    fun initiateOAuthLogin() {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()

        val url = BuildConfig.OAUTH_SERVER.buildUpon()
            .path("auth")
            .build()

        intent.launchUrl(this, url)
    }

    fun showInfoActivity() {
        startActivity(Intent(this, InfoActivity::class.java))
    }
}