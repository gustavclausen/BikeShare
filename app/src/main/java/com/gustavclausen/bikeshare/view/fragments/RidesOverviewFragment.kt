package com.gustavclausen.bikeshare.view.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.adapters.RidesRecyclerAdapter
import com.gustavclausen.bikeshare.viewmodels.RideViewModel

class RidesOverviewFragment : Fragment() {

    private lateinit var mRideVM: RideViewModel
    private lateinit var mRideAdapter: RidesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rides_overview, container, false)

        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)

        mRideAdapter = RidesRecyclerAdapter(context!!)
        mRideAdapter.setList(mRideVM.endedRides)

        val ridesList = view.findViewById(R.id.ride_list) as RecyclerView
        ridesList.layoutManager = LinearLayoutManager(activity)
        ridesList.adapter = mRideAdapter

        return view
    }
}