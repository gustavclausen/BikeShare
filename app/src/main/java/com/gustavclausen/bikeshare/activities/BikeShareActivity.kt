package com.gustavclausen.bikeshare.activities

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.gustavclausen.bikeshare.BikeShareApplication
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.fragments.AccountFragment
import com.gustavclausen.bikeshare.fragments.OverviewFragment
import com.gustavclausen.bikeshare.fragments.RideFragment
import com.gustavclausen.bikeshare.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_bike_share.*
import org.jetbrains.anko.doAsync

class BikeShareActivity : AppCompatActivity() {

    // Listener for menu items in bottom navigation bar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        // Return (i.e. do nothing) if user presses the same navigation item
        if (bottom_navigation.selectedItemId == item.itemId)
            return@OnNavigationItemSelectedListener false

        when (item.itemId) {
            R.id.navigation_ride -> {
                loadFragment(RideFragment())
                setTitle(R.string.title_ride)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_overview -> {
                loadFragment(OverviewFragment())
                setTitle(R.string.title_overview)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_account -> {
                loadFragment(AccountFragment())
                setTitle(R.string.title_account)
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_share)

        // Load default fragment on launch
        if (savedInstanceState == null)
            loadFragment(RideFragment())

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        doAsync {
            // User account configuration
            val userPreferences = applicationContext.getSharedPreferences(
                BikeShareApplication.PREF_USER_FILE,
                Context.MODE_PRIVATE
            )
            val registeredUserId = userPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

            // Create dummy user account if it is the first time user starts the app
            if (registeredUserId == null) {
                val newUserId = ViewModelProviders.of(this@BikeShareActivity).get(UserViewModel::class.java).create("Frank Castle")

                val editor = userPreferences.edit()
                editor.putString(BikeShareApplication.PREF_USER_ID, newUserId)
                editor.apply()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
