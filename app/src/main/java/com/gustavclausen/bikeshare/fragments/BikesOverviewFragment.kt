package com.gustavclausen.bikeshare.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.adapters.BikesRecyclerAdapter
import com.gustavclausen.bikeshare.models.Bike
import com.gustavclausen.bikeshare.viewmodel.BikeListViewModel
import io.realm.RealmResults

class BikesOverviewFragment : Fragment() {

    private lateinit var mBikeListVM: BikeListViewModel
    private lateinit var mBikeAdapter: BikesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bikes_overview, container, false)

        mBikeAdapter = BikesRecyclerAdapter(context!!)

        val bikesList = view.findViewById(R.id.bike_list) as RecyclerView
        bikesList.layoutManager = LinearLayoutManager(activity)
        bikesList.adapter = mBikeAdapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBikeListVM = ViewModelProviders.of(this).get(BikeListViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        mBikeListVM.allBikes.addChangeListener { realm: RealmResults<Bike> ->
            mBikeAdapter.setBikesList(realm)
        }
    }
}