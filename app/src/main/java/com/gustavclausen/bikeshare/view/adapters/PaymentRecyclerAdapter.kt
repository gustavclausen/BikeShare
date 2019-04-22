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

class PaymentRecyclerAdapter(private val context: Context) :
    RealmRecyclerViewAdapter<Ride, PaymentRecyclerAdapter.PaymentHolder>(null, true) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHolder =
        PaymentHolder(LayoutInflater.from(context), parent)

    override fun onBindViewHolder(holder: PaymentHolder, position: Int) =
        holder.bind(getItem(position)!!)

    fun setPaymentList(rides: RealmResults<Ride>) = updateData(rides)

    inner class PaymentHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_payment, parent, false)) {

        internal fun bind(ride: Ride) {
            val riderFullName = itemView.findViewById(R.id.payment_rider_full_name) as TextView
            riderFullName.text = ride.rider?.fullName

            val paymentAmount = itemView.findViewById(R.id.payment_amount) as TextView
            paymentAmount.text = context.getString(R.string.money_amount_text, ride.finalPrice)
        }
    }
}