package com.gustavclausen.bikeshare.view.activities

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.gustavclausen.bikeshare.BikeShareApplication
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.utils.InternetConnectionUtils
import com.gustavclausen.bikeshare.view.fragments.AccountFragment
import com.gustavclausen.bikeshare.view.fragments.OverviewFragment
import com.gustavclausen.bikeshare.view.fragments.BikeMapFragment
import com.gustavclausen.bikeshare.view.fragments.EndRideFragment
import com.gustavclausen.bikeshare.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.activity_bike_share.*
import org.jetbrains.anko.doAsync

class BikeShareActivity : AppCompatActivity() {

    private lateinit var mUserPreferences: SharedPreferences

    // Listener for menu items in bottom navigation bar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        // Return (i.e. do nothing) if user presses the same navigation item
        if (bottom_navigation_bar.selectedItemId == item.itemId)
            return@OnNavigationItemSelectedListener false

        when (item.itemId) {
            R.id.navigation_ride -> {
                loadRideFragment()
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

        mUserPreferences = applicationContext.getSharedPreferences(
            BikeShareApplication.PREF_USER_FILE,
            Context.MODE_PRIVATE
        )

        // Load default fragment on launch
        if (savedInstanceState == null)
            loadRideFragment()

        bottom_navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        loginUser()
    }

    private fun loginUser() {
        doAsync {
            val registeredUserId = mUserPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

            // Create dummy user account if it is the first time user starts the app
            if (registeredUserId == null) {
                val userVM = ViewModelProviders.of(this@BikeShareActivity).get(UserViewModel::class.java)
                val newUserId = userVM.create("Frank Castle")

                // Save preferences
                val editor = mUserPreferences.edit()
                editor.putString(BikeShareApplication.PREF_USER_ID, newUserId)
                editor.apply()
            }
        }
    }

    fun getUserId(): String? {
        return mUserPreferences.getString(BikeShareApplication.PREF_USER_ID, null)
    }

    fun updateLastRide(rideId: String?) {
        val editor = mUserPreferences.edit()
        editor.putString(BikeShareApplication.PREF_LAST_RIDE_ID, rideId)
        editor.apply()
    }

    internal fun loadRideFragment() {
        val lastRideId = mUserPreferences.getString(BikeShareApplication.PREF_LAST_RIDE_ID, null)

        if (lastRideId == null) {
            // User does not have a on-going ride, thus load map with bikes
            loadFragment(BikeMapFragment())
        } else {
            // If user has on-going ride, load fragment where user can end ride
            loadFragment(EndRideFragment.newInstance(lastRideId))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
