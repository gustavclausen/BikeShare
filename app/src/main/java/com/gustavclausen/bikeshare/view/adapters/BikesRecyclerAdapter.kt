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

class BikesRecyclerAdapter(private val context: Context, private val onDeleteEvent: ((Bike) -> Unit)?) :
    RealmRecyclerViewAdapter<Bike, BikesRecyclerAdapter.BikeHolder>(null, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeHolder =
        BikeHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: BikeHolder, position: Int) {
        val bike = getItem(position)!!

        holder.bind(bike)
    }

    fun setList(bikeList: RealmResults<Bike>) = updateData(bikeList)

    inner class BikeHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_bike, parent, false)) {

        internal fun bind(bike: Bike) {
            // Open detail view of bike on click
            itemView.setOnClickListener {
                context.startActivity(BikeDetailActivity.newIntent(context, bike.lockId))
            }

            // Delete bike on long click
            itemView.setOnLongClickListener {
                onDeleteEvent?.invoke(bike)
                true
            }

            val bikeTypeField = itemView.findViewById<TextView>(R.id.bike_type)
            bikeTypeField.text = bike.type

            val bikePositionAddressField = itemView.findViewById<TextView>(R.id.bike_position_address)
            bikePositionAddressField.text = bike.positionAddress

            val bikeInUseField = itemView.findViewById<TextView>(R.id.bike_in_use)
            bikeInUseField.text = if (bike.inUse) context.getString(R.string.in_use)
                                  else context.getString(R.string.not_in_use)
        }
    }
}