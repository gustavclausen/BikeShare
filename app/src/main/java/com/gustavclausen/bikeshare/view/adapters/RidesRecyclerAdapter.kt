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

class RidesRecyclerAdapter(private val context: Context, private val onDeleteEvent: ((Ride) -> Unit)?) :
    RealmRecyclerViewAdapter<Ride, RidesRecyclerAdapter.RideHolder>(null, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideHolder =
        RideHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: RideHolder, position: Int) {
        holder.bind(ride = getItem(position)!!)
    }

    fun setList(rideList: RealmResults<Ride>) = updateData(rideList)

    inner class RideHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_ride, parent, false)) {

        internal fun bind(ride: Ride) {
            // Open detail view of ride on click
            itemView.setOnClickListener {
                context.startActivity(RideDetailActivity.newIntent(context, ride.id))
            }

            // Delete ride on long click
            itemView.setOnLongClickListener {
                onDeleteEvent?.invoke(ride)
                true
            }

            val startAddressField = itemView.findViewById<TextView>(R.id.ride_start_address)
            startAddressField.text = ride.startPositionAddress

            val endAddressField = itemView.findViewById<TextView>(R.id.ride_end_address)
            endAddressField.text = ride.endPositionAddress

            val distanceField = itemView.findViewById<TextView>(R.id.ride_distance)
            distanceField.text = context.getString(R.string.km_placeholder_text, ride.distanceKm)
        }
    }
}