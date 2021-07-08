package com.scaler.microblogs

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.scaler.microblogs.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration:AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = binding.navView

         val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
         appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        navController.let {
            bottomNavigationView.setupWithNavController(navController)
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_account
                || destination.id == R.id.nav_feed || destination.id == R.id.nav_tags
            ) {
                bottomNavigationView.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}