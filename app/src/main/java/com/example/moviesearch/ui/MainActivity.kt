package com.example.moviesearch.ui

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
lateinit var color: Color
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var lastMainFragmentId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.title = Common.STRING_EMPTY
        setSupportActionBar(toolbar)
        initNavigation()
    }

    private fun initNavigation() {
        navView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val navCon = findNavController(R.id.navHostFragment)
                when (item.itemId) {
                    R.id.MovieSearchFragment ->  {
                        if (navCon.currentDestination?.id == R.id.MoviesSavedFragment) {
                            lastMainFragmentId = R.id.MovieSearchFragment
                            val action = MoviesSavedFragmentDirections.actionMoviesSavedFragmentToMovieSearchFragment()
                            navCon.navigate(action)
                        }
                    }
                    R.id.MoviesSavedFragment ->  {
                        if (navCon.currentDestination?.id == R.id.MovieSearchFragment) {
                            lastMainFragmentId = R.id.MoviesSavedFragment
                            val action = MovieSearchFragmentDirections.actionMovieSearchFragmentToMoviesSavedFragment()
                            navCon.navigate(action)
                        }
                    }
                }
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        })
        drawer = drawer_layout
        toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer, R.string.navigation_drawer)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val navController = findNavController(R.id.navHostFragment)
        lastMainFragmentId = navController.currentDestination!!.id
        navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            when (destination.id) {
                R.id.MovieSearchFragment -> showNAvigationDrawer(true)
                R.id.MoviesSavedFragment -> showNAvigationDrawer(true)
                R.id.detailsFragment -> showNAvigationDrawer(false)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        } else if (item?.itemId == android.R.id.home) {
            val navController = findNavController(R.id.navHostFragment)
            if (lastMainFragmentId == R.id.MoviesSavedFragment) {
                val action = DetailsFragmentDirections.actionDetailsFragmentToMoviesSavedFragment()
                navController.navigate(action)
            }
            else if (lastMainFragmentId == R.id.MovieSearchFragment) {
                val action = DetailsFragmentDirections.actionDetailsFragmentToMovieSearchFragment()
                navController.navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }
        else
            super.onBackPressed()
    }

    private fun showNAvigationDrawer(visible: Boolean) {
        toggle.isDrawerIndicatorEnabled = visible
    }
}
