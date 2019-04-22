package com.gustavclausen.bikeshare.view.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.view.activities.BikeDetailActivity
import com.gustavclausen.bikeshare.data.entities.Bike
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults

class BikesRecyclerAdapter(private val context: Context) :
    RealmRecyclerViewAdapter<Bike, BikesRecyclerAdapter.BikeHolder>(null, true) {

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

    fun setBikesList(bikeList: RealmResults<Bike>) {
        updateData(bikeList)
    }

    inner class BikeHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_bike, parent, false)) {

        internal fun bind(bike: Bike) {
            val type = itemView.findViewById(R.id.bike_type) as TextView
            type.text = bike.type

            val lastLocationAddress = itemView.findViewById(R.id.bike_last_known_address) as TextView
            lastLocationAddress.text = bike.lastLocationAddress

            val inUse = itemView.findViewById(R.id.bike_in_use) as TextView
            inUse.text = if (bike.inUse) context.getString(R.string.in_use)
                         else context.getString(R.string.not_in_use)
        }
    }
}