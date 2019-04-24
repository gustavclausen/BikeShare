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
import com.gustavclausen.bikeshare.view.adapters.BikesRecyclerAdapter
import com.gustavclausen.bikeshare.viewmodels.BikeViewModel
import kotlinx.android.synthetic.main.fragment_bikes_overview.*

class BikesOverviewFragment : Fragment() {

    private lateinit var mBikeVM: BikeViewModel
    private lateinit var mBikeAdapter: BikesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBikeVM = ViewModelProviders.of(this).get(BikeViewModel::class.java)
        mBikeAdapter = BikesRecyclerAdapter(context!!, onDeleteEvent = { bike ->
            mBikeVM.delete(bike.lockId, context!!)
        })
        mBikeAdapter.setList(mBikeVM.allBikes)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bikes_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bike_list.layoutManager = LinearLayoutManager(activity)
        bike_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        bike_list.adapter = mBikeAdapter
    }
}