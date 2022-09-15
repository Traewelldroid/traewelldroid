package de.hbch.traewelling.ui.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.hbch.traewelling.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        binding.apply {
            infoActivity = this@InfoActivity
        }

        setContentView(binding.root)
    }

    fun showOSSLicenses() {
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }
}