package de.hbch.traewelling.ui.info

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.hbch.traewelling.databinding.ActivityInfoBinding
import de.hbch.traewelling.ui.legal.LegalActivity

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
        intent.data = Uri.parse("https://github.com/jheubuch/traewelling-android")
        startActivity(intent)
    }

    fun viewLegalInfo() {
        startActivity(Intent(this, LegalActivity::class.java))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}