package com.gustavclausen.bikeshare.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.activities.BikeDetailActivity
import com.gustavclausen.bikeshare.models.Bike
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class BikesRecyclerAdapter (private val context: Context, data: OrderedRealmCollection<Bike>) :
        RealmRecyclerViewAdapter<Bike, BikesRecyclerAdapter.BikeHolder>(data, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeHolder =
        BikeHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: BikeHolder, position: Int) {
        val bike = getItem(position)!!

        holder.bind(bike)
        // Open detail view on click
        holder.itemView.setOnClickListener {
            context.startActivity(BikeDetailActivity.newIntent(context, bike.lockId))
        }
    }

    inner class BikeHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_bike, parent, false)) {

        internal fun bind(bike: Bike) {
            val type = itemView.findViewById(R.id.bike_list_item_type) as TextView
            type.text = bike.type

            val lastLocationAddress = itemView.findViewById(R.id.bike_list_item_address) as TextView
            lastLocationAddress.text = bike.lastLocationAddress

            val inUse = itemView.findViewById(R.id.bike_list_item_in_use) as TextView
            inUse.text = if (bike.inUse) context.getString(R.string.in_use)
                         else context.getString(R.string.not_in_use)
        }
    }
}