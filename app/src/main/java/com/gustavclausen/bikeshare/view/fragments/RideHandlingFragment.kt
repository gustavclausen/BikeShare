package com.gustavclausen.bikeshare.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R

class RideHandlingFragment : Fragment() {

    private lateinit var mRideId: String

    companion object {
        private const val ARG_RIDE_ID = "com.gustavclausen.bikeshare.arg_handling_ride_id"

        fun newInstance(rideId: String): RideHandlingFragment {
            val args = Bundle()
            args.putString(ARG_RIDE_ID, rideId)

            val fragment = RideHandlingFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride_handling, container, false)
    }
}