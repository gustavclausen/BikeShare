package com.gustavclausen.bikeshare.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.fragments.RideDetailFragment

class RideDetailActivity : SingleFragmentActivity() {

    companion object {
        private const val EXTRA_RIDE_ID = "com.gustavclausen.bikeshare.detail_ride_id"

        fun newIntent(packageContext: Context, rideId: String): Intent {
            val intent = Intent(packageContext, RideDetailActivity::class.java)
            intent.putExtra(EXTRA_RIDE_ID, rideId)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.title_ride_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Displays the "back"-button in the action bar
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            /*
             * Finishes the activity and navigates the user back
             * to the activity that started this activity.
             */
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun createFragment(): Fragment {
        val rideId = intent.getStringExtra(EXTRA_RIDE_ID)

        return RideDetailFragment.newInstance(rideId)
    }
}