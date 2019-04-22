package com.gustavclausen.bikeshare.view.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.data.entities.Ride
import com.gustavclausen.bikeshare.view.activities.RideDetailActivity
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults

class RidesRecyclerAdapter(private val context: Context) :
    RealmRecyclerViewAdapter<Ride, RidesRecyclerAdapter.RideHolder>(null, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideHolder =
        RideHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: RideHolder, position: Int) {
        val ride = getItem(position)!!

        holder.bind(ride)
        // Open detail view on click
        holder.itemView.setOnClickListener {
            context.startActivity(RideDetailActivity.newIntent(context, ride.id))
        }
    }

    fun setRidesList(ridesList: RealmResults<Ride>) = updateData(ridesList)

    inner class RideHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_ride, parent, false)) {

        internal fun bind(ride: Ride) {
            val startAddress = itemView.findViewById(R.id.ride_start_address) as TextView
            startAddress.text = ride.startPositionAddress

            val endAddress = itemView.findViewById(R.id.ride_end_address) as TextView
            endAddress.text = ride.endPositionAddress

            val distance = itemView.findViewById(R.id.ride_distance) as TextView
            distance.text = context.getString(R.string.distance_km_text, ride.distanceKm)
        }
    }
}