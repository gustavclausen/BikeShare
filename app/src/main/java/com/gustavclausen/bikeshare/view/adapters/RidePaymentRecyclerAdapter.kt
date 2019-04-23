package com.gustavclausen.bikeshare.view.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.data.entities.Ride
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults

class RidePaymentRecyclerAdapter(private val context: Context) :
    RealmRecyclerViewAdapter<Ride, RidePaymentRecyclerAdapter.RidePaymentHolder>(null, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RidePaymentHolder =
        RidePaymentHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: RidePaymentHolder, position: Int) =
        holder.bind(getItem(position)!!)

    fun setList(rides: RealmResults<Ride>) = updateData(rides)

    inner class RidePaymentHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_payment, parent, false)) {

        internal fun bind(ride: Ride) {
            val riderFullNameField = itemView.findViewById<TextView>(R.id.payment_rider_full_name)
            riderFullNameField.text = ride.rider?.fullName

            val paymentAmountField = itemView.findViewById<TextView>(R.id.payment_amount)
            paymentAmountField.text = context.getString(R.string.money_amount_text, ride.finalPrice)
        }
    }
}