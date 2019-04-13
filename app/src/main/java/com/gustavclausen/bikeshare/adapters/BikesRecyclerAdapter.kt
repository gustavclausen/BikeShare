package com.gustavclausen.bikeshare.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.models.Bike
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class BikesRecyclerAdapter (private val context: Context, data: OrderedRealmCollection<Bike>) :
        RealmRecyclerViewAdapter<Bike, BikesRecyclerAdapter.BikeHolder>(data, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeHolder =
        BikeHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: BikeHolder, position: Int) = holder.bind(getItem(position)!!)

    inner class BikeHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_bike, parent, false)) {

        internal fun bind(bike: Bike) {
            val bikeType = itemView.findViewById(R.id.bike_list_item_type) as TextView
            bikeType.text = bike.type

            val lastKnownPositionAddress = itemView.findViewById(R.id.bike_list_item_address) as TextView
            lastKnownPositionAddress.text = bike.lastKnownPositionAddress

            val bikeInUse = itemView.findViewById(R.id.bike_list_item_in_use) as TextView
            bikeInUse.text = if (bike.inUse) context.getString(R.string.in_use)
                             else context.getString(R.string.not_in_use)
        }
    }
}