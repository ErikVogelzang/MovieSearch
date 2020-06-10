package com.example.moviesearch.ui

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.example.moviesearch.R
import com.example.moviesearch.common.Common
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    //region Initialization

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var lastMainFragmentId = 0

    private fun initNavigation() {
        navView.setNavigationItemSelectedListener(setNavItemListener())
        val navController = findNavController(R.id.navHostFragment)
        lastMainFragmentId = navController.currentDestination!!.id
        navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            setDestinationChangedListener(destination)
        }
    }

    private fun initViews() {
        toolbar.title = Common.STRING_EMPTY
        setSupportActionBar(toolbar)
        drawer = drawer_layout
        toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer, R.string.navigation_drawer)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setNavItemListener(): NavigationView.OnNavigationItemSelectedListener {
        return object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val navCon = findNavController(R.id.navHostFragment)
                when (item.itemId) {
                    R.id.MovieSearchFragment ->  {
                        if (navCon.currentDestination?.id == R.id.MoviesSavedFragment) {
                            lastMainFragmentId = R.id.MovieSearchFragment
                            navCon.navigate(MoviesSavedFragmentDirections.actionMoviesSavedFragmentToMovieSearchFragment())
                        }
                    }
                    R.id.MoviesSavedFragment ->  {
                        if (navCon.currentDestination?.id == R.id.MovieSearchFragment) {
                            lastMainFragmentId = R.id.MoviesSavedFragment
                            navCon.navigate(MovieSearchFragmentDirections.actionMovieSearchFragmentToMoviesSavedFragment())
                        }
                    }
                }
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        }
    }

    private fun setDestinationChangedListener(destination: NavDestination) {
        when (destination.id) {
            R.id.MovieSearchFragment -> toggle.isDrawerIndicatorEnabled = true
            R.id.MoviesSavedFragment -> toggle.isDrawerIndicatorEnabled = true
            R.id.detailsFragment -> toggle.isDrawerIndicatorEnabled = false
        }
    }

    //endregion

    //region Android Lifecycle Functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initNavigation()
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
            if (lastMainFragmentId == R.id.MoviesSavedFragment)
                navController.navigate(MovieDetailsFragmentDirections.actionDetailsFragmentToMoviesSavedFragment())
            else if (lastMainFragmentId == R.id.MovieSearchFragment)
                navController.navigate(MovieDetailsFragmentDirections.actionDetailsFragmentToMovieSearchFragment())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }
        else if (!toggle.isDrawerIndicatorEnabled) {
            super.onBackPressed()
        }
    }

    //endregion
}
