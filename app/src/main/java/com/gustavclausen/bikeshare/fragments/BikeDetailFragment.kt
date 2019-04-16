package com.gustavclausen.bikeshare.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R

class BikeDetailFragment : Fragment() {

    lateinit var mBikeLockId: String

    companion object {
        private const val ARG_BIKE_LOCK_ID = "com.gustavclausen.bikeshare.arg_bike_lock_id"

        fun newInstance(bikeLockId: String): BikeDetailFragment {
            val args = Bundle()
            args.putString(ARG_BIKE_LOCK_ID, bikeLockId)

            val fragment = BikeDetailFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBikeLockId = arguments!!.getString(ARG_BIKE_LOCK_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bike_detail, container, false)
    }
}