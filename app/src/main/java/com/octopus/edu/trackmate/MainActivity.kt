package com.octopus.edu.trackmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import com.octopus.edu.trackmate.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment

        val navController = navHostFragment.navController
        setupBottomNav(navController)
    }

    @OptIn(NavigationUiSaveStateControl::class)
    private fun setupBottomNav(navController: NavController) {
        NavigationUI.setupWithNavController(
            binding.bottomNav,
            navController,
            false,
        )
    }
}
