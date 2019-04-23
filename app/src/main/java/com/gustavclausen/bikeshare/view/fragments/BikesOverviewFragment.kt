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
import com.gustavclausen.bikeshare.view.adapters.BikesRecyclerAdapter
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel

class BikesOverviewFragment : Fragment() {

    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mBikeAdapter: BikesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bikes_overview, container, false)

        mBikeVM = ViewModelProviders.of(this).get(BikeViewModel::class.java)

        mBikeAdapter = BikesRecyclerAdapter(context!!)
        mBikeAdapter.setList(mBikeVM.allBikes)

        val bikesList = view.findViewById(R.id.bike_list) as RecyclerView
        bikesList.layoutManager = LinearLayoutManager(activity)
        bikesList.adapter = mBikeAdapter

        return view
    }
}