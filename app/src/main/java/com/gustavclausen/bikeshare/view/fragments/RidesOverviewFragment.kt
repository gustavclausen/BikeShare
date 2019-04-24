package com.gustavclausen.bikeshare.view.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.adapters.RidesRecyclerAdapter
import com.gustavclausen.bikeshare.viewmodels.RideViewModel
import kotlinx.android.synthetic.main.fragment_rides_overview.*

class RidesOverviewFragment : Fragment() {

    private lateinit var mRideVM: RideViewModel
    private lateinit var mRideAdapter: RidesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRideVM = ViewModelProviders.of(this).get(RideViewModel::class.java)
        mRideAdapter = RidesRecyclerAdapter(context!!, onDeleteEvent = { ride ->
            mRideVM.deleteRide(ride.id)
        })
        mRideAdapter.setList(mRideVM.endedRides)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rides_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ride_list.layoutManager = LinearLayoutManager(activity)
        ride_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        ride_list.adapter = mRideAdapter
    }
}