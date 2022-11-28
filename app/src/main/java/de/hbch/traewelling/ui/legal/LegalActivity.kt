package de.hbch.traewelling.ui.legal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.hbch.traewelling.databinding.ActivityLegalBinding

class LegalActivity : AppCompatActivity() {
    lateinit var binding: ActivityLegalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegalBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.webviewLegal.loadUrl("file:///android_asset/legal.html")

        setContentView(binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}