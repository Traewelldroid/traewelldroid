package de.traewelling.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.jcloquell.androidsecurestorage.SecureStorage
import de.traewelling.R
import de.traewelling.api.TraewellingApi
import de.traewelling.databinding.ActivityMainBinding
import de.traewelling.shared.LoggedInUserViewModel
import de.traewelling.shared.SharedValues

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
            setOf(R.id.dashboard_fragment, R.id.active_checkins_fragment, R.id.user_fragment)
        ))

        val secureStorage = SecureStorage(this)
        TraewellingApi.jwt = secureStorage.getObject(SharedValues.SS_JWT, String::class.java)!!
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}