package de.hbch.traewelling.ui.info

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        binding.apply {
            infoActivity = this@InfoActivity
        }

        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(binding.root)
    }

    fun showOSSLicenses() {
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }

    fun viewOnGitHub() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BuildConfig.REPO_URL)
        startActivity(intent)
    }

    fun viewLegalInfo() {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .build()

        intent.launchUrl(this, Uri.parse(BuildConfig.PRIVACY_URL))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}