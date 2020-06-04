package com.example.moviesearch.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.moviesearch.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initNavigation()
    }

    private fun initNavigation() {
        val navController = findNavController(R.id.navHostFragment)
        NavigationUI.setupWithNavController(navView, navController)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            when (destination.id) {
                R.id.MovieSearchFragment -> showFragmentUI(true)
                R.id.MoviesSavedFragment -> showFragmentUI(true)
                R.id.detailsFragment -> showFragmentUI(false)
            }
        }
    }

    private fun showFragmentUI(navBar: Boolean) {
        when (navBar) {
            true -> navView.visibility = View.VISIBLE
            false -> navView.visibility = View.GONE
        }
    }
}
