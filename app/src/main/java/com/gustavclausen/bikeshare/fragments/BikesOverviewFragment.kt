package com.gustavclausen.bikeshare.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.adapters.BikesRecyclerAdapter
import com.gustavclausen.bikeshare.models.Bike
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_bikes_overview.*

class BikesOverviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bikes_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        overview_bikes_list.layoutManager = LinearLayoutManager(activity)

        Realm.getInstanceAsync(Realm.getDefaultConfiguration()!!, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                val bikes = realm.where(Bike::class.java).findAllAsync() ?: return

                overview_bikes_list.adapter = BikesRecyclerAdapter(context!!, bikes)
            }
        })
    }
}