package de.hbch.traewelling.ui.main

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets.Type.systemBars
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.jcloquell.androidsecurestorage.SecureStorage
import de.c1710.filemojicompat_ui.views.picker.EmojiPackItemAdapter
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.databinding.ActivityMainBinding
import de.hbch.traewelling.events.UnauthorizedEvent
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.ui.login.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var secureStorage: SecureStorage
    lateinit var emojiPackItemAdapter: EmojiPackItemAdapter

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedEvent(@Suppress("UNUSED_PARAMETER") unauthorizedEvent: UnauthorizedEvent) {
        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
        secureStorage.removeObject(SharedValues.SS_JWT)
        TraewellingApi.jwt = ""
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setOnApplyWindowInsetsListener { view, insets ->
            val insetTop = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(systemBars()).top
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetTop
            }
            view.updatePadding(top = insetTop)
            insets
        }

        secureStorage = SecureStorage(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNavigationBar
        setSupportActionBar(binding.toolbarMain)
        setupWithNavController(bottomNavigationView, navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(
            setOf(R.id.dashboard_fragment, R.id.active_checkins_fragment, R.id.statisticsFragment, R.id.user_fragment)
        ))

        val secureStorage = SecureStorage(this)
        TraewellingApi.jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java)!!

        emojiPackItemAdapter = EmojiPackItemAdapter.get(this)
        
        checkVerifiedDomains()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkVerifiedDomains() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(DomainVerificationManager::class.java)
            val userState = manager.getDomainVerificationUserState(packageName)
            val unapprovedDomains = userState?.hostToStateMap
                ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }
            if (unapprovedDomains?.containsKey("traewelling.de") == true &&
                secureStorage.getObject(SharedValues.SS_VERIFY_DOMAINS, Boolean::class.java) != false) {
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle(getString(R.string.request_url_verification))
                alertDialog.setMessage(getString(R.string.request_url_verification_text))
                alertDialog.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.yes)
                ) { _, _ ->
                    startActivity(
                        Intent(
                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                            Uri.parse("package:${packageName}")
                        )
                    )
                }
                alertDialog.setButton(
                    AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.no)
                ) { _, _ ->
                    secureStorage.storeObject(SharedValues.SS_VERIFY_DOMAINS, false)
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }
        }
    }
}