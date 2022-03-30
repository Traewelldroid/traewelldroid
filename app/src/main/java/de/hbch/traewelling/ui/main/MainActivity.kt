package de.hbch.traewelling.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.Data
import de.hbch.traewelling.databinding.ActivityMainBinding
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SharedValues
import io.sentry.Sentry
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNavigationBar
        setupWithNavController(bottomNavigationView, navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(
            setOf(R.id.dashboard_fragment, R.id.active_checkins_fragment, R.id.statisticsFragment, R.id.user_fragment)
        ))

        val secureStorage = SecureStorage(this)
        TraewellingApi.jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java)!!

        navController.addOnDestinationChangedListener { _, _, _ -> getNotificationCount() }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getNotificationCount() {
        TraewellingApi
            .notificationService
            .getUnreadNotificationsCount()
            .enqueue(object: Callback<Data<Int>> {
                override fun onResponse(
                    call: retrofit2.Call<Data<Int>>,
                    response: Response<Data<Int>>
                ) {
                    if (response.isSuccessful) {
                        val count = response.body()
                        if (count != null) {
                            if (count.data == 0) {
                                binding.bottomNavigationBar.removeBadge(R.id.dashboard_fragment)
                            } else {
                                val badge =
                                    binding.bottomNavigationBar.getOrCreateBadge(R.id.dashboard_fragment)
                                badge.isVisible = true
                                badge.number = count.data
                            }
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<Data<Int>>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
    }
}