package com.gustavclausen.bikeshare.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.fragments.BikeDetailFragment

class BikeDetailActivity : SingleFragmentActivity() {

    companion object {
        private const val EXTRA_BIKE_LOCK_ID = "com.gustavclausen.bikeshare.bike_lock_id"

        fun newIntent(packageContext: Context, bikeLockId: String): Intent {
            val intent = Intent(packageContext, BikeDetailActivity::class.java)
            intent.putExtra(EXTRA_BIKE_LOCK_ID, bikeLockId)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.title_bike_detail)
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
        val bikeLockId = intent.getStringExtra(EXTRA_BIKE_LOCK_ID)

        return BikeDetailFragment.newInstance(bikeLockId)
    }
}