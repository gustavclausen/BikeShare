package com.gustavclausen.bikeshare.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.fragments.RegisterBikeFragment

class RegisterBikeActivity : SingleFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.title_register_bike)
    }

    override fun createFragment(): Fragment {
        return RegisterBikeFragment()
    }
}